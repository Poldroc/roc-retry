# roc-retry
[中文](./README-zh.md)

roc-retry is a Java retry framework that supports both procedural programming and annotation-based programming.

## **Feature Description**

* Uses the **Builder pattern**, supporting an elegant **Fluent API** programming style
* Based on CGLIB bytecode proxy retry
* Annotation-based retry mechanism, allowing users to customize annotations
* Provides **multiple support strategies**, including blocking, listening, recovery, waiting, and stopping strategies
* Adopts Netty-like interface **API design** philosophy, ensuring interface consistency and flexibility in replacement
* Seamless integration with Spring/Spring-Boot





![image-20260114234156954](https://engroc1.oss-cn-shenzhen.aliyuncs.com/Typora/202601142341133.png)

## Quick Start

### Introduction

```xml
<dependency>
    <groupId>io.github.poldroc</groupId>
    <artifactId>retry-core</artifactId>
    <version>1.1</version>
</dependency>
```



### Simple Entry

```java
public class RetryerTest {
    /**
     * Retry on default exception
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
     * Default configuration test
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



## Annotation Usage

### Introduction

```xml
<dependency>
    <groupId>io.github.poldroc</groupId>
    <artifactId>retry-annotation</artifactId>
    <version>1.1</version>
</dependency>
```

[Usage Details](https://github.com/Poldroc/roc-retry/blob/master/retry-test/src/test/java/com/poldroc/retry/test/springboot/RocRetryApplicationTest.java)

### Retry

Used to specify retry-related configurations

```java
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RetryAble(DefaultRetryAbleHandler.class)
public @interface Retry {

    /**
     * Retry class implementation
     *
     * @return Retry
     */
    Class<? extends com.poldroc.retry.api.core.Retry> retry() default DefaultRetry.class;

    /**
     * Maximum number of attempts
     * 1. Default is 3 including the first normal execution of the method
     *
     * @return Number of attempts
     */
    int maxAttempt() default 3;

    /**
     * Scenario that triggers a retry
     * 1. Default is triggered by exception
     *
     * @return Scenario that triggers a retry
     */
    Class<? extends RetryCondition> condition() default ExceptionCauseRetryCondition.class;

    /**
     * Listener
     * 1. Default does not perform any listening
     *
     * @return Listener
     */
    Class<? extends RetryListen> listen() default NoRetryListen.class;

    /**
     * Recovery operation
     * 1. Default does not perform any recovery operation
     *
     * @return Class corresponding to the recovery operation
     */
    Class<? extends Recover> recover() default NoRecover.class;

    /**
     * Retry wait strategy
     * 1. Supports specifying multiple, if not specified, no waiting is performed,
     *
     * @return Retry wait strategy
     */
    RetryWait[] waits() default {};
}

```



### RetryWait

Used to specify the retry wait strategy

```java
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@RetryWaitAble(DefaultRetryWaitAbleHandler.class)
public @interface RetryWait {

    /**
     * Default value
     * 1. fixed mode, corresponds to a fixed wait time
     * 2. Incremental
     * @return Default value
     */
    long value() default RetryWaitConst.DEFAULT_VALUE_MILLS;

    /**
     * Minimum value
     * @return Minimum value
     */
    long min() default RetryWaitConst.DEFAULT_MIN_MILLS;

    /**
     * Maximum value
     * @return Maximum value
     */
    long max() default RetryWaitConst.DEFAULT_MAX_MILLS;

    /**
     * Influencing factor
     * 1. Incremental retry, default is {@link RetryWaitConst#INCREASE_MILLS_FACTOR}
     * 2. Exponential mode. Default is {@link RetryWaitConst#MULTIPLY_FACTOR}
     * @return Influencing factor
     */
    double factor() default Double.MIN_VALUE;

    /**
     * Specifies the class information for the retry wait time
     * @return Retry wait time class
     */
    Class<? extends com.poldroc.retry.api.support.wait.RetryWait> retryWait() default NoRetryWait.class;

}

```



### Annotation Parsing Process



<img src="https://engroc.oss-cn-fuzhou.aliyuncs.com/Typora/202410170130335.png" width="50%" />

## Module Introduction

### retry-api

* Interface definition module

* Refers to Netty's interface design, if you want to implement your own retry framework, you can try to introduce it

### retry-core

* Core module. It is the default implementation of the retry-api module
* Has the `Retryer` bootstrap class, which supports writing declarative retry code with an elegant **Fluent API**

### retry-annotation

* Annotation implementation module
* Proxy retry based on dynamic proxy or CGLIB bytecode, not dependent on spring, flexible use
* Allows customization of annotations and their implementations. Users can imitate `com.poldroc.retry.annotation.annotation.Retry`, by `@RetryAble()` wrapping custom `RetryAbleHandler` implementation classes to achieve their own retry annotations

### retry-spring

* Module that integrates retry functionality into the Spring framework, allowing developers to seamlessly use declarative retry functionality in Spring applications, it integrates the retry mechanism through Spring AOP

* #### Main Components

  1. **`@EnableRetry` Annotation**:
     This is an annotation used to enable Spring retry functionality, similar to other Spring functionality enabling annotations (such as `@EnableCaching`). It injects the `RetryAopConfig` class into the Spring application context through `@Import`, thereby enabling AOP aspect processing
  2. **`RetryAop` Aspect Class**:
     The `RetryAop` class is responsible for intercepting all methods marked with the `@Retry` annotation and executing the retry logic when the method execution fails. This aspect obtains method signatures, parameters, and other information, and calls the retry processor `RetryMethodHandler` to perform the actual retry operation
  3. **`RetryAopConfig` Configuration Class**:
     This is a Spring configuration class that automatically scans and registers all necessary components in the spring module (such as the RetryAop class) through `@ComponentScan`, ensuring that Spring can correctly manage these components

### retry-springboot-stater

* Provides out-of-the-box retry functionality for projects through the Spring Boot auto-configuration mechanism
* This template introduces the `retry-spring` module, which uses the auto-configuration mechanism to use the `@EnableRetry` annotation and automatically enables the retry functionality defined in the `retry-spring` module.



## Support

### Condition

The condition for triggering a retry can specify multiple conditions. Default is to throw an exception.

Based on the [RetryCondition](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/condition/RetryCondition.java) interface, used in conjunction with [stop](#Stop), when it meets the condition and does not meet the stop, it starts to retry.

The `RetryCondition` interface is defined as follows:

```java
public interface RetryCondition<R> {
    boolean condition(final RetryAttempt<R> retryAttempt);
}
```

`RetryAttempt` is defined as follows:

```java
public interface RetryAttempt<R> {

    /**
     * Get the result of the method execution
     * @return The result of the method execution
     */
    R result();

    /**
     * Get the number of retries
     * @return The number of retries
     */
    int attempt();

    /**
     * Get the exception information
     * @return The exception information
     */
    Throwable cause();

    /**
     * Get the time consumed
     * @return The time consumed
     */
    AttemptTime time();

    /**
     * Get the retry history information
     * @return The retry history information
     */
    List<RetryAttempt<R>> history();

    /**
     * Get the request parameters
     * @return The request parameters
     */
    Object[] params();
}
```

`RetryCondition` is based on [RetryAttempt](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/model/RetryAttempt.java) content to customize the retry condition, its properties include `method execution result`, `number of retries`, `exception`, `retry history information`, `request parameters`. So you can customize the retry condition through these properties. `RetryAttempt` is updated every time execute retries, so you can get the last retry information every time you decide whether to retry.

#### Customization by Users

Users can inherit different abstract classes to implement custom retry condition judgments:

1. **Inherit AbstractCauseRetryCondition** and override the `causeCondition` method to judge whether to trigger a retry through exception information.
2. **Inherit AbstractResultRetryCondition** and override the `resultCondition` method to judge whether to trigger a retry based on the result.
3. **Inherit AbstractTimeRetryCondition** and override the `timeCondition` method to judge whether to trigger a retry through the time consumed.

### Stop

The condition for terminating a retry. Default is 3 retry attempts, including the first execution.

Based on the [RetryStop](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/stop/RetryStop.java) interface, used in conjunction with [condition](#Condition), when it meets the condition and does not meet the stop, it starts to retry.

The `RetryStop` is defined as follows:

```java
public interface RetryStop {
    boolean stop(final RetryAttempt attempt);
}
```

`RetryStop` is based on [RetryAttempt](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/model/RetryAttempt.java) content to customize the termination condition, its properties include `method execution result`, `number of retries`, `exception`, `retry history information`, `request parameters`. So you can customize the termination condition through these properties. `RetryAttempt` is updated every time execute retries, so you can get the last retry information every time you decide whether to terminate.

#### Customization by Users

Users can implement the `RetryStop` interface to implement custom termination condition judgments, you can refer to the default termination strategy:

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

Retry wait strategy. Default is no waiting time (not recommended).

Based on the [RetryWait](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/wait/RetryWait.java) interface, the interface is defined as follows:

```java
public interface RetryWait{
    WaitTime waitTime(final RetryWaitContext retryWaitContext);
}
```

`RetryWaitContext` is defined as follows:

```java
public interface RetryWaitContext<R> extends RetryAttempt<R> {
    /**
     * Base value (milliseconds)
     * 1. fixed: fixed interval
     * 2. Incremental/Exponential: as the initial value
     * 3. random/noRetry this value will be ignored
     * @return Base value
     */
    long value();

    /**
     * Minimum wait time (milliseconds)
     * @return Minimum wait time (milliseconds)
     */
    long min();

    /**
     * Maximum wait time (milliseconds)
     * @return Maximum wait time (milliseconds)
     */
    long max();

    /**
     * Transformation factor (Incremental/Milliseconds)
     * 1. Incremental: the time increased each time
     * 2. Exponential: the factor multiplied each time
     * @return Transformation factor
     */
    double factor();

    /**
     * Corresponding class information
     * @return Class information
     */
    Class<? extends RetryWait> retryWait();

}
```

Calculate the wait time between retries through the strategy, and implement retry waiting with [block](#Block).

The component provides four wait time calculation strategies:

1. `NoRetryWait`: No waiting time strategy, retry immediately.
2. `FixedRetryWait`: Fixed time interval waiting strategy.
3. `IncreaseRetryWait`: Incremental retry waiting strategy, according to the number of retries, the wait time increases by a constant factor.
4. `ExponentialRetryWait`: Exponential growth retry waiting strategy, according to the number of retries, the wait time grows exponentially by a factor.

In the source code, the use of `RetryWait` requires the `RetryWaiter` constructor to build the retry wait time context information `RetryWaitContext`.

In the `RetryWaiter` constructor, the default wait time `value` is 1s, the minimum time value `min` is 0s, the maximum time value `max` is 30min, and the change factor `factor`: if the strategy chooses `ExponentialRetryWait` exponential growth retry waiting strategy, the default value is 1.618; if `IncreaseRetryWait` incremental retry waiting strategy is chosen, the default value is 2s.

```java
public class RetryWaiter<R> {

    /**
     * Type of retry wait class
     */
    private Class<? extends RetryWait> retryWait = NoRetryWait.class;

    /**
     * Default wait time
     */
    private long value = RetryWaitConst.DEFAULT_VALUE_MILLS;

    /**
     * Minimum value
     */
    private long min = RetryWaitConst.DEFAULT_MIN_MILLS;

    /**
     * Maximum value
     */
    private long max = RetryWaitConst.DEFAULT_MAX_MILLS;

    /**
     * Change factor
     * <p>
     * 1. If it is {@link com.poldroc.retry.core.support.wait.ExponentialRetryWait} then it is {@link com.poldroc.retry.core.constant.RetryWaitConst#MULTIPLY_FACTOR}
     * <p>
     * 2. If it is {@link com.poldroc.retry.core.support.wait.IncreaseRetryWait} then it is {@link com.poldroc.retry.core.constant.RetryWaitConst#INCREASE_MILLS_FACTOR}
     */
    private double factor = Double.MIN_VALUE;

    /**
     * Private constructor
     */
    private RetryWaiter() {
    }

    /**
     * Set the type of retry wait object
     * And set the default factor
     * @param retryWait Retry wait class
     * @param <R>       Generic
     * @return Retry wait class
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
     * Build retry wait time context
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



#### Customization by Users

Users can inherit the `AbstractRetryWait` abstract class to implement custom retry wait time calculation strategies, such as:

```java
/**
 * Incremental retry wait strategy
 */
public class IncreaseRetryWait extends AbstractRetryWait {
    @Override
    public WaitTime waitTime(RetryWaitContext retryWaitContext) {
        int previousAttempt = retryWaitContext.attempt() - 1;
        // The result is the value of the retry wait time plus the retry attempt minus one multiplied by the retry wait time factor, then rounded
        long result = Math.round(retryWaitContext.value() + previousAttempt * retryWaitContext.factor());
        return super.rangeCorrect(result, retryWaitContext.min(), retryWaitContext.max());
    }
}

```



### Block

The way to block retry waiting. Default is thread sleep blocking method.

Based on the [RetryBlock](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/block/RetryBlock.java) interface, defined as follows:

```java
public interface RetryBlock {
    void block(final WaitTime waitTime);
}
```

#### Customization by Users

Users can implement the `RetryBlock` interface to implement custom blocking strategies, you can refer to the default blocking strategy:

```java
public class ThreadSleepRetryBlock implements RetryBlock {
    @Override
    public void block(WaitTime waitTime) {
        try {
            waitTime.unit().sleep(waitTime.time());
        } catch (InterruptedException e) {
            // Restore status
            Thread.currentThread().interrupt();
            throw new RetryException(e);
        }
    }
}
```



### Listen

Specify the implementation of the retry listener, default is no listening.

Based on the [RetryListen](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/listen/RetryListen.java) interface, the interface is defined as follows:

```java
public interface RetryListen {
    <R> void listen(final RetryAttempt<R> attempt);
}
```

Through the abstract listener `AbstractRetryListenInit`, different listening behaviors can be flexibly combined.

```java
public abstract class AbstractRetryListenInit implements RetryListen {
    @Override
    public <R> void listen(RetryAttempt<R> attempt) {
        List<RetryListen> listens = new LinkedList<>();
        this.init(listens, attempt);
        // Execute
        for (RetryListen listen : listens) {
            listen.listen(attempt);
        }
    }

    protected abstract void init(final LinkedList<RetryListen> pipeline, final RetryAttempt attempt);
}
```

* **Listener chain execution**: It implements a general `listen()` method, managing and executing multiple listeners.

* **Abstract initialization**: By abstract method `init()`, the specific listener addition process is handed over to the subclass to implement, thereby achieving different listener initialization logic.

When multiple listeners need to be executed at the same time, such as recording logs and counting retry attempts at the same time, you can combine them through `RetryListens.listens()`, simplifying the listener management in the code. It is defined as follows:

```java
public class RetryListens {

    private RetryListens() {
    }

    /**
     * Do not perform any listening actions
     *
     * @return Listener
     */
    public static RetryListen noListen() {
        return NoRetryListen.getInstance();
    }

    /**
     * Specify multiple listeners
     *
     * @param retryListens Multiple listener information
     * @return Listener
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

#### Customization by Users

1. When multiple listeners need to be executed, `AbstractRetryListenInit` provides the basic execution logic. Developers can inherit this class and implement the `init()` method to achieve custom listener initialization and execution order.

   ```java
   public class CustomRetryListen extends AbstractRetryListenInit {
       @Override
       protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
           // Custom initialization logic, such as deciding whether to add a certain listener based on the number of retries
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

2. You can also implement the `RetryListen` interface, and by calling the `RetryListens.listens()` method, combine multiple listeners (`MyListen1`, `MyListen2`, `MyListen3`) into an execution chain, and execute these listeners in the retry mechanism.

   ```java
   public class MyListens implements RetryListen {
   
       @Override
       public <R> void listen(RetryAttempt<R> attempt) {
           RetryListens.listens(new MyListen1(), new MyListen2(), new MyListen3()).listen(attempt);
       }
   }
   ```

   

### Recover

When the retry conditions are still met, but the retry stop conditions are met, a specified recovery strategy can be triggered. Default is no recovery.

Based on the [Recover](https://github.com/Poldroc/roc-retry/blob/master/retry-api/src/main/java/com/poldroc/retry/api/support/recover/Recover.java) interface, the interface is defined as follows:

```java
public interface Recover {

    /**
     * Execute recovery
     * @param retryAttempt Retry information
     * @param <R> Generic
     */
    <R> void recover(final RetryAttempt<R> retryAttempt);

}
```



#### Customization by Users

Users can implement the `Recover` interface to implement custom recovery strategies, you can refer to the default recovery strategy:

```java
public class MyRecover implements Recover {

    @Override
    public <R> void recover(RetryAttempt<R> retryAttempt) {
        Object[] params = retryAttempt.params();

        String name = params[0].toString();
        // Notification
        System.out.println("[Recover] " + name + " query failed!");
    }
}
```
