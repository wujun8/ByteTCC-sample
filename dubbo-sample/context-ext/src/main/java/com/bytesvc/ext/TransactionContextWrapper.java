package com.bytesvc.ext;

import org.bytesoft.bytetcc.supports.dubbo.CompensableBeanRegistry;
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
    private transient String localPrefix;
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
        this(context, null);
    }

    public TransactionContextWrapper(TransactionContext context, Map<String, Serializable> attachments) {
        setContext(context);
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
        if (context == null) {
            //just start
            return;
        }
        this.context = context;
    }

    /**
     *
     * @return prefix string of key, diff between branch transactions
     */
    public String getLocalPrefix() {
        if (this.localPrefix == null) {
            try {
                this.localPrefix = CompensableBeanRegistry.getInstance().getBeanFactory().getCompensableCoordinator().getIdentifier();
            } catch (Throwable e) {
                String error = "error occurred when trying to get CompensableCoordinator#identifier";
                logger.error(error, e);
                throw new ContextException(error, e);
            }
        }
        return this.localPrefix;
    }

    public Map<String, Serializable> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Serializable> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String key, Serializable attachment) {
        addAttachment(key, attachment, true);
    }

    /**
     * if local return local, then if global return global
     * @param key
     * @return the attachment in local(first return) or global
     */
    public Serializable getAttachment(String key) {
        Serializable value = getAttachment(key, true);
        return value != null ? value : getGlobalAttachment(key);
    }

    public Serializable getLocalAttachment(String key) {
        return getAttachment(key, true);
    }

    public void addGlobalAttachment(String key, Serializable attachment) {
        addAttachment(key, attachment, false);
    }

    public Serializable getGlobalAttachment(String key) {
        return getAttachment(key, false);
    }

    public Serializable getAttachment(String key, Serializable defaultValue) {
        Serializable value = getAttachment(key);
        return value != null ? value : defaultValue;
    }

    private String wrapKey(String key) {
        return String.format("%s_%s", getLocalPrefix(), key);
    }

    private String unwrapKey(String key) {
        return key.substring(getLocalPrefix().length());
    }

    private void addAttachment(String key, Serializable attachment, boolean local) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null!");
        }
        if (this.attachments == null) {
            this.attachments = new HashMap<String, Serializable>();
        }
        String internalKey = key;
        if (local) {
            internalKey = wrapKey(key);
        }
        this.attachments.put(internalKey, attachment);
    }

    private Serializable getAttachment(String key, boolean local) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null!");
        }
        if (this.attachments == null) {
            return null;
        }
        String internalKey = key;
        if (local) {
            internalKey = wrapKey(key);
        }
        return this.attachments.get(internalKey);
    }
}
