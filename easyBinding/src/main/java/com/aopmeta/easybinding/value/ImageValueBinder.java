package com.aopmeta.easybinding.value;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * 图片赋值器
 */
public class ImageValueBinder implements ValueBinder<ImageView, Drawable> {
    @Override
    public void setValue(ImageView view, Drawable value) {
        view.setImageDrawable(value);
    }
}
