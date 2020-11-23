package com.atguigu.gmall.common.cache;


import java.lang.annotation.*;

/**
 * @title: gmall
 * @Author LiuXianKun
 * @Date: 2020/11/21 11:11
 */

@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface GmallCache {
    String prefix() default "cache";
}
