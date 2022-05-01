package com.aopmeta.easybinding.base.list;

import android.view.View;
import android.view.ViewGroup;

import com.aopmeta.easybinding.base.ViewBinding;

/**
 * 绑定列表后通过这些方法回调自行绑定或设置值
 */
public abstract class OnBindAdapterCallback {
    public abstract void bind(ViewBinding binding, ViewGroup root, int viewType);
    public void setValue(ViewBinding binding, Object value) {}
}
