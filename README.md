# roc-retry

roc-retry 是支持过程式编程和注解编程的 java 重试框架



## **特性描述**

* 使用 **Builder 模式** ，支持优雅的 **Fluent API** 编程风格
* 基于字节码的代理重试
* 基于**注解**的重试机制，允许用户自定义注解
* 提供**多种支持策略**，包括阻塞、监听、恢复、等待和终止等策略
* 采用 Netty 类似的接口**API设计**思想，保证接口的一致性，和替换的灵活性
* 无缝接入 Spring\Spring-Boot



## 注解

### Retry

用于指定重试的相关配置

### RetryWait

用于指定重试的等待策略



## 快速开始

### 引入

```xml
<dependency>
    <groupId>io.github.poldroc</groupId>
    <artifactId>retry-core</artifactId>
    <version>1.0</version>
</dependency>
```



