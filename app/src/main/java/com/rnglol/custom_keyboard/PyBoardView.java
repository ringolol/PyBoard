package com.rnglol.custom_keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;

import java.util.List;
import java.util.logging.Level;

public class PyBoardView extends KeyboardView {

    public PyBoardView(Context cont, AttributeSet attrs) {
        super(cont, attrs);
    }

    public PyBoardView(Context cont, AttributeSet attrs, int defStyle) {
        super(cont, attrs, defStyle);
    }

    @Override
    public boolean onLongPress(Keyboard.Key k) {
        String str = String.format("OnLongPress: Key -- %s == %d", (char)k.codes[0],k.codes[0]);
        Log.println(Log.INFO,"INFO", str);

        int primCode = 1;
        switch (k.codes[0])
        {
            case 'T':
                primCode = -200;
                break;
            case 'F':
                primCode = -201;
                break;
            case '(':
                primCode = -202;
                break;
            case '[':
                primCode = -203;
                break;
            case '{':
                primCode = -204;
                break;
            case '<':
                primCode = -205;
                break;
            case ':':
                primCode = -206;
                break;
            case '/':
                primCode = -207;
                break;
            case '=':
                primCode = -208;
                break;
            case '*':
                primCode = -209;
                break;
            case '-':
                primCode = -210;
                break;
            case '+':
                primCode = -211;
                break;
            case '\'':
                primCode = -212;
                break;
            case ',':
                primCode = -213;
                break;
        }

        if(primCode != 1)
        {
            getOnKeyboardActionListener().onKey(primCode, null);
            return true;
        }

        return super.onLongPress(k);
    }


}
