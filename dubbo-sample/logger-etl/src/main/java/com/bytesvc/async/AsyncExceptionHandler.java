package com.bytesvc.async;

import com.bytesvc.logging.CompensableLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class AsyncExceptionHandler extends SimpleAsyncUncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        logger.error(String.format("Unexpected error occurred invoking async " +
                "method '%s'.", method), ex);
        if (method.getClass().isAssignableFrom(CompensableLogger.class)) {
            logger.error("CompensableLogger error!");
        }
    }
}
