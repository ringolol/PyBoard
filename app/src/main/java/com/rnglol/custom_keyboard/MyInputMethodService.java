package com.rnglol.custom_keyboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.VibrationEffect;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.os.Vibrator;

public class MyInputMethodService extends InputMethodService implements PyBoardView.OnKeyboardActionListener {
    private PyBoardView keyboardView;
    private Keyboard keyboard_normal, keyboard_symbols;
    private boolean caps = false;
    Vibrator vibra;

    @Override
    public View onCreateInputView() {
        keyboardView = (PyBoardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);

        keyboard_normal = new Keyboard(this, R.xml.keyboard_normal);
        keyboard_normal.setShifted(false);
        keyboard_symbols = new Keyboard(this, R.xml.symbols_normal);

        keyboardView.setKeyboard(keyboard_normal);
        keyboardView.setOnKeyboardActionListener(this);
        vibra = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        return keyboardView;
    }

    void vibrate_on_tap(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibra.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibra.vibrate(45);
        }
    }

    void CommitSnippet(InputConnection ic, String str)
    {
        String buff = "";
        int pos = 0;
        int i = 0;
        // find pointer position
        for (char ch: str.toCharArray()) {
            if(ch == '☺') {
                pos = i;
            }
            else {
                buff += ch;
                i++;
            }
        }
        ic.commitText(buff, 1);
        // move pointer
        for(int j=0; j<i-pos; j++)
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
        // get back to normal keyboard
        keyboardView.setKeyboard(keyboard_normal);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.println(Log.INFO,"INFO", "onKey");
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        if(primaryCode == Keyboard.KEYCODE_SHIFT) {
            caps = !caps;
            keyboardView.setShifted(caps);
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
                caps = false;
                keyboardView.setShifted(caps);
                keyboardView.setKeyboard(keyboard_symbols);
                return;
            case -101:
                keyboardView.setKeyboard(keyboard_normal);
                return;
            case -102:
                // smiley face shows where the pointer will be
                CommitSnippet(ic, " if ☺ else None");
                return;
            case -103:
                CommitSnippet(ic, "for x in ☺:");
                return;
            case -104:
                CommitSnippet(ic, "for i in range(☺):");
                return;
            case -105:
                CommitSnippet(ic, "for i, x in enumerate(☺):");
                return;
            case -106:
                CommitSnippet(ic, "def ☺(*args,**kwargs):");
                return;
            case -107:
                CommitSnippet(ic, "#PY Board easy input\ndef gen_in(n):\n\tfor _ in range(n):\n" +
                        "\t\tyield int(input())\n\na, b, c = gen_in(3)☺");
                return;
            case -108:
                CommitSnippet(ic, "print(☺)");
                return;
            case -200:
                ic.commitText("True", 1);
                return;
            case -201:
                ic.commitText("False", 1);
                return;
            default:
                char code = (char) primaryCode;
                // handle CapsLock (Original characters are Capitalized)
                if(Character.isLetter(code) && !caps)
                    code = Character.toLowerCase(code);

                ic.commitText(String.valueOf(code), 1);
        }

        // Caps is used only for 1 letter
        // we might move it into switch-default
        if(caps) {
            caps = false;
            keyboardView.setShifted(caps);
        }
    }

    @Override
    public void onPress(int primaryCode) {
        vibrate_on_tap();
    }

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