package com.rnglol.custom_keyboard;

import android.app.Activity;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.util.Timer;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private KeyboardView keyboardView;
    private Keyboard keyboard_shifted, keyboard_normal, keyboard_symbols;
    private boolean caps = false;

    @Override
    public View onCreateInputView() {
        // get the KeyboardView and add our Keyboard layout to it
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard_shifted = new Keyboard(this, R.xml.keyboard_shifeted);
        keyboard_shifted.setShifted(true);
        keyboard_normal = new Keyboard(this, R.xml.keyboard_normal);
        keyboard_normal.setShifted(false);
        keyboard_symbols = new Keyboard(this, R.xml.symbols_normal);
        keyboardView.setKeyboard(keyboard_normal);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        InputConnection ic = getCurrentInputConnection();

        if (ic == null) return;

        if(primaryCode == Keyboard.KEYCODE_SHIFT) {
            if (!caps) {
                keyboardView.setKeyboard(keyboard_shifted);
                caps = true;
            } else {
                keyboardView.setKeyboard(keyboard_normal);
                caps = false;
            }
            return;
        }

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                CharSequence selectedText = ic.getSelectedText(0);
                if (TextUtils.isEmpty(selectedText)) {
                    // no selection, so delete previous character
                    ic.deleteSurroundingText(1, 0);
                } else {
                    // delete the selection
                    ic.commitText("", 1);
                }
                return;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode));
                return;
            case -100:
                keyboardView.setKeyboard(keyboard_symbols);
                return;
            case -101:
                keyboardView.setKeyboard(keyboard_normal);
                caps = false;
                return;
            default:
                char code = (char) primaryCode;
                // handle CapsLock
                if(Character.isLetter(code) && !caps)
                    code = Character.toLowerCase(code);

                ic.commitText(String.valueOf(code), 1);
        }

        // Caps is used only for 1 letter
        // we might move it into switch-default
        if(caps) {
            keyboardView.setKeyboard(keyboard_normal);
            caps = false;
        }
    }

    @Override
    public void onPress(int primaryCode) { }

    @Override
    public void onRelease(int primaryCode) { }

    @Override
    public void onText(CharSequence text) { }

    @Override
    public void swipeLeft() { }

    @Override
    public void swipeRight() { }

    @Override
    public void swipeDown() { }

    @Override
    public void swipeUp() { }
}