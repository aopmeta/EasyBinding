package com.aopmeta.easybinding.value;

import android.view.View;

/**
 * 赋值接口类
 */
public interface ValueBinder<V extends View, Val> {
    void setValue(V view, Val value);
}
