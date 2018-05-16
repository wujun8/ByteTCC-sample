package com.bytesvc.ext;

import org.bytesoft.compensable.TransactionContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TransactionContextWrapper implements Serializable {

    private transient TransactionContext context;
    private Map<String, Serializable> attachments;

    public TransactionContextWrapper(TransactionContext context) {
        this.context = context;
    }

    public TransactionContextWrapper(TransactionContext context, Map<String, Serializable> attachments) {
        this.context = context;
        this.attachments = attachments;
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
