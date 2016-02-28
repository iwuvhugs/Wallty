package com.iwuvhugs.wallty.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import com.iwuvhugs.wallty.WalltyApplication;

@SuppressLint("NewApi")
public class TextViewCustom extends TextView {

    public TextViewCustom(Context context) {
        super(context);
        setTypeface(WalltyApplication.dinRoundProRegular);
    }

    public TextViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(WalltyApplication.dinRoundProRegular);
    }

    public TextViewCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(WalltyApplication.dinRoundProRegular);
    }

    public TextViewCustom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setTypeface(WalltyApplication.dinRoundProRegular);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused)
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused)
            super.onWindowFocusChanged(focused);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}
