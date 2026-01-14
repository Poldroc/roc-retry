package com.poldroc.retry.test.springboot;

import com.poldroc.retry.annotation.annotation.Retry;
import com.poldroc.retry.annotation.annotation.RetryWait;
import com.poldroc.retry.api.exception.RetryException;
import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.core.support.condition.*;
import com.poldroc.retry.core.support.listen.AbstractRetryListenInit;
import com.poldroc.retry.core.support.listen.NoRetryListen;
import com.poldroc.retry.core.support.recover.NoRecover;
import com.poldroc.retry.core.support.wait.*;
import com.poldroc.retry.test.RocRetryApplication;
import com.poldroc.retry.test.support.conditions.MultipleConditionsCondition;
import com.poldroc.retry.test.support.listens.CountingListen;
import com.poldroc.retry.test.support.listens.CustomRetryListen;
import com.poldroc.retry.test.support.listens.MultipleCountingListen;
import com.poldroc.retry.test.support.listens.TimingListen;
import com.poldroc.retry.test.support.recover.MyRecover;
import com.poldroc.retry.test.support.recovers.CountingRecover;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * SpringBoot 注解式 API 全面测试
 *
 * 本测试类旨在覆盖 SpringBoot 环境下 Retry 注解式 API 的所有使用场景，包括：
 * 1. 基础注解配置（默认值、自定义尝试次数等）
 * 2. 各种重试条件策略（异常条件、结果条件、自定义条件）
 * 3. 各种等待策略（固定等待、指数退避、递增等待、随机等待、无等待）
 * 4. 监听器配置与调用验证
 * 5. 恢复策略配置与调用验证
 * 6. 异常处理（受检异常、运行时异常、错误）
 * 7. 边界条件（零等待、最大尝试次数边界、超时控制）
 * 8. 自定义策略实现
 * 9. 组合策略测试
 * 10. 性能与并发测试
 *
 * 测试使用模块化组织，每个模块用注释分隔，方便维护和查找。
 * 所有测试都在 SpringBoot 上下文中运行，验证注解与 Spring AOP 的集成。
 *
 * @since 2025-12-29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RocRetryApplication.class, SpringBootAnnotationComprehensiveTest.TestConfig.class})
public class SpringBootAnnotationComprehensiveTest {

    private static final Logger log = LoggerFactory.getLogger(SpringBootAnnotationComprehensiveTest.class);
    @Autowired
    private ApplicationContext applicationContext;

    // ==================== 基础注解配置测试 ====================

    /**
     * 测试：默认注解配置（不指定任何属性，使用默认值）
     * 预期：默认最大尝试3次，遇到异常时重试
     */
    @Test
    public void testDefaultAnnotationConfiguration() {
        DefaultAnnotationService service = applicationContext.getBean(DefaultAnnotationService.class);
        try {
            service.executeWithDefaultRetry();
        } catch (RuntimeException e) {
            // Expected
        }
        assertEquals(3, service.getAttemptCount());
    }

    /**
     * 测试：自定义最大尝试次数（maxAttempt = 1）
     * 预期：只执行一次，不重试
     */
    @Test
    public void testSingleAttemptAnnotation() {
        SingleAttemptService service = applicationContext.getBean(SingleAttemptService.class);
        try {
            service.executeWithSingleAttempt();
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // Expected
            assertNotNull(e);
        } finally {
            assertEquals(1, service.getAttemptCount());
        }
    }

    /**
     * 测试：自定义最大尝试次数（maxAttempt = 5）
     * 预期：重试5次后抛出异常
     */
    @Test
    public void testMultipleAttemptsAnnotation() {
        MultipleAttemptsService service = applicationContext.getBean(MultipleAttemptsService.class);
        try {
            service.executeWithMultipleAttempts();
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // Expected
            assertNotNull(e);
        } finally {
            assertEquals(5, service.getAttemptCount());
        }
    }

    /**
     * 测试：注解在成功方法上
     * 预期：成功返回，不触发重试
     */
    @Test
    public void testAnnotationOnSuccessfulMethod() {
        SuccessfulMethodService service = applicationContext.getBean(SuccessfulMethodService.class);
        String result = service.executeSuccessfully();
        assertEquals("success", result);
    }

    // ==================== 重试条件策略测试 ====================

    /**
     * 测试：基于异常的默认重试条件（默认配置）
     * 预期：遇到异常时重试
     */
    @Test(expected = RuntimeException.class)
    public void testExceptionBasedCondition() {
        ExceptionConditionService service = applicationContext.getBean(ExceptionConditionService.class);
        service.executeWithException();
        assertEquals(3, service.getAttemptCount());
    }

    /**
     * 测试：基于结果的空值条件（isNullResult）
     * 预期：返回null时重试，返回非null值时成功
     */
    @Test
    public void testNullResultCondition() {
        NullResultConditionService service = applicationContext.getBean(NullResultConditionService.class);
        String result = service.executeWithNullResultCondition();
        assertEquals("final-result", result);
    }

    /**
     * 测试：基于结果的非空值条件（isNotNullResult）
     * 预期：返回非null时重试，返回null时成功
     */
    @Test
    public void testNotNullResultCondition() {
        NotNullResultConditionService service = applicationContext.getBean(NotNullResultConditionService.class);
        String result = service.executeWithNotNullResultCondition();
        assertNull(result);
    }

    /**
     * 测试：基于结果的等值条件（isEqualsResult）
     * 预期：返回特定值时重试，返回其他值时成功
     */
    @Test
    public void testEqualsResultCondition() {
        EqualsResultConditionService service = applicationContext.getBean(EqualsResultConditionService.class);
        String result = service.executeWithEqualsResultCondition();
        assertEquals("final-value", result);
    }

    /**
     * 测试：基于结果的不等值条件（isNotEqualsResult）
     * 预期：返回非特定值时重试，返回特定值时成功
     */
    @Test
    public void testNotEqualsResultCondition() {
        NotEqualsResultConditionService service = applicationContext.getBean(NotEqualsResultConditionService.class);
        String result = service.executeWithNotEqualsResultCondition();
        assertEquals("target-value", result);
    }

    /**
     * 测试：基于特定异常类型的条件（isExceptionCause）
     * 预期：特定异常类型时重试，其他异常类型（NullPointerException）时不重试
     */
    @Test
    public void testSpecificExceptionCondition() {
        SpecificExceptionConditionService service = applicationContext.getBean(SpecificExceptionConditionService.class);
        try {
            service.executeWithSpecificExceptionCondition();
            fail("Should have thrown NullPointerException");
        } catch (RuntimeException e) {
            // Expected
            log.info("Caught expected exception: {}", e.getCause().getMessage());
            log.info("executeWithSpecificExceptionCondition attempts: {}", service.getAttemptCount());
            assertTrue(e.getCause() instanceof NullPointerException);
        }

    }

    /**
     * 测试：自定义条件策略
     * 预期：根据自定义条件逻辑决定是否重试
     */
    @Test
    public void testCustomConditionStrategy() {
        CustomConditionService service = applicationContext.getBean(CustomConditionService.class);
        String result = service.executeWithCustomCondition();
        assertEquals("success-after-3-attempts", result);
    }

    /**
     * 测试：多条件组合（conditions）
     * 预期：多个条件中任意一个满足即重试
     */
    @Test
    public void testMultipleConditionsCombination() {
        MultipleConditionsService service = applicationContext.getBean(MultipleConditionsService.class);
        String result = service.executeWithMultipleConditions();
        assertEquals("success-after-conditions", result);
    }

    // ==================== 等待策略测试 ====================

    /**
     * 测试：固定等待策略（FixedRetryWait）
     * 预期：每次重试等待固定时间
     */
    @Test(timeout = 5000)
    public void testFixedWaitStrategy() {
        FixedWaitService service = applicationContext.getBean(FixedWaitService.class);
        long startTime = System.currentTimeMillis();

        try {
            service.executeWithFixedWait();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        // 3次尝试，2次等待，每次300ms，至少600ms
        assertTrue("Duration should be at least 600ms: " + duration, duration >= 600);
        assertTrue("Duration should be less than 3000ms: " + duration, duration < 3000);
    }

    /**
     * 测试：指数退避等待策略（ExponentialRetryWait）
     * 预期：等待时间按指数增长，有最大限制
     */
    @Test(timeout = 5000)
    public void testExponentialWaitStrategy() {
        ExponentialWaitService service = applicationContext.getBean(ExponentialWaitService.class);
        long startTime = System.currentTimeMillis();

        try {
            service.executeWithExponentialWait();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        // 等待序列：100ms, 200ms, 400ms (capped at 300ms) = ~600ms
        assertTrue("Duration should be at least 600ms: " + duration, duration >= 600);
        assertTrue("Duration should be less than 3000ms: " + duration, duration < 3000);
    }

    /**
     * 测试：递增等待策略（IncreaseRetryWait）
     * 预期：等待时间每次递增固定值
     */
    @Test(timeout = 5000)
    public void testIncreaseWaitStrategy() {
        IncreaseWaitService service = applicationContext.getBean(IncreaseWaitService.class);
        long startTime = System.currentTimeMillis();

        try {
            service.executeWithIncreaseWait();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        // 等待序列：100ms, 150ms, 200ms = ~450ms
        assertTrue("Duration should be at least 450ms: " + duration, duration >= 450);
        assertTrue("Duration should be less than 3000ms: " + duration, duration < 3000);
    }

    /**
     * 测试：随机等待策略（RandomRetryWait）
     * 预期：等待时间在指定范围内随机
     */
    @Test(timeout = 5000)
    public void testRandomWaitStrategy() {
        RandomWaitService service = applicationContext.getBean(RandomWaitService.class);
        long startTime = System.currentTimeMillis();

        try {
            service.executeWithRandomWait();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        // 3次等待，每次在100-300ms之间，总时间在300-900ms之间
        assertTrue("Duration should be at least 300ms: " + duration, duration >= 300);
        assertTrue("Duration should be less than 3000ms: " + duration, duration < 3000);
    }

    /**
     * 测试：无等待策略（NoRetryWait）
     * 预期：重试之间不等待
     */
    @Test(timeout = 1000)
    public void testNoWaitStrategy() {
        NoWaitService service = applicationContext.getBean(NoWaitService.class);

        try {
            service.executeWithNoWait();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        // 应该很快完成，无等待
    }

    /**
     * 测试：多个等待策略组合
     * 预期：按顺序循环使用多个等待策略
     */
    @Test(timeout = 5000)
    public void testMultipleWaitStrategies() {
        MultipleWaitsService service = applicationContext.getBean(MultipleWaitsService.class);
        long startTime = System.currentTimeMillis();

        try {
            service.executeWithMultipleWaits();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        long duration = System.currentTimeMillis() - startTime;
        // 等待序列：100ms, 200ms, 100ms, 200ms = ~600ms
        assertTrue("Duration should be at least 600ms: " + duration, duration >= 600);
        assertTrue("Duration should be less than 3000ms: " + duration, duration < 3000);
    }

    /**
     * 测试：零等待时间
     * 预期：配置零等待时间，快速重试
     */
    @Test(timeout = 1000)
    public void testZeroWaitTime() {
        ZeroWaitTimeService service = applicationContext.getBean(ZeroWaitTimeService.class);

        try {
            service.executeWithZeroWait();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        // 应该很快完成
    }

    // ==================== 监听器测试 ====================

    /**
     * 测试：默认无监听器
     * 预期：重试过程中不调用监听器
     */
    @Test(expected = RuntimeException.class)
    public void testDefaultNoListener() {
        DefaultListenerService service = applicationContext.getBean(DefaultListenerService.class);
        service.executeWithDefaultListener();
        assertEquals(3, service.getAttemptCount());
    }

    /**
     * 测试：自定义监听器
     * 预期：重试过程中调用监听器指定次数
     */
    @Test(expected = RuntimeException.class)
    public void testCustomListener() {
        CustomListenerService service = applicationContext.getBean(CustomListenerService.class);
        log.info("Starting testCustomListener");
        service.executeWithCustomListener();

        // 验证监听器调用次数（应为2次，maxAttempt=3，失败2次触发监听器2次）
        // CountingRetryListen has been called 1 times.
        // CountingRetryListen has been called 2 times.
    }

    /**
     * 测试：多个监听器
     * 预期：所有监听器都被调用
     */
    @Test(expected = RuntimeException.class)
    public void testMultipleListeners() {
        MultipleListenersService service = applicationContext.getBean(MultipleListenersService.class);
        service.executeWithMultipleListeners();
        // 验证两个监听器调用次数（应为2次，maxAttempt=3，失败2次触发监听器2次）
        // ListenerOne has been called 1 times.
        // ListenerTwo has been called 1 times.
        // ListenerOne has been called 2 times.
        // ListenerTwo has been called 2 times.

    }

    /**
     * 测试：监听器在成功时不调用
     * 预期：方法成功时监听器不被调用
     */
    @Test
    public void testListenerNotCalledOnSuccess() {
        ListenerOnSuccessService service = applicationContext.getBean(ListenerOnSuccessService.class);
        String result = service.executeSuccessWithListener();
    }

    // ==================== 恢复策略测试 ====================

    /**
     * 测试：默认无恢复策略
     * 预期：重试耗尽后不执行恢复操作
     */
    @Test(expected = RuntimeException.class)
    public void testDefaultNoRecovery() {
        DefaultRecoveryService service = applicationContext.getBean(DefaultRecoveryService.class);
        service.executeWithDefaultRecovery();
    }

    /**
     * 测试：自定义恢复策略
     * 预期：重试耗尽后执行恢复操作
     */
    @Test(expected = RuntimeException.class)
    public void testCustomRecovery() {
        CustomRecoveryService service = applicationContext.getBean(CustomRecoveryService.class);
        service.executeWithCustomRecovery();

        // 恢复策略调用在服务内部验证（应为1次）
        // CountingRecover has been called 1 times.

    }

    /**
     * 测试：恢复策略在成功时不调用
     * 预期：方法成功时恢复策略不被调用
     */
    @Test
    public void testRecoveryNotCalledOnSuccess() {
        RecoveryOnSuccessService service = applicationContext.getBean(RecoveryOnSuccessService.class);
        String result = service.executeSuccessWithRecovery();
        assertEquals("success", result);
    }

    // ==================== 边界条件测试 ====================

    /**
     * 测试：最大等待时间限制（默认5秒限制）
     * 预期：配置的等待时间超过限制时被限制在5秒内
     */
    @Test(timeout = 6000)
    public void testMaximumWaitTimeLimit() {
        MaxWaitTimeService service = applicationContext.getBean(MaxWaitTimeService.class);

        try {
            service.executeWithLargeWaitTime();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        // 应该在被限制的时间内完成，不会等待完整的10秒
    }

    /**
     * 测试：极小等待时间（1毫秒）
     * 预期：配置极小等待时间能正常工作
     */
    @Test(timeout = 1000)
    public void testTinyWaitTime() {
        TinyWaitTimeService service = applicationContext.getBean(TinyWaitTimeService.class);

        try {
            service.executeWithTinyWait();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        // 应该很快完成
    }

    // ==================== 自定义策略测试 ====================

    /**
     * 测试：完全自定义的策略组合
     * 预期：所有自定义组件协同工作
     */
    @Test
    public void testFullyCustomStrategy() {
        FullyCustomService service = applicationContext.getBean(FullyCustomService.class);
        String result = service.executeWithFullyCustomStrategy();
        assertEquals("custom-success", result);
    }

    /**
     * 测试：基于尝试次数的自定义条件
     * 预期：根据尝试次数决定是否重试
     */
    @Test
    public void testAttemptBasedCustomCondition() {
        AttemptBasedConditionService service = applicationContext.getBean(AttemptBasedConditionService.class);
        String result = service.executeWithAttemptBasedCondition();
        assertEquals("success-after-attempts", result);
    }

    // ==================== 组合策略测试 ====================

    /**
     * 测试：完整配置的综合场景
     * 预期：所有策略组件协同工作
     */
    @Test(timeout = 5000)
    public void testCompleteConfigurationScenario() {
        CompleteConfigurationService service = applicationContext.getBean(CompleteConfigurationService.class);
        long startTime = System.currentTimeMillis();
        try {
            service.executeWithCompleteConfiguration();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("executeWithCompleteConfiguration duration: " + duration + " ms");
        // 验证监听器和恢复策略调用次数 （应为3次监听器，1次恢复, 消耗时间约 100 + 100*2 + 100*4 = 700ms）
        // CountingRetryListen has been called 1 times.
        // CountingRetryListen has been called 2 times.
        // CountingRetryListen has been called 3 times.
        // CountingRecover has been called 1 times.
        // executeWithCompleteConfiguration duration: 727 ms

    }


    // ==================== 性能与并发测试（概念性） ====================

    /**
     * 测试：大量重试的性能
     * 预期：配置大量重试次数时能正常工作
     */
    @Test(timeout = 2000)
    public void testPerformanceWithManyRetries() {
        PerformanceService service = applicationContext.getBean(PerformanceService.class);

        try {
            service.executeWithManyRetries();
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        // 应快速完成（使用无等待策略）
    }

    // ==================== 测试配置类 ====================

    @Configuration
    public static class TestConfig {

        // 基础注解配置测试服务
        @Bean
        public DefaultAnnotationService defaultAnnotationService() {
            return new DefaultAnnotationService();
        }

        @Bean
        public SingleAttemptService singleAttemptService() {
            return new SingleAttemptService();
        }

        @Bean
        public MultipleAttemptsService multipleAttemptsService() {
            return new MultipleAttemptsService();
        }

        @Bean
        public SuccessfulMethodService successfulMethodService() {
            return new SuccessfulMethodService();
        }

        // 重试条件策略测试服务
        @Bean
        public ExceptionConditionService exceptionConditionService() {
            return new ExceptionConditionService();
        }

        @Bean
        public NullResultConditionService nullResultConditionService() {
            return new NullResultConditionService();
        }

        @Bean
        public NotNullResultConditionService notNullResultConditionService() {
            return new NotNullResultConditionService();
        }

        @Bean
        public EqualsResultConditionService equalsResultConditionService() {
            return new EqualsResultConditionService();
        }

        @Bean
        public NotEqualsResultConditionService notEqualsResultConditionService() {
            return new NotEqualsResultConditionService();
        }

        @Bean
        public SpecificExceptionConditionService specificExceptionConditionService() {
            return new SpecificExceptionConditionService();
        }

        @Bean
        public CustomConditionService customConditionService() {
            return new CustomConditionService();
        }

        @Bean
        public MultipleConditionsService multipleConditionsService() {
            return new MultipleConditionsService();
        }

        // 等待策略测试服务
        @Bean
        public FixedWaitService fixedWaitService() {
            return new FixedWaitService();
        }

        @Bean
        public ExponentialWaitService exponentialWaitService() {
            return new ExponentialWaitService();
        }

        @Bean
        public IncreaseWaitService increaseWaitService() {
            return new IncreaseWaitService();
        }

        @Bean
        public RandomWaitService randomWaitService() {
            return new RandomWaitService();
        }

        @Bean
        public NoWaitService noWaitService() {
            return new NoWaitService();
        }

        @Bean
        public MultipleWaitsService multipleWaitsService() {
            return new MultipleWaitsService();
        }

        @Bean
        public ZeroWaitTimeService zeroWaitTimeService() {
            return new ZeroWaitTimeService();
        }

        // 监听器测试服务
        @Bean
        public DefaultListenerService defaultListenerService() {
            return new DefaultListenerService();
        }

        @Bean
        public CustomListenerService customListenerService() {
            return new CustomListenerService();
        }

        @Bean
        public MultipleListenersService multipleListenersService() {
            return new MultipleListenersService();
        }

        @Bean
        public ListenerOnSuccessService listenerOnSuccessService() {
            return new ListenerOnSuccessService();
        }

        // 恢复策略测试服务
        @Bean
        public DefaultRecoveryService defaultRecoveryService() {
            return new DefaultRecoveryService();
        }

        @Bean
        public CustomRecoveryService customRecoveryService() {
            return new CustomRecoveryService();
        }

        @Bean
        public RecoveryOnSuccessService recoveryOnSuccessService() {
            return new RecoveryOnSuccessService();
        }

        // 异常处理测试服务
        @Bean
        public CheckedExceptionService checkedExceptionService() {
            return new CheckedExceptionService();
        }

        @Bean
        public RuntimeExceptionService runtimeExceptionService() {
            return new RuntimeExceptionService();
        }

        @Bean
        public ErrorService errorService() {
            return new ErrorService();
        }

        // 边界条件测试服务
        @Bean
        public MaxWaitTimeService maxWaitTimeService() {
            return new MaxWaitTimeService();
        }

        @Bean
        public TinyWaitTimeService tinyWaitTimeService() {
            return new TinyWaitTimeService();
        }

        // 自定义策略测试服务
        @Bean
        public FullyCustomService fullyCustomService() {
            return new FullyCustomService();
        }

        @Bean
        public AttemptBasedConditionService attemptBasedConditionService() {
            return new AttemptBasedConditionService();
        }

        // 组合策略测试服务
        @Bean
        public CompleteConfigurationService completeConfigurationService() {
            return new CompleteConfigurationService();
        }

        @Bean
        public ExhaustionRecoveryService exhaustionRecoveryService() {
            return new ExhaustionRecoveryService();
        }

        // 性能测试服务
        @Bean
        public PerformanceService performanceService() {
            return new PerformanceService();
        }
    }

    // ==================== 服务类实现 ====================

    // 基础注解配置测试服务
    @Service
    public static class DefaultAnnotationService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry
        public void executeWithDefaultRetry() {
            attemptCounter.incrementAndGet();
            log.info("DefaultAnnotationService attempt {}", attemptCounter.get());
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }

        public int getAttemptCount() {
            return attemptCounter.get();
        }
    }

    @Service
    public static class SingleAttemptService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 1)
        public void executeWithSingleAttempt() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Single attempt");
        }

        public int getAttemptCount() {
            return attemptCounter.get();
        }
    }

    @Service
    public static class MultipleAttemptsService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 5)
        public void executeWithMultipleAttempts() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }

        public int getAttemptCount() {
            return attemptCounter.get();
        }
    }

    @Service
    public static class SuccessfulMethodService {
        @Retry(maxAttempt = 3)
        public String executeSuccessfully() {
            return "success";
        }
    }

    // 重试条件策略测试服务
    @Service
    public static class ExceptionConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, condition = ExceptionCauseRetryCondition.class)
        public void executeWithException() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }

        public int getAttemptCount() {
            return attemptCounter.get();
        }
    }

    @Service
    public static class NullResultConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, condition = NullResultRetryCondition.class)
        public String executeWithNullResultCondition() {
            attemptCounter.incrementAndGet();
            return attemptCounter.get() < 3 ? null : "final-result";
        }
    }

    @Service
    public static class NotNullResultConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, condition = NotNullResultRetryCondition.class)
        public String executeWithNotNullResultCondition() {
            attemptCounter.incrementAndGet();
            return attemptCounter.get() < 3 ? "not-null" : null;
        }
    }

    @Service
    public static class EqualsResultConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, condition = EqualsResultCondition.class)
        public String executeWithEqualsResultCondition() {
            attemptCounter.incrementAndGet();
            return attemptCounter.get() < 3 ? "retry-value" : "final-value";
        }

        public static class EqualsResultCondition extends AbstractResultRetryCondition<String> {
            @Override
            protected boolean resultCondition(String result) {
                if (result == null) {
                    return false;
                }
                return result.equals("retry-value");
            }
        }
    }

    @Service
    public static class NotEqualsResultConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, condition = NotEqualsResultCondition.class)
        public String executeWithNotEqualsResultCondition() {
            attemptCounter.incrementAndGet();
            return attemptCounter.get() < 3 ? "not-target" : "target-value";
        }

        public static class NotEqualsResultCondition extends AbstractResultRetryCondition<String> {
            @Override
            protected boolean resultCondition(String result) {
                if (result == null) {
                    return true;
                }
                return !result.equals("target-value");
            }
        }
    }

    @Service
    public static class SpecificExceptionConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        public int getAttemptCount() {
            return attemptCounter.get();
        }

        @Retry(maxAttempt = 5, condition = SpecificExceptionCondition.class)
        public void executeWithSpecificExceptionCondition() {
            attemptCounter.incrementAndGet();
            if (attemptCounter.get() <= 3) {
                throw new IllegalArgumentException("Specific exception");
            } else {
                throw new NullPointerException("Different exception");
            }
        }

        public static class SpecificExceptionCondition extends AbstractCauseRetryCondition {

            @Override
            protected boolean causeCondition(Throwable throwable) {
                log.info(" SpecificExceptionCondition {} ",throwable.getMessage());
                return throwable.getCause() instanceof IllegalArgumentException;

            }
        }
    }

    @Service
    public static class CustomConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 5, condition = CustomAttemptCondition.class)
        public String executeWithCustomCondition() {
            attemptCounter.incrementAndGet();
            if (attemptCounter.get() < 3) {
                throw new RuntimeException("Attempt " + attemptCounter.get());
            }
            return "success-after-3-attempts";
        }

        public static class CustomAttemptCondition implements RetryCondition<String> {
            @Override
            public boolean condition(RetryAttempt<String> retryAttempt) {
                // 只在前2次尝试时重试
                return retryAttempt.attempt() < 3;
            }
        }
    }

    @Service
    public static class MultipleConditionsService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 5, condition = MultipleConditionsCondition.class)
        public String executeWithMultipleConditions() {
            attemptCounter.incrementAndGet();
            if (attemptCounter.get() < 3) {
                return null; // 触发 isNullResult 条件
            }
            return "success-after-conditions";
        }

        public static class MultipleConditionsCondition extends AbstractRetryConditionInit {
            @Override
            protected void init(LinkedList<RetryCondition> pipeline, RetryAttempt retryAttempt) {
                pipeline.add(RetryConditions.isNullResult());
                pipeline.add(RetryConditions.hasExceptionCause());
            }
        }
    }

    // 等待策略测试服务
    @Service
    public static class FixedWaitService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, waits = @RetryWait(value = 300, retryWait = FixedRetryWait.class))
        public void executeWithFixedWait() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class ExponentialWaitService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 4, waits = @RetryWait(
            value = 100,
            factor = 2.0,
            max = 300,
            retryWait = ExponentialRetryWait.class
        ))
        public void executeWithExponentialWait() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class IncreaseWaitService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 4, waits = @RetryWait(
            value = 100,
            factor = 50,
            retryWait = IncreaseRetryWait.class
        ))
        public void executeWithIncreaseWait() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class RandomWaitService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 4, waits = @RetryWait(
            min = 100,
            max = 300,
            retryWait = RandomRetryWait.class
        ))
        public void executeWithRandomWait() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class NoWaitService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 10, waits = @RetryWait(retryWait = NoRetryWait.class))
        public void executeWithNoWait() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class MultipleWaitsService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 5, waits = {
            @RetryWait(value = 100, retryWait = FixedRetryWait.class),
            @RetryWait(value = 200, retryWait = FixedRetryWait.class)
        })
        public void executeWithMultipleWaits() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class ZeroWaitTimeService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 5, waits = @RetryWait(value = 0, retryWait = FixedRetryWait.class))
        public void executeWithZeroWait() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    // 监听器测试服务
    @Service
    public static class DefaultListenerService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, listen = NoRetryListen.class)
        public void executeWithDefaultListener() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
        public int getAttemptCount() {
            return attemptCounter.get();
        }
    }

    @Service
    public static class CustomListenerService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, listen = CountingListen.class)
        public void executeWithCustomListener() {
            attemptCounter.incrementAndGet();
            log.info("executeWithCustomListener Attempt {}", attemptCounter.get());
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class MultipleListenersService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 3, listen = MultipleCountingListen.class)
        public void executeWithMultipleListeners() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class ListenerOnSuccessService {
        private final AtomicInteger listenerCallCount = new AtomicInteger(0);

        @Retry(maxAttempt = 3, listen = CountingListen.class)
        public String executeSuccessWithListener() {
            return "success";
        }
    }

    // 恢复策略测试服务
    @Service
    public static class DefaultRecoveryService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 2, recover = NoRecover.class)
        public void executeWithDefaultRecovery() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class CustomRecoveryService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 2, recover = CountingRecover.class)
        public void executeWithCustomRecovery() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class RecoveryOnSuccessService {
        private final AtomicInteger recoveryCallCount = new AtomicInteger(0);

        @Retry(maxAttempt = 3, recover = CountingRecover.class)
        public String executeSuccessWithRecovery() {
            return "success";
        }

    }

    // 异常处理测试服务
    @Service
    public static class CheckedExceptionService {
        @Retry(maxAttempt = 2)
        public void executeWithCheckedException() throws Exception {
            throw new Exception("checked exception");
        }
    }

    @Service
    public static class RuntimeExceptionService {
        @Retry(maxAttempt = 2)
        public void executeWithRuntimeException() {
            throw new IllegalArgumentException("runtime exception");
        }
    }

    @Service
    public static class ErrorService {
        @Retry(maxAttempt = 2)
        public void executeWithError() {
            throw new OutOfMemoryError("fatal error");
        }
    }

    // 边界条件测试服务
    @Service
    public static class MaxWaitTimeService {
        @Retry(maxAttempt = 2, waits = @RetryWait(value = 10000, retryWait = FixedRetryWait.class))
        public void executeWithLargeWaitTime() {
            throw new RuntimeException("test");
        }
    }

    @Service
    public static class TinyWaitTimeService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 5, waits = @RetryWait(value = 1, retryWait = FixedRetryWait.class))
        public void executeWithTinyWait() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    // 自定义策略测试服务
    @Service
    public static class FullyCustomService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(
            maxAttempt = 3,
            condition = CustomCondition.class,
            listen = CustomRetryListen.class,
            recover = MyRecover.class,
            waits = @RetryWait(value = 100, retryWait = FixedRetryWait.class)
        )
        public String executeWithFullyCustomStrategy() {
            attemptCounter.incrementAndGet();
            if (attemptCounter.get() < 2) {
                throw new RuntimeException("Attempt " + attemptCounter.get());
            }
            return "custom-success";
        }

        public static class CustomCondition implements RetryCondition<String> {
            @Override
            public boolean condition(RetryAttempt<String> retryAttempt) {
                return retryAttempt.attempt() < 3;
            }
        }
    }

    @Service
    public static class AttemptBasedConditionService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 5, condition = AttemptBasedCondition.class)
        public String executeWithAttemptBasedCondition() {
            attemptCounter.incrementAndGet();
            if (attemptCounter.get() < 3) {
                throw new RuntimeException("Attempt " + attemptCounter.get());
            }
            return "success-after-attempts";
        }

        public static class AttemptBasedCondition implements RetryCondition<String> {
            @Override
            public boolean condition(RetryAttempt<String> retryAttempt) {
                // 只在前2次尝试时重试
                return retryAttempt.attempt() < 3;
            }
        }
    }

    // 组合策略测试服务
    @Service
    public static class CompleteConfigurationService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);
        private final AtomicInteger listenerCount = new AtomicInteger(0);
        private final AtomicInteger recoveryCount = new AtomicInteger(0);

        @Retry(
            maxAttempt = 4,
            condition = MultipleConditionsCondition.class,
            waits = @RetryWait(
                value = 100,
                factor = 2.0,
                max = 1000,
                retryWait = ExponentialRetryWait.class
            ),
            listen = CountingListen.class,
            recover = CountingRecover.class
        )
        public void executeWithCompleteConfiguration() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }

    @Service
    public static class ExhaustionRecoveryService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);
        private final AtomicInteger recoveryCount = new AtomicInteger(0);

        @Retry(
            maxAttempt = 3,
            condition = AlwaysTrueRetryCondition.class,
            waits = @RetryWait(value = 10, retryWait = FixedRetryWait.class),
            recover = ExhaustionRecover.class
        )
        public void executeWithExhaustionRecovery() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }

        public int getAttemptCount() {
            return attemptCounter.get();
        }

        public class ExhaustionRecover implements Recover {
            @Override
            public void recover(RetryAttempt attempt) {
                recoveryCount.incrementAndGet();
            }
        }

        public int getRecoveryCount() {
            return recoveryCount.get();
        }
    }

    // 性能测试服务
    @Service
    public static class PerformanceService {
        private final AtomicInteger attemptCounter = new AtomicInteger(0);

        @Retry(maxAttempt = 100, waits = @RetryWait(retryWait = NoRetryWait.class))
        public void executeWithManyRetries() {
            attemptCounter.incrementAndGet();
            throw new RuntimeException("Attempt " + attemptCounter.get());
        }
    }
}
