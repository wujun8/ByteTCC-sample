package com.bytesvc.ext;

import org.bytesoft.common.utils.ByteUtils;
import org.bytesoft.common.utils.SerializeUtils;
import org.bytesoft.compensable.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TransactionContextWrapper implements Serializable {
    static final Logger logger = LoggerFactory.getLogger(TransactionContextWrapper.class);

    private transient TransactionContext context;
    private Map<String, Serializable> attachments;

    public static TransactionContextWrapper readWrapper(String contextWrapperContent, TransactionContext context) {
        if (contextWrapperContent != null) {
            byte[] byteArray = ByteUtils.stringToByteArray(contextWrapperContent);
            try {
                TransactionContextWrapper contextWrapper = (TransactionContextWrapper) SerializeUtils.kryoDeserialize(byteArray);
                contextWrapper.setContext(context);
                return contextWrapper;
            } catch (IOException e) {
                logger.warn("read TransactionContextWrapper from invocation failed", e);
            }
        }
        return new TransactionContextWrapper(context);
    }

    public TransactionContextWrapper(TransactionContext context) {
        this.context = context;
    }

    public TransactionContextWrapper(TransactionContext context, Map<String, Serializable> attachments) {
        this.context = context;
        this.attachments = attachments;
    }

    public String writeToString() {
        byte[] byteArray = null;
        try {
            byteArray = SerializeUtils.kryoSerialize(this);
        } catch (IOException e) {
            logger.warn("write TransactionContextWrapper from invocation failed", e);
            return null;
        }
        return ByteUtils.byteArrayToString(byteArray);
    }

    public TransactionContext getContext() {
        return context;
    }

    public void setContext(TransactionContext context) {
        this.context = context;
    }

    public Map<String, Serializable> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Serializable> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String key, Serializable attachment) {
        if (this.attachments == null) {
            this.attachments = new HashMap<String, Serializable>();
        }
        this.attachments.put(key, attachment);
    }

    public Serializable getAttachment(String key) {
        if (this.attachments == null) {
            return null;
        }
        return this.attachments.get(key);
    }

    public Serializable getAttachment(String key, Serializable defaultValue) {
        Serializable value = getAttachment(key);
        return value != null ? value : defaultValue;
    }
}
