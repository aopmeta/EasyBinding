package com.aopmeta.easybinding.value;

import android.view.View;

/**
 * 可见性赋值器
 */
public class VisibleValueBinder implements ValueBinder<View, Integer> {

    @Override
    public void setValue(View view, Integer value) {
        view.setVisibility(value);
    }
}
