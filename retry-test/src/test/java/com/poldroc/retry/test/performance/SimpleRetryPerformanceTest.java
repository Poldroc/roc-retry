package com.poldroc.retry.test.performance;

import com.poldroc.retry.annotation.annotation.Retry;
import com.poldroc.retry.annotation.core.RetryTemplate;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的重试性能测试
 *
 * 本测试聚焦于三个方面：
 * 1. 比较普通方法调用和启用重试注解的方法调用时间
 * 2. 测试不同失败次数的场景
 * 3. 多线程并发环境下的负载测试
 *
 * @author Claude Code
 */
public class SimpleRetryPerformanceTest {

    /**
     * 性能测量迭代次数
     */
    private static final int ITERATIONS = 1000;

    /**
     * 预热迭代次数，让JIT编译优化生效
     */
    private static final int WARMUP_ITERATIONS = 100;

    /**
     * 包含重试注解的服务类
     */
    public static class RetryService {

        /**
         * 无重试注解的普通方法
         */
        public String normalMethod() {
            return "success";
        }

        /**
         * 带有重试注解的方法（最多3次尝试）
         */
        @Retry(maxAttempt = 3)
        public String retryMethod() {
            return "success";
        }
    }

    /**
     * 专门用于失败测试的服务类
     * 每个实例有独立的失败计数器
     */
    public static class FailureTestingService {
        private final int failuresBeforeSuccess;
        private final AtomicInteger failureCounter = new AtomicInteger(0);

        public FailureTestingService(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        @Retry(maxAttempt = 5)
        public String methodWithFailures() {
            int count = failureCounter.incrementAndGet();
            if (count <= failuresBeforeSuccess) {
                throw new RuntimeException("故意失败 #" + count);
            }
            return "success after " + failuresBeforeSuccess + " failures";
        }
    }

    /**
     * 测量任务的平均执行时间
     */
    private long measureAverageTime(String testName, Runnable task, int iterations) {
        // 预热阶段
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            task.run();
        }

        // 测量阶段
        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            task.run();
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        long averageTime = totalTime / iterations;
        System.out.println(String.format("测试 [%s]: %d 次迭代, 平均时间 = %d ns (%.3f ms)",
            testName, iterations, averageTime, averageTime / 1_000_000.0));
        return averageTime;
    }

    /**
     * 测试1：比较普通方法调用和重试注解方法调用的时间
     *
     * 目的：测量重试注解代理的开销
     */
    @Test
    public void testRetryAnnotationOverhead() {
        System.out.println("\n=== 测试1：重试注解开销 ===");

        RetryService service = new RetryService();
        RetryService proxyService = RetryTemplate.getProxyObject(service);

        // 测量普通方法调用
        long normalTime = measureAverageTime("普通方法调用", () -> {
            service.normalMethod();
        }, ITERATIONS);

        // 测量重试方法调用（带有注解代理）
        long retryTime = measureAverageTime("重试方法调用", () -> {
            proxyService.retryMethod();
        }, ITERATIONS);

        // 计算开销百分比
        double overheadPercent = (retryTime - normalTime) * 100.0 / normalTime;
        System.out.println(String.format("\n重试注解开销: +%.2f%%", overheadPercent));
        System.out.println(String.format("绝对开销: %d ns 每次调用", (retryTime - normalTime)));
    }

    /**
     * 测试2：不同失败次数场景
     *
     * 目的：测试0、1、2、3、4次失败场景下重试行为对性能的影响
     */
    @Test
    public void testDifferentFailureCounts() {
        System.out.println("\n=== 测试2：不同失败次数场景 ===");

        // 测试不同的失败次数（0到4次失败后成功）
        for (int failures = 0; failures <= 4; failures++) {
            // 为每个测试场景创建新的服务实例，确保计数器从0开始
            int finalFailures = failures;
            long time = measureAverageTime(String.format("%d 次失败后成功", failures), () -> {
                try {
                    // 每次迭代创建新的服务实例和代理
                    FailureTestingService service = new FailureTestingService(finalFailures);
                    FailureTestingService proxyService = RetryTemplate.getProxyObject(service);
                    proxyService.methodWithFailures();
                } catch (Exception e) {
                    // 在正确的重试配置下不应该发生
                    // System.err.println("意外异常: " + e.getMessage());
                }
            }, ITERATIONS / 10); // 减少失败场景的迭代次数

            // 计算预期与实际尝试次数
            int expectedAttempts = failures + 1; // 失败次数 + 1次成功尝试
            System.out.println(String.format("  预期尝试次数: %d, 每次尝试平均时间: %.2f ns",
                expectedAttempts, (double)time / expectedAttempts));
        }
    }

    /**
     * 测试3：多线程负载测试
     *
     * 目的：在多线程并发环境下对重试方法进行负载测试，
     * 观察高并发下的响应时间和稳定性
     */
    @Test
    public void testMultiThreadedLoad() throws InterruptedException {
        System.out.println("\n=== 测试3：多线程负载测试 ===");

        final RetryService service = new RetryService();
        final RetryService proxyService = RetryTemplate.getProxyObject(service);

        // 测试不同的线程数量
        int[] threadCounts = {1, 5, 10, 20, 50};
        int operationsPerThread = 100;

        for (int threadCount : threadCounts) {
            System.out.println(String.format("\n--- 使用 %d 个线程测试 ---", threadCount));

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(threadCount);

            final long[] threadTimes = new long[threadCount];
            final AtomicInteger successfulOperations = new AtomicInteger(0);
            final AtomicInteger failedOperations = new AtomicInteger(0);

            // 向执行器提交任务
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        startLatch.await(); // 等待所有线程准备就绪

                        long startTime = System.nanoTime();

                        for (int j = 0; j < operationsPerThread; j++) {
                            try {
                                proxyService.retryMethod();
                                successfulOperations.incrementAndGet();
                            } catch (Exception e) {
                                failedOperations.incrementAndGet();
                            }
                        }

                        long endTime = System.nanoTime();
                        threadTimes[threadId] = endTime - startTime;

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            // 同时启动所有线程
            startLatch.countDown();

            // 等待所有线程完成（带超时）
            boolean completed = finishLatch.await(30, TimeUnit.SECONDS);

            if (!completed) {
                System.err.println("警告: " + threadCount + " 个线程测试超时");
            }

            // 关闭执行器
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // 计算统计信息
            long totalTime = 0;
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;

            for (long time : threadTimes) {
                totalTime += time;
                minTime = Math.min(minTime, time);
                maxTime = Math.max(maxTime, time);
            }

            long averageTime = totalTime / threadCount;
            long totalOperations = threadCount * operationsPerThread;
            long throughput = (totalOperations * 1_000_000_000L) / totalTime; // 操作数/秒

            System.out.println(String.format("线程数: %d, 总操作数: %d", threadCount, totalOperations));
            System.out.println(String.format("成功: %d, 失败: %d",
                successfulOperations.get(), failedOperations.get()));
            System.out.println(String.format("平均线程执行时间: %.3f ms", averageTime / 1_000_000.0));
            System.out.println(String.format("最小线程执行时间: %.3f ms", minTime / 1_000_000.0));
            System.out.println(String.format("最大线程执行时间: %.3f ms", maxTime / 1_000_000.0));
            System.out.println(String.format("吞吐量: %d 操作/秒", throughput));

            // 计算标准差以评估稳定性
            double sumSquaredDiff = 0;
            for (long time : threadTimes) {
                double diff = time - averageTime;
                sumSquaredDiff += diff * diff;
            }
            double stdDev = Math.sqrt(sumSquaredDiff / threadCount);
            System.out.println(String.format("响应时间标准差: %.3f ms (数值越低越稳定)",
                stdDev / 1_000_000.0));
        }
    }

    /**
     * 运行所有测试
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 简单重试性能测试 ==========");
        System.out.println("Java 版本: " + System.getProperty("java.version"));
        System.out.println("可用处理器: " + Runtime.getRuntime().availableProcessors());
        System.out.println("最大内存: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        System.out.println("====================================\n");

        SimpleRetryPerformanceTest test = new SimpleRetryPerformanceTest();

        try {
            test.testRetryAnnotationOverhead();
            test.testDifferentFailureCounts();
            test.testMultiThreadedLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n========== 测试完成 ==========");
    }
}
