package com.aopmeta.easybinding.value;

import android.widget.TextView;

/**
 * 文本赋值器
 */
public class TextValueBinder implements ValueBinder<TextView, CharSequence> {
    @Override
    public void setValue(TextView view, CharSequence value) {
        view.setText(value);
    }
}
