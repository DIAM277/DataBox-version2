package com.databox.annotation;

import java.lang.annotation.*;

// 用户操作日志注解
@Target({ElementType.METHOD})// 作用于方法
@Retention(RetentionPolicy.RUNTIME)// 运行时保留
@Documented
public @interface OpLog {

    /**
     * 操作模块(eg.回收站、文件分享)
     */
    String module() default "";

    /**
     * 操作动作(eg.移动、新增)
     */
    String action() default "";
}
