package org.bytesoft.bytetcc.supports.dubbo.spi;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.bytesvc.ext.TransactionContextRegistry;
import com.bytesvc.ext.TransactionContextWrapper;
import org.bytesoft.compensable.RemotingException;

import java.io.Serializable;
import java.util.Map;


@Activate(order = 1)
public class ContextExtFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (RpcContext.getContext().isProviderSide()) {
            return this.providerInvoke(invoker, invocation);
        } else {
            return this.consumerInvoke(invoker, invocation);
        }
    }

    protected Result consumerInvoke(Invoker<?> invoker, Invocation invocation) {
        //before invoke provider, write context
        TransactionContextRegistry contextRegistry = TransactionContextRegistry.getInstance();
        TransactionContextWrapper contextWrapper = contextRegistry.getCurrentContextWrapper();
        invocation.getAttachments().put(TransactionContextWrapper.class.getName(), contextWrapper.writeToString());

        Result result = invoker.invoke(invocation);
        //after invoke provider, read context
        return unwrapResult(result);
    }

    protected Result providerInvoke(Invoker<?> invoker, Invocation invocation) {
        //before invoke provider, read context
        TransactionContextRegistry contextRegistry = TransactionContextRegistry.getInstance();
        TransactionContextWrapper contextWrapper = TransactionContextRegistry.readWrapperFromDubboInvocation(invocation);
        contextRegistry.setCurrentContextWrapper(contextWrapper);

        Result result = null;
        try {
            result = invoker.invoke(invocation);
        } catch (RpcException e) {
            throw e;
        } catch (Throwable e) {
            throw new RpcException(e.getMessage());
        }
        //after invoke provider, write context
        contextWrapper = contextRegistry.getCurrentContextWrapper();
        return wrapResult(result, contextWrapper.writeToString());
    }

    private Result wrapResult(Result result, String contextWrapperContent) {
        if (result == null) {
            return null;
        }
        if (!(result instanceof RpcResult)) {
            throw new RpcException("result not RpcResult, not supported");
        }
        RpcResult rpcResult = (RpcResult) result;
        Object value = result.getValue();
        if (value instanceof CompensableServiceFilter.InvocationResult) {
            CompensableServiceFilter.InvocationResult wrapped = (CompensableServiceFilter.InvocationResult) value;
            if (wrapped.isFailure()) {
                Throwable serverError = wrapped.getError();
                if (serverError instanceof RpcException) {
                    throw (RpcException) wrapped.getError();
                } else if (serverError instanceof RemotingException) {
                    throw new RpcException(serverError.getMessage());
                }
                return errorResult(serverError, contextWrapperContent);
            }
            wrapped.setVariable(TransactionContextWrapper.class.getName(), contextWrapperContent);
            rpcResult.setValue(wrapped);
        }
        return rpcResult;
    }

    private Result errorResult(Throwable throwable, String contextWrapperContent) {
        RpcResult rpcResult = new RpcResult();
        CompensableServiceFilter.InvocationResult wrapped = new CompensableServiceFilter.InvocationResult();
        wrapped.setError(throwable);
        wrapped.setVariable(TransactionContextWrapper.class.getName(), contextWrapperContent);
        rpcResult.setValue(wrapped);
        return rpcResult;
    }

    private Result unwrapResult(Result result) {
        if (result == null) {
            return null;
        }
        if (!(result instanceof RpcResult)) {
            throw new RpcException("result not RpcResult, not supported");
        }
        RpcResult rpcResult = (RpcResult) result;
        Object value = rpcResult.getValue();
        if (value instanceof CompensableServiceFilter.InvocationResult) {
            CompensableServiceFilter.InvocationResult wrapped = (CompensableServiceFilter.InvocationResult) value;
            //read context from wrapped Result
            String contextWrapperContent = (String) wrapped.getVariable(TransactionContextWrapper.class.getName());
            TransactionContextWrapper contextWrapper = TransactionContextWrapper.readWrapper(contextWrapperContent, TransactionContextRegistry.getCurrentContext());
            TransactionContextRegistry.getInstance().setCurrentContextWrapper(contextWrapper);

            rpcResult.setValue(null);
            rpcResult.setException(null);
            if (wrapped.isFailure()) {
                rpcResult.setException(wrapped.getError());
            } else {
                rpcResult.setValue(wrapped.getValue());
            }
        }
        return rpcResult;
    }
}
