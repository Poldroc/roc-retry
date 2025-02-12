# roc-retry

roc-retry 是支持过程式编程和注解编程的 java 重试框架



## **特性描述**

* 使用 **Builder 模式** ，支持优雅的 **Fluent API** 编程风格
* 基于CGLIB字节码的代理重试
* 基于**注解**的重试机制，允许用户自定义注解
* 允许**依赖异常、返回值的某个状态**来作为触发重试的条件。
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



### 注解解析流程



<img src="https://engroc.oss-cn-fuzhou.aliyuncs.com/Typora/202410170130335.png" width="50%" />

## 模块介绍

### retry-api

* 接口定义模块

* 参考Netty的接口设计，如果想实现自己的重试框架，可以引入尝试一下

### retry-core

* 核心模块。是对retry-api模块的默认实现
* 拥有`Retryer`引导类，支持用优雅的 **Fluent API** 写出声明式的重试代码

### retry-annotation

* 注解实现模块
* 基于动态代理或者CGLIB字节码实现的代理重试，不依赖spring，灵活使用
* 允许自定义注解及其实现。用户可以模仿`com.poldroc.retry.annotation.annotation.Retry`，通过`@RetryAble()`包装自定义的`RetryAbleHandler`实现类来实现属于自己的重试注解

### retry-spring

* 为Spring框架集成重试功能的模块，允许开发者在Spring应用中无缝使用声明式重试功能，它通过Spring AOP集成了重试机制

* #### 主要组件

  1. **`@EnableRetry` 注解**：
     这是一个用于启用Spring重试功能的注解，类似于其他Spring功能的启用注解（如`@EnableCaching`）。它通过`@Import`将`RetryAopConfig`类注入到Spring的应用上下文中，从而启用AOP切面处理
  2. **`RetryAop` 切面类**：
     `RetryAop`类负责拦截所有标注了`@Retry`注解的方法，并在方法执行失败时执行重试逻辑。该切面通过获取方法签名、参数等信息，调用重试处理器`RetryMethodHandler`进行实际的重试操作
  3. **`RetryAopConfig` 配置类**：
     这是一个Spring配置类，通过`@ComponentScan`自动扫描并注册spring模块中所有必要的组件（如RetryAop类），确保Spring可以正确管理这些组件

### retry-springboot-stater

* 通过Spring Boot自动配置机制为项目提供开箱即用的重试功能
* 该模板引入了`retry-spring` 模块，通过自动配置机制来使用`@EnableRetry` 注解，自动启用`retry-spring`模块中定义的重试功能。



## Support

### Condition

重试触发的条件，可以指定多个条件。默认为抛出异常。

基于[RetryCondition](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/condition/RetryCondition.java)接口，配合[stop](#Stop)使用，当符合condition并且不符合stop，则开始重试。

`RetryCondition`接口定义如下：

```java
public interface RetryCondition<R> {
    boolean condition(final RetryAttempt<R> retryAttempt);
}
```

`RetryAttempt`定义如下：

```java
public interface RetryAttempt<R> {

    /**
     * 获取方法执行的结果
     * @return 方法执行的结果
     */
    R result();

    /**
     * 获取重试次数
     * @return 重试次数
     */
    int attempt();

    /**
     * 获取异常信息
     * @return 异常信息
     */
    Throwable cause();

    /**
     * 获取消耗时间
     * @return 消耗时间
     */
    AttemptTime time();

    /**
     * 获取重试历史信息
     * @return 重试历史信息
     */
    List<RetryAttempt<R>> history();

    /**
     * 获取请求参数
     * @return 请求参数
     */
    Object[] params();
}
```

`RetryCondition`基于[RetryAttempt](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/model/RetryAttempt.java)内容来自定义重试的条件，其属性包含`方法执行的结果`、`重试次数`、`异常`、`重试历史信息`、`请求参数`。所以可以通过这些属性来自定义重试条件。`RetryAttempt`每次execute重试会更新，因此每次判断是否重试的时候可以获取上次重试信息。

#### 用户自定义

用户可以继承不同的抽象类来实现自定义的重试条件判断：

1. **继承 AbstractCauseRetryCondition** 并重载 `causeCondition` 方法，通过异常信息判断是否触发重试。
2. **继承 AbstractResultRetryCondition** 并重载 `resultCondition` 方法，根据结果判断是否触发重试。
3. **继承 AbstractTimeRetryCondition** 并重载 `timeCondition` 方法，通过消耗时间判断是否触发重试。

### Stop

终止重试的条件。默认为重试次数为3，包括第一次执行。

基于[RetryStop](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/stop/RetryStop.java)接口，配合[condition](#Condition)使用，当符合condition并且不符合stop，则开始重试。

`RetryStop`定义如下：

```java
public interface RetryStop {
    boolean stop(final RetryAttempt attempt);
}
```

`RetryStop`基于[RetryAttempt](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/model/RetryAttempt.java)内容来自定义终止的条件，其属性包含`方法执行的结果`、`重试次数`、`异常`、`重试历史信息`、`请求参数`。所以可以通过这些属性来自定义终止条件。`RetryAttempt`每次execute重试会更新，因此每次判断是否终止的时候可以获取上次重试信息。

#### 用户自定义

用户可以实现`RetryStop`接口来实现自定义的终止条件判断，可以参考默认的终止策略：

```java
public class MaxAttemptRetryStop implements RetryStop {

    private final int maxAttempt;

    public MaxAttemptRetryStop(int maxAttempt) {
        if (maxAttempt <= 0) {
            throw new IllegalArgumentException("MaxAttempt must be positive");
        }
        this.maxAttempt = maxAttempt;
    }

    @Override
    public boolean stop(RetryAttempt attempt) {
        return attempt.attempt() >= maxAttempt;
    }
}
```

### Wait

重试等待策略。默认为无时间等待（不建议使用）。

基于[RetryWait](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/wait/RetryWait.java)接口，接口定义如下：

```java
public interface RetryWait{
    WaitTime waitTime(final RetryWaitContext retryWaitContext);
}
```

`RetryWaitContext`定义如下：

```java
public interface RetryWaitContext<R> extends RetryAttempt<R> {
    /**
     * 基础值（毫秒）
     * 1. fixed: 固定间隔
     * 2. 递增/指数：为初始值
     * 3. random/noRetry 这个值会被忽略
     * @return 基础值
     */
    long value();

    /**
     * 最小等待时间（毫秒）
     * @return 最小等待时间（毫秒）
     */
    long min();

    /**
     * 最大等待时间（毫秒）
     * @return 最大等待时间（毫秒）
     */
    long max();

    /**
     * 变换因子（递增/毫秒）
     * 1. 递增：每次增加的时间
     * 2. 指数：每次乘的因数
     * @return 变换因子
     */
    double factor();

    /**
     * 对应的 class 信息
     * @return class 信息
     */
    Class<? extends RetryWait> retryWait();

}
```

通过策略计算出重试之间的等待时间，配合[block](#Block)实现重试等待。

组件提供了四种等待时间计算策略：

1. `NoRetryWait`：无时间等待策略，立即重试。
2. `FixedRetryWait`：固定时间间隔等待策略。
3. `IncreaseRetryWait`：递增重试等待策略，根据重试次数，等待时间呈factor常数增长。
4. `ExponentialRetryWait`：指数增长的重试等待策略，根据重试次数，等待时间呈factor指数增长。

在源码中，`RetryWait`的使用是需要`RetryWaiter`构造器来构建重试等待时间上下文信息`RetryWaitContext`。

在`RetryWaiter`构造器中，默认等待时间`value`为1s、最小时间值`min`为0s、最大时间值`max`为30min、变化因子`factor`: 如果策略选择`ExponentialRetryWait`指数增长的重试等待策略，默认值为1.618；选择`IncreaseRetryWait`递增重试等待策略，默认值为2s。

```java
public class RetryWaiter<R> {

    /**
     * 重试等待类的类型
     */
    private Class<? extends RetryWait> retryWait = NoRetryWait.class;

    /**
     * 默认的等待时间
     */
    private long value = RetryWaitConst.DEFAULT_VALUE_MILLS;

    /**
     * 最小值
     */
    private long min = RetryWaitConst.DEFAULT_MIN_MILLS;

    /**
     * 最大值
     */
    private long max = RetryWaitConst.DEFAULT_MAX_MILLS;

    /**
     * 变化因子
     * <p>
     * 1. 如果是 {@link com.poldroc.retry.core.support.wait.ExponentialRetryWait} 则为 {@link com.poldroc.retry.core.constant.RetryWaitConst#MULTIPLY_FACTOR}
     * <p>
     * 2. 如果是 {@link com.poldroc.retry.core.support.wait.IncreaseRetryWait} 则为 {@link com.poldroc.retry.core.constant.RetryWaitConst#INCREASE_MILLS_FACTOR}
     */
    private double factor = Double.MIN_VALUE;

    /**
     * 构造器私有化
     */
    private RetryWaiter() {
    }

    /**
     * 设置重试等待的对象类型
     * 并且设置默认的因子
     * @param retryWait 重试等待类
     * @param <R>       泛型
     * @return 重试等待类
     */
    public static <R> RetryWaiter<R> retryWait(Class<? extends RetryWait> retryWait) {
        RetryWaiter<R> retryWaiter = new RetryWaiter<>();
        retryWaiter.retryWait = retryWait;
        if (IncreaseRetryWait.class.equals(retryWait)) {
            retryWaiter.factor(RetryWaitConst.INCREASE_MILLS_FACTOR);
        }
        if (ExponentialRetryWait.class.equals(retryWait)) {
            retryWaiter.factor(RetryWaitConst.MULTIPLY_FACTOR);
        }
        return retryWaiter;
    }

    public Class<? extends RetryWait> retryWait() {
        return retryWait;
    }

    public long value() {
        return value;
    }

    public RetryWaiter<R> value(long value) {
        this.value = value;
        return this;
    }

    public long min() {
        return min;
    }

    public RetryWaiter<R> min(long min) {
        this.min = min;
        return this;
    }

    public long max() {
        return max;
    }

    public RetryWaiter<R> max(long max) {
        this.max = max;
        return this;
    }

    public double factor() {
        return factor;
    }

    public RetryWaiter<R> factor(double factor) {
        this.factor = factor;
        return this;
    }

    /**
     * 构建重试等待时间上下文
     */
    public RetryWaitContext<R> context() {
        return new DefaultRetryWaitContext<R>()
                .value(value)
                .min(min)
                .max(max)
                .factor(factor)
                .retryWait(retryWait);
    }
}
```



#### 用户自定义

用户可以继承`AbstractRetryWait`抽象类来实现自定义的重试等待时间计算策略，如：

```java
/**
 * 递增重试等待策略
 */
public class IncreaseRetryWait extends AbstractRetryWait {
    @Override
    public WaitTime waitTime(RetryWaitContext retryWaitContext) {
        int previousAttempt = retryWaitContext.attempt() - 1;
        // 结果为重试等待时间的值加上重试次数减一乘以重试等待时间的因子，然后四舍五入
        long result = Math.round(retryWaitContext.value() + previousAttempt * retryWaitContext.factor());
        return super.rangeCorrect(result, retryWaitContext.min(), retryWaitContext.max());
    }
}

```



### Block

重试等待阻塞方式。默认为线程睡眠的阻塞方法。

基于[RetryBlock](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/block/RetryBlock.java)接口，定义如下：

```java
public interface RetryBlock {
    void block(final WaitTime waitTime);
}
```

#### 用户自定义

用户可以实现`RetryBlock`接口来实现自定义的阻塞策略，可以参考默认的阻塞策略：

```java
public class ThreadSleepRetryBlock implements RetryBlock {
    @Override
    public void block(WaitTime waitTime) {
        try {
            waitTime.unit().sleep(waitTime.time());
        } catch (InterruptedException e) {
            // 恢复状态
            Thread.currentThread().interrupt();
            throw new RetryException(e);
        }
    }
}
```



### Listen

指定重试的监听实现，默认为不做监听。

基于[RetryListen](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/listen/RetryListen.java)接口，接口定义如下：

```java
public interface RetryListen {
    <R> void listen(final RetryAttempt<R> attempt);
}
```

通过抽象化监听器`AbstractRetryListenInit`，使得不同的监听行为可以被灵活组合。

```java
public abstract class AbstractRetryListenInit implements RetryListen {
    @Override
    public <R> void listen(RetryAttempt<R> attempt) {
        List<RetryListen> listens = new LinkedList<>();
        this.init(listens, attempt);
        // 执行
        for (RetryListen listen : listens) {
            listen.listen(attempt);
        }
    }

    protected abstract void init(final LinkedList<RetryListen> pipeline, final RetryAttempt attempt);
}
```

* **监听器链执行**：它实现了一个通用的 `listen()` 方法，管理和执行多个监听器。

* **抽象初始化**：通过抽象方法 `init()`，将具体的监听器添加过程交给子类去实现，从而实现不同的监听器初始化逻辑。

当需要同时执行多个监听器时，比如同时记录日志和统计重试次数，可以通过 `RetryListens.listens()` 组合它们，简化代码中的监听器管理。定义如下：

```java
public class RetryListens {

    private RetryListens() {
    }

    /**
     * 不进行任何监听动作
     *
     * @return 监听器
     */
    public static RetryListen noListen() {
        return NoRetryListen.getInstance();
    }

    /**
     * 指定多个监听器
     *
     * @param retryListens 多个监听器信息
     * @return 监听器
     */
    public static RetryListen listens(final RetryListen... retryListens) {
        if (null == retryListens || retryListens.length == 0) {
            return noListen();
        }
        return new AbstractRetryListenInit() {
            @Override
            protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
                for (RetryListen retryListen : retryListens) {
                    pipeline.addLast(retryListen);
                }
            }
        };
    }
}
```

#### 用户自定义

1. 当需要执行多个监听器时，`AbstractRetryListenInit` 提供了基础的执行逻辑。开发者可以通过继承该类并实现 `init()` 方法，来实现自定义的监听器初始化和执行顺序。

   ```java
   public class CustomRetryListen extends AbstractRetryListenInit {
       @Override
       protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
           // 自定义初始化逻辑，比如根据重试次数决定是否添加某个监听器
           if (attempt.attempt() == 2) {
               pipeline.add(new LogRetryListen());
           }
           pipeline.add(new StatRetryListen());
       }
   
       private class LogRetryListen implements RetryListen {
   
           @Override
           public <R> void listen(RetryAttempt<R> attempt) {
               System.out.println("LogRetryListen: " + attempt);
           }
       }
   
       private class StatRetryListen implements RetryListen {
           @Override
           public <R> void listen(RetryAttempt<R> attempt) {
               System.out.println("StatRetryListen");
           }
       }
   }
   ```

2. 也可以实现`RetryListen`接口，通过调用 `RetryListens.listens()` 方法，将多个监听器 (`MyListen1`、`MyListen2`、`MyListen3`) 组合成一个执行链，在重试机制中执行这些监听器。

   ```java
   public class MyListens implements RetryListen {
   
       @Override
       public <R> void listen(RetryAttempt<R> attempt) {
           RetryListens.listens(new MyListen1(), new MyListen2(), new MyListen3()).listen(attempt);
       }
   }
   ```

   

### Recover

当仍然满足重试条件，但是满足重试停止条件时，则可以触发指定恢复的策略。默认不做恢复。

基于[Recover](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/recover/Recover.java)接口，接口定义如下：

```java
public interface Recover {

    /**
     * 执行恢复
     * @param retryAttempt 重试信息
     * @param <R> 泛型
     */
    <R> void recover(final RetryAttempt<R> retryAttempt);

}
```



#### 用户自定义

用户可以实现`Recover`接口来实现自定义的恢复策略，可以参考默认的恢复策略：

```java
public class MyRecover implements Recover {

    @Override
    public <R> void recover(RetryAttempt<R> retryAttempt) {
        Object[] params = retryAttempt.params();

        String name = params[0].toString();
        // 通知
        System.out.println("[Recover] " + name + "查询失败了！");
    }
}
```



