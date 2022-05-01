package com.aopmeta.easybinding.base;

/**
 * 需要绑定方法的代理类，掉用get即调用实际绑定的方法
 */
public interface OnBindingCallback<T> {
    T get();
}
