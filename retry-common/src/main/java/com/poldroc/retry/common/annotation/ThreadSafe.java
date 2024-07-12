package com.poldroc.retry.common.annotation;

import java.lang.annotation.*;
/**
 * 线程安全注解
 * 放在类上，标识当前类为线程安全的。
 * 放在方法上，标识方法是线程安全的。
 * <p>
 * 注意：目前此注解仅供内部使用，用来标识类是否线程安全。(表示作者的预期) 真正效果需要验证。
 * <p>
 * 后期用途：可能会直接基于 class 进行反射创建，要求有些类需要显示指定这个注解。
 * @author Poldroc
 * @date 2024/7/11
 */

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadSafe {
}
