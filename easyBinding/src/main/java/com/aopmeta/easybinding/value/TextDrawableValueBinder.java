package com.aopmeta.easybinding.value;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

/**
 * 文本上下左右的图片赋值器
 */
public class TextDrawableValueBinder implements ValueBinder<TextView, Drawable> {
    public enum Direction {
        START, END, TOP, BOTTOM
    }

    private final Direction mDirection;

    public TextDrawableValueBinder(Direction direction) {
        mDirection = direction;
    }

    @Override
    public void setValue(TextView view, Drawable drawable) {
        Drawable[] drawables = view.getCompoundDrawables();
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        }

        switch (mDirection) {
            case START:
                view.setCompoundDrawables(drawable, drawables[1], drawables[2], drawables[3]);
                break;
            case END:
                view.setCompoundDrawables(drawables[0], drawables[1], drawable, drawables[3]);
                break;
            case TOP:
                view.setCompoundDrawables(drawables[0], drawable, drawables[2], drawables[3]);
                break;
            case BOTTOM:
                view.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawable);
                break;
        }
    }
}
