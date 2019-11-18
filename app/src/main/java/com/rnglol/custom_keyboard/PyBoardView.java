package com.rnglol.custom_keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;

import java.util.List;

public class PyBoardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;

    public PyBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PyBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*@Override
    protected boolean onLongPress(Keyboard.Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        //} else if (key.codes[0] == 113) {
            //return true;
        } else {
            //Log.d("LatinKeyboardView", "KEY: " + key.codes[0]);
            return super.onLongPress(key);
        }
    }*/

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*Context context = getContext();
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            if (key.codes[0] == 7) {
                Log.e("KEY", "Drawing key with code " + key.codes[0]);
                Drawable dr = (Drawable) context.getResources().getDrawable(R.drawable.red_tint);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);

            } else {
                Drawable dr = (Drawable) context.getResources().getDrawable(R.drawable.blue_tint);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);
            }
        }*/
    }
}
