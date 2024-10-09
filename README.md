# roc-retry

roc-retry 是支持过程式编程和注解编程的 java 重试框架



## **特性描述**

* 使用 **Builder 模式** ，支持优雅的 **Fluent API** 编程风格
* 基于字节码的代理重试
* 基于**注解**的重试机制，允许用户自定义注解
* 允许依赖异常、**返回值的某个状态**来作为触发重试的条件
* 提供**多种支持策略**，包括阻塞、监听、恢复、等待和终止等策略
* 采用 Netty 类似的接口**API设计**思想，保证接口的一致性，和替换的灵活性
* 无缝接入 Spring\Spring-Boot



## 快速开始

### 引入

```xml
<dependency>
    <groupId>io.github.poldroc</groupId>
    <artifactId>retry-core</artifactId>
    <version>1.1</version>
</dependency>
```



### 简单入门

```java
public class RetryerTest {
    /**
     * 默认异常进行重试
     */
    @Test(expected = RuntimeException.class)
    public void helloTest() {
        Retryer.<String>newInstance()
                .callable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("called...");
                        throw new RuntimeException();
                    }
                }).retryCall();
    }

    /**
     * 默认配置测试
     */
    @Test(expected = RuntimeException.class)
    public void defaultConfigTest() {
        Retryer.<String>newInstance()
                .maxAttempt(4)
                .listen(RetryListens.noListen())
                .recover(Recovers.noRecover())
                .condition(RetryConditions.hasExceptionCause())
                .retryWaitContext(RetryWaiter.<String>retryWait(NoRetryWait.class).context())
                .callable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("called...");
                        throw new RuntimeException();
                    }
                }).retryCall();
    }
}
```





## 注解使用

### 引入

```xml
<dependency>
    <groupId>io.github.poldroc</groupId>
    <artifactId>retry-annotation</artifactId>
    <version>1.1</version>
</dependency>
```

[使用详情](https://github.com/Poldroc/roc-retry/blob/master/retry-test/src/test/java/com/poldroc/retry/test/springboot/RocRetryApplicationTest.java)

### Retry

用于指定重试的相关配置

```java
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RetryAble(DefaultRetryAbleHandler.class)
public @interface Retry {

    /**
     * 重试类实现
     *
     * @return 重试
     */
    Class<? extends com.poldroc.retry.api.core.Retry> retry() default DefaultRetry.class;

    /**
     * 最大尝试次数
     * 1. 默认为3 包含方法第一次正常执行的次数
     *
     * @return 次数
     */
    int maxAttempt() default 3;

    /**
     * 重试触发的场景
     * 1. 默认为异常触发
     *
     * @return 重试触发的场景
     */
    Class<? extends RetryCondition> condition() default ExceptionCauseRetryCondition.class;

    /**
     * 监听器
     * 1. 默认不进行监听
     *
     * @return 监听器
     */
    Class<? extends RetryListen> listen() default NoRetryListen.class;

    /**
     * 恢复操作
     * 1. 默认不进行任何恢复操作
     *
     * @return 恢复操作对应的类
     */
    Class<? extends Recover> recover() default NoRecover.class;

    /**
     * 重试等待策略
     * 1. 支持指定多个，如果不指定，则不进行任何等待，
     *
     * @return 重试等待策略
     */
    RetryWait[] waits() default {};
}

```



### RetryWait

用于指定重试的等待策略

```java
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@RetryWaitAble(DefaultRetryWaitAbleHandler.class)
public @interface RetryWait {

    /**
     * 默认值
     * 1. fixed 模式，则对应固定等待时间
     * 2. 递增
     * @return 默认值
     */
    long value() default RetryWaitConst.DEFAULT_VALUE_MILLS;

    /**
     * 最小值
     * @return 最小值
     */
    long min() default RetryWaitConst.DEFAULT_MIN_MILLS;

    /**
     * 最大值
     * @return 最大值
     */
    long max() default RetryWaitConst.DEFAULT_MAX_MILLS;

    /**
     * 影响因数
     * 1. 递增重试，默认为 {@link RetryWaitConst#INCREASE_MILLS_FACTOR}
     * 2. 指数模式。默认为 {@link RetryWaitConst#MULTIPLY_FACTOR}
     * @return 影响因数
     */
    double factor() default Double.MIN_VALUE;

    /**
     * 指定重试的等待时间 class 信息
     * @return 重试等待时间 class
     */
    Class<? extends com.poldroc.retry.api.support.wait.RetryWait> retryWait() default NoRetryWait.class;

}

```

