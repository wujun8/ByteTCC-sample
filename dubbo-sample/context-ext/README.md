## Context 扩展实现 Demo

> 实现原理：基于 Dubbo 提供的 Filter，对 RPC invocation 拦截并封装或解析 Context attachments.
> 具体是，在 consumer 发起调用前将当前 Context 序列化写入 invocation, 到 provider 反序列化设置当前 Context，return 前在将修改后的信息写入 Result，回到 consumer 读取并设置当前 Context.
> ***目前实现方式使用了 ByteTCC 的一个内部静态类，故 filter 包名称与其一致，后期会尝试并入 ByteTCC***

验证步骤：
1. 数据库、zookeeper 环境配置
2. 启动 dubbo-sample/sample-consumer ```com.bytesvc.main.ProviderMain```
3. 启动`dubbo-sample/sample-provider ``com.bytesvc.main.GenericConsumerMain```

**注意：**
consumer 配置 filter 顺序会影响 Context 传递

    <dubbo:reference filter="compensable,contextext"/>

