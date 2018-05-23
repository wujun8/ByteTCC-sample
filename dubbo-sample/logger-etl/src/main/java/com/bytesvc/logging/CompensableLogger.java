package com.bytesvc.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.bytesoft.common.utils.ByteUtils;
import org.bytesoft.compensable.CompensableBeanFactory;
import org.bytesoft.compensable.archive.CompensableArchive;
import org.bytesoft.compensable.archive.TransactionArchive;
import org.bytesoft.compensable.aware.CompensableBeanFactoryAware;
import org.bytesoft.compensable.aware.CompensableEndpointAware;
import org.bytesoft.transaction.archive.XAResourceArchive;
import org.bytesoft.transaction.xa.TransactionXid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Aspect
@Component
@Async("loggerTaskExecutor")
public class CompensableLogger {
    static final Logger logger = LoggerFactory.getLogger(CompensableLogger.class);

    @Resource
    private CompensableBeanFactory beanFactory;

    @Pointcut("execution(public * org.bytesoft.compensable.logging.CompensableLogger.*(..))")
    public void loggerPoint() {}

    @AfterReturning("loggerPoint() && args(archive)")
    public void loggerTransactionArchive(JoinPoint joinPoint, TransactionArchive archive) throws IOException {
        logger.debug(joinPoint.toString());
        logger.info("Transaction: {}", archive.getXid());
        byte[] bytes = this.beanFactory.getArchiveDeserializer().serialize((TransactionXid) archive.getXid(), archive);
        // one-way send to MQ
        // RocketMQ putUserProperty
        // timestamp, ...
        // action: "create-tx"
        logger.debug(ByteUtils.byteArrayToString(bytes));
    }

    @AfterReturning("loggerPoint() && args(archive)")
    public void loggerCompensableArchive(JoinPoint joinPoint, CompensableArchive archive) {
        logger.debug(joinPoint.toString());
        logger.debug("Compensable: {}", archive.getIdentifier());
        //serialize and send
    }

    @AfterReturning("loggerPoint() && args(archive)")
    public void loggerXAResourceArchive(JoinPoint joinPoint, XAResourceArchive archive) {
        logger.debug(joinPoint.toString());
        logger.debug("XAResource: {}", archive.getDescriptor().getIdentifier());
        // serialize and send
    }


    @Pointcut("execution(public * org.bytesoft.compensable.logging.CompensableLogger.recover(..))")
    public void recoverPoint() {}

    @Before("recoverPoint()")
    public void loggerRecoverStart(JoinPoint joinPoint) {
        logger.debug(joinPoint.toString());
        // send
    }

    @AfterReturning("recoverPoint()")
    public void loggerRecoverEnd(JoinPoint joinPoint) {
        logger.debug(joinPoint.toString());
        // send
    }

    public void setBeanFactory(CompensableBeanFactory tbf) {
        this.beanFactory = tbf;
    }

}
