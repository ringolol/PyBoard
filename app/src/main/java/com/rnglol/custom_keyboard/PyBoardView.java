package com.rnglol.custom_keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import java.util.List;

public class PyBoardView extends KeyboardView {

    public static final int SuperTextSize = 40;
    Paint super_paint;

    // init paint for superscript text
    void CreatePaint()
    {
        super_paint = new Paint();
        super_paint.setTextAlign(Paint.Align.CENTER);
        super_paint.setTextSize(SuperTextSize);
        super_paint.setColor(Color.GRAY);
    }

    public PyBoardView(Context cont, AttributeSet attrs) {
        super(cont, attrs);
        CreatePaint();
    }

    public PyBoardView(Context cont, AttributeSet attrs, int defStyle) {
        super(cont, attrs, defStyle);
        CreatePaint();
    }

    // get superscript character by original character label
    int toSecondSet(int c) {
        switch (c)
        {
            case '(':
                return ')';
            case 'T':
                return -200;
            case 'F':
                return -201;
            case '[':
                return ']';
            case '{':
                return '}';
            case '<':
                return '>';
            case ':':
                return ';';
            case '/':
                return '\\';
            case '=':
                return '#';
            case '*':
                return '!';
            case '-':
                return '_';
            case '+':
                return '@';
            case '\'':
                return '\"';
            case ',':
                return '%';
        }
        return 0;
    }

    // on long press print superscript text
    @Override
    public boolean onLongPress(Keyboard.Key k) {
        Log.println(Log.INFO,"INFO", "onLongPress");

        // get superscript character by original character label
        int ch = toSecondSet(k.codes[0]);

        if(ch != 0)
        {
            // call onKey method with superscript character
            getOnKeyboardActionListener().onKey(ch, null);
            return true;
        }

        // call super handler
        return super.onLongPress(k);
    }

    // engineerically crafted method for drawing superscript text
    void DrawSuperText(String str, Canvas canvas, Keyboard.Key key) {
        // measure superscript text width
        float str_width = super_paint.measureText(str);
        canvas.drawText(str, key.x + key.width - str_width / 2 - 10, key.y + SuperTextSize + 5, super_paint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // call super method
        super.onDraw(canvas);

        // draw superscripts
        List <Keyboard.Key> keys = getKeyboard().getKeys();
        for(Keyboard.Key key: keys) {
            // todo it might be useless check
            if (key.label != null) {
                String key_label = key.label.toString().toUpperCase();
                if(key_label.length() == 1) {
                    String super_string = "";
                    int secondset_pair = toSecondSet(key_label.charAt(0));
                    if(secondset_pair == -200) {
                        super_string = "tr";
                    } else if(secondset_pair == -201) {
                        super_string = "fl";
                    } else if(secondset_pair != 0) {
                        super_string = String.valueOf((char)secondset_pair);
                    }
                    if(!super_string.isEmpty())
                        DrawSuperText(super_string,canvas,key);
                }
            }
        }
    }
}
