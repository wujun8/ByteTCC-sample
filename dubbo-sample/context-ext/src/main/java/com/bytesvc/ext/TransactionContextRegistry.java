package com.bytesvc.ext;

import com.alibaba.dubbo.rpc.Invocation;
import org.bytesoft.bytetcc.supports.dubbo.CompensableBeanRegistry;
import org.bytesoft.compensable.CompensableBeanFactory;
import org.bytesoft.compensable.CompensableManager;
import org.bytesoft.compensable.CompensableTransaction;
import org.bytesoft.compensable.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        TransactionContextWrapper originalContextWrapper = CURRENT.get();
        if (originalContextWrapper == null) {
            CURRENT.set(contextWrapper);
        } else {
            //do not change the object ref
            originalContextWrapper.setContext(contextWrapper.getContext());
            originalContextWrapper.setAttachments(contextWrapper.getAttachments());
        }
    }

    public TransactionContextWrapper getCurrentContextWrapper() {
        TransactionContextWrapper contextWrapper = CURRENT.get();
        if (contextWrapper == null) {
            contextWrapper = new TransactionContextWrapper(getCurrentContext());
            CURRENT.set(contextWrapper);
        }
        return contextWrapper;
    }

    public static TransactionContext getCurrentContext() {
        CompensableBeanRegistry beanRegistry = CompensableBeanRegistry.getInstance();
        CompensableBeanFactory beanFactory = beanRegistry.getBeanFactory();
        CompensableManager transactionManager = beanFactory.getCompensableManager();
        CompensableTransaction transaction = transactionManager.getCompensableTransactionQuietly();
        return transaction == null ? null : transaction.getTransactionContext();
    }

    public static TransactionContextWrapper readWrapperFromDubboInvocation(Invocation invocation) {
        String contextWrapperContent = invocation.getAttachment(TransactionContextWrapper.class.getName());
        return TransactionContextWrapper.readWrapper(contextWrapperContent, getCurrentContext());
    }

}
