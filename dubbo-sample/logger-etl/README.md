# 分布式事务日志收集

本示例基于 Spring AOP 拦截事务日志，异步收集日志数据，数据发送到 MQ 未实现

* 独立线程池处理日志收集
* 日志收集动作异常统一处理

配置文件
    
	<aop:aspectj-autoproxy />
	<task:annotation-driven exception-handler="asyncExceptionHandler"/>
	<task:executor id="loggerTaskExecutor" pool-size="3-100" queue-capacity="1000" />
	<bean id="asyncExceptionHandler" class="com.bytesvc.async.AsyncExceptionHandler"/>
	
