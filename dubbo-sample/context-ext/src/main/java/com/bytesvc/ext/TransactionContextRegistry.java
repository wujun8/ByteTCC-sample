package com.bytesvc.ext;

import com.alibaba.dubbo.rpc.Invocation;
import org.bytesoft.common.utils.ByteUtils;
import org.bytesoft.common.utils.SerializeUtils;
import org.bytesoft.compensable.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TransactionContextRegistry {
    static final Logger logger = LoggerFactory.getLogger(TransactionContextRegistry.class);

    private static TransactionContextRegistry instance;
    private final ThreadLocal<TransactionContextWrapper> CURRENT = new ThreadLocal<TransactionContextWrapper>();

    private TransactionContextRegistry() {
    }

    public static TransactionContextRegistry getInstance() {
        if (instance == null) {
            instance = new TransactionContextRegistry();
        }
        return instance;
    }


    public void setCurrentContextWrapper(TransactionContextWrapper contextWrapper) {
        CURRENT.set(contextWrapper);
    }

    public TransactionContextWrapper getCurrentContextWrapper() {
        TransactionContextWrapper contextWrapper = CURRENT.get();
        if (contextWrapper == null) {
            contextWrapper = new TransactionContextWrapper(null);
            CURRENT.set(contextWrapper);
        }
        return contextWrapper;
    }

    public TransactionContext getCurrentContext() {
        TransactionContextWrapper contextWrapper = getCurrentContextWrapper();
        return contextWrapper == null ? null : contextWrapper.getContext();
    }

    public TransactionContext readFromInvocation(Invocation invocation) {
        String transactionContextContent = invocation.getAttachment(TransactionContext.class.getName());
        byte[] byteArray = ByteUtils.stringToByteArray(transactionContextContent);
        TransactionContext transactionContext = null;
        try {
            transactionContext = (TransactionContext) SerializeUtils.hessianDeserialize(byteArray);
        } catch (IOException e) {
            logger.warn("read TransactionContext from invocation failed", e);
        }
        return transactionContext;
    }

    public TransactionContextWrapper readWrapperFromInvocation(Invocation invocation) {
        String contextWrapperContent = invocation.getAttachment(TransactionContextWrapper.class.getName());
        if (contextWrapperContent != null) {
            byte[] byteArray = ByteUtils.stringToByteArray(contextWrapperContent);
            try {
                return (TransactionContextWrapper) SerializeUtils.kryoDeserialize(byteArray);
            } catch (IOException e) {
                logger.warn("read TransactionContextWrapper from invocation failed", e);
            }
        }
        return new TransactionContextWrapper(readFromInvocation(invocation));
    }

    public String writeWrapperToString(TransactionContextWrapper contextWrapper) {
        byte[] byteArray = null;
        try {
            byteArray = SerializeUtils.kryoSerialize(contextWrapper);
        } catch (IOException e) {
            logger.warn("write TransactionContextWrapper from invocation failed", e);
            return null;
        }
        return ByteUtils.byteArrayToString(byteArray);
    }
}
