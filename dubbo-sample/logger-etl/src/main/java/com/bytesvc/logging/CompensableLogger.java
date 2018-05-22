package com.bytesvc.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.bytesoft.compensable.archive.CompensableArchive;
import org.bytesoft.compensable.archive.TransactionArchive;
import org.bytesoft.transaction.archive.XAResourceArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Async("loggerTaskExecutor")
public class CompensableLogger {
    static final Logger logger = LoggerFactory.getLogger(CompensableLogger.class);

    @Pointcut("execution(public * org.bytesoft.compensable.logging.CompensableLogger.*(..))")
    public void loggerPoint() {}

    @AfterReturning("loggerPoint() && args(archive)")
    public void loggerTransactionArchive(JoinPoint joinPoint, TransactionArchive archive) {
        logger.debug(joinPoint.toString());
        logger.debug("Transaction: {}", archive.getXid());
    }

    @AfterReturning("loggerPoint() && args(archive)")
    public void loggerCompensableArchive(JoinPoint joinPoint, CompensableArchive archive) {
        logger.debug(joinPoint.toString());
        logger.debug("Compensable: {}", archive.getIdentifier());
    }

    @AfterReturning("loggerPoint() && args(archive)")
    public void loggerXAResourceArchive(JoinPoint joinPoint, XAResourceArchive archive) {
        logger.debug(joinPoint.toString());
        logger.debug("XAResource: {}", archive.getDescriptor().getIdentifier());
    }


    @Pointcut("execution(public * org.bytesoft.compensable.logging.CompensableLogger.recover(..))")
    public void recoverPoint() {}

    @Before("recoverPoint()")
    public void loggerRecoverStart(JoinPoint joinPoint) {
        logger.debug(joinPoint.toString());
    }

    @AfterReturning("recoverPoint()")
    public void loggerRecoverEnd(JoinPoint joinPoint) {
        logger.debug(joinPoint.toString());
    }
}
