package com.aopmeta.easybinding.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法上添加该注解后自动生成BDR枚举类并以去除了set和get后的方法名作为枚举类型添加到BDR中
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE) //源码级别保留，编译后丢弃
public @interface Binding {
}
