package com.rnglol.custom_keyboard;

import android.content.Context;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.VibrationEffect;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.InputConnection;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.view.inputmethod.InputConnection.CURSOR_UPDATE_IMMEDIATE;

public class MyInputMethodService extends InputMethodService implements PyBoardView.OnKeyboardActionListener {
    private PyBoardView keyboardView;
    private Keyboard keyboard_normal, keyboard_symbols;
    private boolean caps = false;
    Vibrator vibra;
    LinearLayout candidates;

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

    void updCandidates() {
        InputConnection ic = getCurrentInputConnection();
        String before = ic.getTextBeforeCursor(15,0).toString();
        String after = ic.getTextAfterCursor(15,0).toString();
        ((TextView)candidates.getChildAt(0)).setText(before + "☺" + after);
        ((TextView)candidates.getChildAt(1)).setText(before + "☺" + after);
        ((TextView)candidates.getChildAt(2)).setText(before + "☺" + after);
    }

    @Override
    public View onCreateCandidatesView () {
        candidates = new LinearLayout(getApplicationContext());
        candidates.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Button btn = (Button) view;
                InputConnection ic = getCurrentInputConnection();
                ic.commitText(btn.getText(),1);
                updCandidates();
            }
        };

        int cand_text_size = 20;
        Button cand1 = new Button(getApplicationContext());
        Button cand2 = new Button(getApplicationContext());
        Button cand3 = new Button(getApplicationContext());
        cand1.setOnClickListener(listener);
        cand2.setOnClickListener(listener);
        cand3.setOnClickListener(listener);
        cand1.setTextSize(cand_text_size);
        cand2.setTextSize(cand_text_size);
        cand3.setTextSize(cand_text_size);
        int key_height = (int) getResources().getDimension(R.dimen.CandidatesHeight);
        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(0,key_height,1);
        candidates.addView(cand1,0,par);
        candidates.addView(cand2,1,par);
        candidates.addView(cand3,2,par);
        updCandidates();
        setCandidatesViewShown(true);
        return candidates;
    }

    @Override
    public void onUpdateCursorAnchorInfo(CursorAnchorInfo info) {
        Log.println(Log.INFO,"INFO", "CursorUpdate");
        super.onUpdateCursorAnchorInfo(info);
        updCandidates();
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

        switch (primaryCode) {
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboardView.setShifted(caps);
                break;
            case Keyboard.KEYCODE_DELETE:
                CharSequence selectedText = ic.getSelectedText(0);
                if (TextUtils.isEmpty(selectedText)) {
                    // no selection, so delete previous character
                    ic.deleteSurroundingText(1, 0);
                } else {
                    // delete the selection
                    ic.commitText("", 1);
                }
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, primaryCode));
                break;
            case -100:
                caps = false;
                keyboardView.setShifted(caps);
                keyboardView.setKeyboard(keyboard_symbols);
                break;
            case -101:
                keyboardView.setKeyboard(keyboard_normal);
                break;
            case -102:
                // smiley face shows where the pointer will be
                CommitSnippet(ic, " if ☺ else None");
                break;
            case -103:
                CommitSnippet(ic, "for x in ☺:");
                break;
            case -104:
                CommitSnippet(ic, "for i in range(☺):");
                break;
            case -105:
                CommitSnippet(ic, "for i, x in enumerate(☺):");
                break;
            case -106:
                CommitSnippet(ic, "def ☺(*args,**kwargs):");
                break;
            case -107:
                CommitSnippet(ic, "#PY Board easy input\ndef gen_in(n):\n\tfor _ in range(n):\n" +
                        "\t\tyield int(input())\n\na, b, c = gen_in(3)☺");
                break;
            case -108:
                CommitSnippet(ic, "print(☺)");
                break;
            case -200:
                ic.commitText("True", 1);
                break;
            case -201:
                ic.commitText("False", 1);
                break;
            default:
                char code = (char) primaryCode;
                // handle CapsLock (Original characters are Capitalized)
                if(Character.isLetter(code) && !caps)
                    code = Character.toLowerCase(code);

                ic.commitText(String.valueOf(code), 1);
                // Caps is used only for 1 letter
                if(caps) {
                    caps = false;
                    keyboardView.setShifted(caps);
                }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ic.requestCursorUpdates(CURSOR_UPDATE_IMMEDIATE);
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