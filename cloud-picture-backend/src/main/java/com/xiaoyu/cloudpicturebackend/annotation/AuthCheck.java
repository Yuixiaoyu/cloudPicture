package com.xiaoyu.cloudpicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: AuthCheck
 * Description:
 *
 * @Author: fy
 * @create: 2024-12-12 22:27
 * @version: 1.0
 */
@Target(ElementType.METHOD) //生效范围：方法生效
@Retention(RetentionPolicy.RUNTIME)  //运行时生效
public @interface AuthCheck {

    /**
     * 必须具有某个角色
     * @return
     */
    String mustRole() default "";


}
