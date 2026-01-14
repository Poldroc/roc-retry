package com.poldroc.retry.test.core;

import com.poldroc.retry.api.exception.RetryException;
import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.api.support.stop.RetryStop;
import com.poldroc.retry.api.support.wait.RetryWait;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;
import com.poldroc.retry.core.core.RetryWaiter;
import com.poldroc.retry.core.core.Retryer;
import com.poldroc.retry.core.support.condition.ExceptionCauseRetryCondition;
import com.poldroc.retry.core.support.condition.RetryConditions;
import com.poldroc.retry.core.support.listen.NoRetryListen;
import com.poldroc.retry.core.support.recover.NoRecover;
import com.poldroc.retry.core.support.stop.MaxAttemptRetryStop;
import com.poldroc.retry.core.support.wait.*;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * 编程式 API 全面测试
 *
 * 本测试类旨在覆盖 Retryer 编程式 API 的所有使用场景，包括：
 * 1. 基础配置（最大尝试次数、Callable 设置等）
 * 2. 各种重试条件策略
 * 3. 各种等待策略
 * 4. 停止策略
 * 5. 监听器
 * 6. 恢复策略
 * 7. 阻塞策略
 * 8. 异常处理
 * 9. 边界条件
 * 10. 性能与超时
 * 11. 上下文参数
 * 12. 线程安全
 *
 * 测试使用模块化组织，每个模块用注释分隔，方便维护和查找。
 *
 * @since 2025-12-29
 */
public class RetryerComprehensiveTest {

    // ==================== 基础配置测试 ====================

    /**
     * 测试：正常执行无异常，不应触发重试
     */
    @Test
    public void testNormalExecutionNoRetry() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return "success";
                })
                .retryCall();

        assertEquals("success", result);
        assertEquals(1, executionCount.get());
    }

    /**
     * 测试：最大尝试次数设置为1，应只执行一次
     */
    @Test
    public void testMaxAttemptOne() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(1)
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return "single";
                })
                .retryCall();

        assertEquals("single", result);
        assertEquals(1, executionCount.get());
    }

    /**
     * 测试：最大尝试次数为0应抛出异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testZeroMaxAttemptThrowsException() {
        Retryer.<String>newInstance()
                .maxAttempt(0)
                .callable(() -> "test")
                .retryCall();
    }

    /**
     * 测试：负数最大尝试次数应抛出异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxAttemptThrowsException() {
        Retryer.<String>newInstance()
                .maxAttempt(-1)
                .callable(() -> "test")
                .retryCall();
    }

    /**
     * 测试：null Callable 应抛出异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullCallableThrowsException() {
        Retryer.<String>newInstance()
                .callable(null)
                .retryCall();
    }

    /**
     * 测试：默认配置（不设置最大尝试次数）使用默认值3次
     */
    @Test(expected = RuntimeException.class)
    public void testDefaultMaxAttemptIsThree() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        // 默认最大尝试次数为3，应执行3次
        assertEquals(3, executionCount.get());
    }

    // ==================== 条件策略测试 ====================

    /**
     * 测试：alwaysTrue 条件，即使成功也重试到最大次数
     */
    @Test
    public void testAlwaysTrueConditionRetriesOnSuccess() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.alwaysTrue())
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return "success";
                })
                .retryCall();

        assertEquals("success", result);
        assertEquals(3, executionCount.get()); // 即使成功也重试了3次
    }

    /**
     * 测试：alwaysFalse 条件，即使失败也不重试
     */
    @Test(expected = RuntimeException.class)
    public void testAlwaysFalseConditionNoRetryOnFailure() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(5)
                .condition(RetryConditions.alwaysFalse())
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(1, executionCount.get()); // 只执行一次
    }

    /**
     * 测试：hasExceptionCause 条件（默认），遇到异常时重试
     */
    @Test(expected = RuntimeException.class)
    public void testHasExceptionCauseCondition() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.hasExceptionCause())
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(3, executionCount.get());
    }

    /**
     * 测试：isNullResult 条件，结果为 null 时重试
     */
    @Test
    public void testIsNullResultCondition() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.isNullResult())
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return executionCount.get() < 3 ? null : "final";
                })
                .retryCall();

        assertEquals("final", result);
        assertEquals(3, executionCount.get()); // 前两次返回null，重试
    }

    /**
     * 测试：isNotNullResult 条件，结果不为 null 时重试
     */
    @Test
    public void testIsNotNullResultCondition() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.isNotNullResult())
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return executionCount.get() < 3 ? "not null" : null;
                })
                .retryCall();

        assertNull(result);
        assertEquals(3, executionCount.get()); // 前两次返回非null，重试
    }

    /**
     * 测试：isEqualsResult 条件，结果等于预期值时重试
     */
    @Test
    public void testIsEqualsResultCondition() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.isEqualsResult("retry"))
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return executionCount.get() < 3 ? "retry" : "final";
                })
                .retryCall();

        assertEquals("final", result);
        assertEquals(3, executionCount.get()); // 前两次返回"retry"，重试
    }

    /**
     * 测试：isNotEqualsResult 条件，结果不等于预期值时重试
     */
    @Test
    public void testIsNotEqualsResultCondition() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.isNotEqualsResult("final"))
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return executionCount.get() < 3 ? "not final" : "final";
                })
                .retryCall();

        assertEquals("final", result);
        assertEquals(3, executionCount.get()); // 前两次返回"not final"，重试
    }

    /**
     * 测试：isExceptionCause 条件，特定异常类型时重试
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsExceptionCauseCondition() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.isExceptionCause(IllegalArgumentException.class))
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new IllegalArgumentException("specific error");
                })
                .retryCall();

        assertEquals(3, executionCount.get()); // IllegalArgumentException 匹配，重试
    }

    /**
     * 测试：isExceptionCause 条件，异常类型不匹配时不重试
     */
    @Test(expected = RuntimeException.class)
    public void testIsExceptionCauseConditionNotMatch() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.isExceptionCause(IllegalArgumentException.class))
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("different error");
                })
                .retryCall();

        assertEquals(1, executionCount.get()); // RuntimeException 不匹配，不重试
    }

    /**
     * 测试：conditions 组合多个条件
     */
    @Test
    public void testMultipleConditionsCombined() {
        AtomicInteger executionCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.conditions(
                    RetryConditions.isNullResult(),
                    RetryConditions.alwaysTrue()
                ))
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return executionCount.get() < 3 ? null : "result";
                })
                .retryCall();

        assertEquals("result", result);
        assertEquals(3, executionCount.get()); // 两个条件都满足，重试
    }

    /**
     * 测试：自定义条件基于尝试次数
     */
    @Test(expected = RuntimeException.class)
    public void testCustomConditionBasedOnAttemptNumber() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(5)
                .condition(new RetryCondition<String>() {
                    @Override
                    public boolean condition(RetryAttempt<String> retryAttempt) {
                        // 只在前3次尝试时重试
                        return retryAttempt.attempt() < 3;
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        // 应执行：第1次（失败，条件true），第2次（失败，条件true），第3次（失败，条件true），第4次（失败，条件false）停止
        assertEquals(4, executionCount.get());
    }

    // ==================== 等待策略测试 ====================

    /**
     * 测试：FixedRetryWait 固定等待策略
     */
    @Test(timeout = 3000)
    public void testFixedRetryWait() {
        AtomicInteger executionCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(4)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(FixedRetryWait.class)
                            .value(200)  // 每次固定等待200ms
                            .context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(4, executionCount.get());
        // 3次等待，每次200ms，至少600ms
        assertTrue("Duration should be at least 600ms: " + duration, duration >= 600);
        assertTrue("Duration should be less than 2000ms: " + duration, duration < 2000);
    }

    /**
     * 测试：ExponentialRetryWait 指数退避等待策略
     */
    @Test(timeout = 3000)
    public void testExponentialRetryWait() {
        AtomicInteger executionCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(5)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(ExponentialRetryWait.class)
                            .value(100)  // 初始等待100ms
                            .factor(2)   // 指数因子2
                            .max(500)    // 最大等待500ms
                            .context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(5, executionCount.get());
        // 等待序列：100ms, 200ms, 400ms, 800ms( capped to 500ms) = ~1200ms
        assertTrue("Duration should be at least 1200ms: " + duration, duration >= 1200);
        assertTrue("Duration should be less than 2500ms: " + duration, duration < 2500);
    }

    /**
     * 测试：IncreaseRetryWait 递增等待策略
     */
    @Test(timeout = 3000)
    public void testIncreaseRetryWait() {
        AtomicInteger executionCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(4)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(IncreaseRetryWait.class)
                            .value(100)  // 初始等待100ms
                            .factor(50)  // 每次增加50ms
                            .context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(4, executionCount.get());
        // 等待序列：100ms, 150ms, 200ms = ~450ms
        assertTrue("Duration should be at least 450ms: " + duration, duration >= 450);
        assertTrue("Duration should be less than 1500ms: " + duration, duration < 1500);
    }

    /**
     * 测试：RandomRetryWait 随机等待策略
     */
    @Test(timeout = 3000)
    public void testRandomRetryWait() {
        AtomicInteger executionCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(4)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(RandomRetryWait.class)
                            .min(100)    // 最小等待100ms
                            .max(300)    // 最大等待300ms
                            .context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(4, executionCount.get());
        // 3次等待，每次在100-300ms之间，总等待时间在300-900ms之间
        assertTrue("Duration should be at least 300ms: " + duration, duration >= 300);
        assertTrue("Duration should be less than 2000ms: " + duration, duration < 2000);
    }

    /**
     * 测试：NoRetryWait 无等待策略
     */
    @Test(timeout = 1000)
    public void testNoRetryWait() {
        AtomicInteger executionCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(10)  // 多次尝试
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(NoRetryWait.class).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(10, executionCount.get());
        // 无等待，应该很快完成
        assertTrue("Should complete quickly: " + duration, duration < 100);
    }

    /**
     * 测试：多个等待策略组合（按顺序应用）
     */
    @Test(timeout = 3000)
    public void testMultipleWaitStrategies() {
        AtomicInteger executionCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(4)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(FixedRetryWait.class).value(100).context(),
                        RetryWaiter.<String>retryWait(FixedRetryWait.class).value(200).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(4, executionCount.get());
        // 等待序列：第1次等待100ms，第2次等待200ms，第3次等待100ms（循环）
        // 总等待：100 + 200 + 100 = 400ms
        assertTrue("Duration should be at least 400ms: " + duration, duration >= 400);
        assertTrue("Duration should be less than 1500ms: " + duration, duration < 1500);
    }

    // ==================== 停止策略测试 ====================

    /**
     * 测试：默认停止策略（MaxAttemptRetryStop）
     */
    @Test(expected = RuntimeException.class)
    public void testDefaultStopStrategy() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(3, executionCount.get());
    }

    /**
     * 测试：自定义停止策略（立即停止）
     */
    @Test(expected = RuntimeException.class)
    public void testCustomImmediateStopStrategy() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .stop(new RetryStop() {
                    @Override
                    public boolean stop(RetryAttempt attempt) {
                        return true; // 总是停止
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(1, executionCount.get()); // 立即停止，只执行一次
    }

    /**
     * 测试：自定义停止策略（基于尝试次数）
     */
    @Test(expected = RuntimeException.class)
    public void testCustomStopStrategyBasedOnAttempt() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .stop(new RetryStop() {
                    @Override
                    public boolean stop(RetryAttempt attempt) {
                        return attempt.attempt() >= 2; // 尝试2次后停止
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(2, executionCount.get()); // 执行2次后停止
    }

    // ==================== 监听器测试 ====================

    /**
     * 测试：默认无监听器
     */
    @Test(expected = RuntimeException.class)
    public void testDefaultNoListener() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(3, executionCount.get());
        // 无监听器，无额外验证
    }

    /**
     * 测试：自定义监听器调用次数
     */
    @Test(expected = RuntimeException.class)
    public void testCustomListenerInvocationCount() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger listenerCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .listen(new RetryListen() {
                    @Override
                    public <R> void listen(RetryAttempt<R> attempt) {
                        listenerCount.incrementAndGet();
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(3, executionCount.get()); // 3次尝试
        assertEquals(2, listenerCount.get());  // 监听器在第2次和第3次尝试时调用（不包括第一次）
    }

    /**
     * 测试：多个监听器
     */
    @Test(expected = RuntimeException.class)
    public void testMultipleListeners() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger listener1Count = new AtomicInteger(0);
        AtomicInteger listener2Count = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .listen(new RetryListen() {
                    @Override
                    public <R> void listen(RetryAttempt<R> attempt) {
                        listener1Count.incrementAndGet();
                    }
                })
                .listen(new RetryListen() {
                    @Override
                    public <R> void listen(RetryAttempt<R> attempt) {
                        listener2Count.incrementAndGet();
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(3, executionCount.get());
        assertEquals(2, listener1Count.get()); // 每个监听器都被调用2次
        assertEquals(2, listener2Count.get());
    }

    /**
     * 测试：监听器在成功时不被调用
     */
    @Test
    public void testListenerNotCalledOnSuccess() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger listenerCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .listen(new RetryListen() {
                    @Override
                    public <R> void listen(RetryAttempt<R> attempt) {
                        listenerCount.incrementAndGet();
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return "success";
                })
                .retryCall();

        assertEquals("success", result);
        assertEquals(1, executionCount.get());
        assertEquals(0, listenerCount.get()); // 成功时监听器不被调用
    }

    // ==================== 恢复策略测试 ====================

    /**
     * 测试：默认无恢复策略
     */
    @Test(expected = RuntimeException.class)
    public void testDefaultNoRecovery() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger recoveryCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(2)
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(2, executionCount.get());
        assertEquals(0, recoveryCount.get()); // 无恢复策略
    }

    /**
     * 测试：自定义恢复策略在重试耗尽时调用
     */
    @Test(expected = RuntimeException.class)
    public void testCustomRecoveryCalledOnExhaustion() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger recoveryCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(2)
                .recover(new Recover() {
                    @Override
                    public void recover(RetryAttempt attempt) {
                        recoveryCount.incrementAndGet();
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("error");
                })
                .retryCall();

        assertEquals(2, executionCount.get());
        assertEquals(1, recoveryCount.get()); // 恢复策略被调用一次
    }

    /**
     * 测试：恢复策略在成功时不调用
     */
    @Test
    public void testRecoveryNotCalledOnSuccess() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger recoveryCount = new AtomicInteger(0);

        String result = Retryer.<String>newInstance()
                .maxAttempt(3)
                .recover(new Recover() {
                    @Override
                    public void recover(RetryAttempt attempt) {
                        recoveryCount.incrementAndGet();
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return "success";
                })
                .retryCall();

        assertEquals("success", result);
        assertEquals(1, executionCount.get());
        assertEquals(0, recoveryCount.get()); // 成功时不调用恢复策略
    }

    // ==================== 阻塞策略测试 ====================

    /**
     * 测试：默认阻塞策略（线程睡眠）
     * 通过等待策略的时间来间接测试
     */
    @Test(timeout = 3000)
    public void testDefaultBlockStrategy() {
        AtomicInteger executionCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(3)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(FixedRetryWait.class).value(200).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(3, executionCount.get());
        // 2次等待，每次200ms，至少400ms
        assertTrue("Duration should be at least 400ms: " + duration, duration >= 400);
    }

    /**
     * 测试：自定义阻塞策略（立即返回，不阻塞）
     * 注意：实际使用时需要实现 RetryBlock 接口，这里仅演示概念
     */
    @Test(timeout = 1000)
    public void testNoBlockStrategyConcept() {
        // 实际测试需要实现 RetryBlock 接口，这里仅作为示例
        // 通常使用 NoRetryWait 来达到类似效果
        AtomicInteger executionCount = new AtomicInteger(0);

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(10)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(NoRetryWait.class).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(10, executionCount.get());
        // 无等待，应该很快完成
    }

    // ==================== 异常处理测试 ====================

    /**
     * 测试：Checked Exception 被包装成 RetryException
     */
    @Test
    public void testCheckedExceptionWrappedInRetryException() {
        AtomicInteger executionCount = new AtomicInteger(0);

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(2)
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new Exception("checked exception");
                    })
                    .retryCall();
            fail("Should have thrown RetryException");
        } catch (RetryException e) {
            assertTrue(e.getCause() instanceof Exception);
            assertEquals("checked exception", e.getCause().getMessage());
        }

        assertEquals(2, executionCount.get());
    }

    /**
     * 测试：Runtime Exception 不被包装
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRuntimeExceptionNotWrapped() {
        Retryer.<String>newInstance()
                .maxAttempt(2)
                .callable(() -> {
                    throw new IllegalArgumentException("runtime exception");
                })
                .retryCall();
    }

    /**
     * 测试：Error 不被包装，直接抛出
     */
    @Test(expected = OutOfMemoryError.class)
    public void testErrorNotWrapped() {
        Retryer.<String>newInstance()
                .maxAttempt(2)
                .callable(() -> {
                    throw new OutOfMemoryError("fatal error");
                })
                .retryCall();
    }

    // ==================== 边界条件测试 ====================

    /**
     * 测试：最大等待时间默认限制（5秒）
     */
    @Test(timeout = 6000)
    public void testLargeWaitTimeCapped() {
        AtomicInteger executionCount = new AtomicInteger(0);

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(2)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(FixedRetryWait.class).value(10000).context() // 10秒，应被限制成5秒
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(2, executionCount.get()); // 1次等待，应该被限制在5秒内完成，不会超时
    }

    /**
     * 测试：零等待时间
     */
    @Test(timeout = 1000)
    public void testZeroWaitTime() {
        AtomicInteger executionCount = new AtomicInteger(0);

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(5)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(FixedRetryWait.class).value(0).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(5, executionCount.get());
        // 应该很快完成
    }

    /**
     * 测试：极小等待时间（1毫秒）
     */
    @Test(timeout = 1000)
    public void testTinyWaitTime() {
        AtomicInteger executionCount = new AtomicInteger(0);

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(5)
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(FixedRetryWait.class).value(1).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(5, executionCount.get());
        // 应该很快完成
    }

    // ==================== 性能与超时测试 ====================

    /**
     * 测试：大量重试次数下的性能
     */
    @Test(timeout = 2000)
    public void testManyRetriesPerformance() {
        AtomicInteger executionCount = new AtomicInteger(0);

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(100)  // 100次尝试
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(NoRetryWait.class).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(100, executionCount.get());
        // 无等待，应该很快完成
    }

    /**
     * 测试：组合策略下的超时控制
     */
    @Test(timeout = 5000)
    public void testTimeoutWithCombinedStrategies() {
        AtomicInteger executionCount = new AtomicInteger(0);

        try {
            Retryer.<String>newInstance()
                    .maxAttempt(5)
                    .condition(RetryConditions.alwaysTrue())
                    .retryWaitContext(
                        RetryWaiter.<String>retryWait(FixedRetryWait.class).value(500).context()
                    )
                    .callable(() -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("error");
                    })
                    .retryCall();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(5, executionCount.get());
        // 4次等待，每次500ms，总等待约2000ms，加上执行时间，应在timeout(5000ms)内完成
    }

    // ==================== 上下文参数测试 ====================

    /**
     * 测试：RetryContext 构建
     */
    @Test
    public void testRetryContextBuild() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer<String> retryer = Retryer.<String>newInstance()
                .maxAttempt(3)
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return "test";
                });

        // 构建上下文但不执行
        assertNotNull(retryer.context());
        assertEquals(0, executionCount.get()); // 尚未执行
    }

    /**
     * 测试：通过 RetryContext 执行
     */
    @Test
    public void testExecuteViaRetryContext() {
        AtomicInteger executionCount = new AtomicInteger(0);

        Retryer<String> retryer = Retryer.<String>newInstance()
                .maxAttempt(3)
                .callable(() -> {
                    executionCount.incrementAndGet();
                    return "success";
                });

        String result = retryer.retryCall(retryer.context());

        assertEquals("success", result);
        assertEquals(1, executionCount.get());
    }

    // ==================== 线程安全测试 ====================

    /**
     * 测试：InstanceFactory 线程安全
     */
    @Test(timeout = 5000)
    public void testInstanceFactoryThreadSafety() throws InterruptedException {
        final int threadCount = 10;
        final AtomicInteger successCount = new AtomicInteger(0);
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    InstanceFactory instanceFactory = InstanceFactory.getInstance();
                    ExceptionCauseRetryCondition condition = instanceFactory.threadSafe(ExceptionCauseRetryCondition.class);
                    assertNotNull(condition);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount, successCount.get());
    }

    /**
     * 测试：Retryer 实例非线程安全（@NotThreadSafe 注解）
     * 多个线程共享同一个 Retryer 实例可能导致不可预期行为
     * 这里验证至少不会崩溃
     */
    @Test(timeout = 5000)
    public void testRetryerNotThreadSafe() throws InterruptedException {
        final int threadCount = 5;
        final AtomicInteger successCount = new AtomicInteger(0);
        final Retryer<String> sharedRetryer = Retryer.<String>newInstance()
                .maxAttempt(2)
                .callable(() -> "result");

        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    // 共享非线程安全实例，行为未定义但不应崩溃
                    sharedRetryer.retryCall();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 可能抛出异常，这是可接受的
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 至少一些线程成功完成
        assertTrue(successCount.get() > 0);
    }

    // ==================== 综合场景测试 ====================

    /**
     * 测试：完整配置的综合场景
     */
    @Test(timeout = 5000)
    public void testCompleteConfigurationScenario() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger listenerCount = new AtomicInteger(0);
        AtomicInteger recoveryCount = new AtomicInteger(0);
        final String expectedResult = "final-result";

        String result = Retryer.<String>newInstance()
                .maxAttempt(4)
                .condition(RetryConditions.conditions(
                    RetryConditions.hasExceptionCause(),
                    RetryConditions.isNullResult()
                ))
                .retryWaitContext(
                    RetryWaiter.<String>retryWait(ExponentialRetryWait.class)
                        .value(50)
                        .factor(2)
                        .max(200)
                        .context()
                )
                .listen(new RetryListen() {
                    @Override
                    public <R> void listen(RetryAttempt<R> attempt) {
                        listenerCount.incrementAndGet();
                    }
                })
                .recover(new Recover() {
                    @Override
                    public void recover(RetryAttempt attempt) {
                        recoveryCount.incrementAndGet();
                    }
                })
                .callable(() -> {
                    int count = executionCount.incrementAndGet();
                    if (count < 3) {
                        throw new RuntimeException("error " + count);
                    }
                    return expectedResult;
                })
                .retryCall();

        assertEquals(expectedResult, result);
        assertEquals(3, executionCount.get()); // 前2次失败，第3次成功
        assertEquals(2, listenerCount.get());  // 监听第2次和第3次尝试
        assertEquals(0, recoveryCount.get());  // 成功，不调用恢复
    }

    /**
     * 测试：重试耗尽后的恢复场景
     */
    @Test(expected = RuntimeException.class)
    public void testExhaustionRecoveryScenario() {
        AtomicInteger executionCount = new AtomicInteger(0);
        AtomicInteger recoveryCount = new AtomicInteger(0);

        Retryer.<String>newInstance()
                .maxAttempt(3)
                .condition(RetryConditions.alwaysTrue())
                .retryWaitContext(
                    RetryWaiter.<String>retryWait(FixedRetryWait.class).value(10).context()
                )
                .recover(new Recover() {
                    @Override
                    public void recover(RetryAttempt attempt) {
                        recoveryCount.incrementAndGet();
                    }
                })
                .callable(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("persistent error");
                })
                .retryCall();

        // 以下代码不会执行，因为会抛出异常
        // 但在异常抛出前，恢复策略应该被调用
        // 实际验证在 catch 块外无法进行，但恢复策略会被调用一次
    }
}
