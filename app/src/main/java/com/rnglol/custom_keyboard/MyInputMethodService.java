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
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.InputConnection;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.*;
import java.util.Vector;

import static android.view.inputmethod.InputConnection.CURSOR_UPDATE_IMMEDIATE;

public class MyInputMethodService extends InputMethodService implements PyBoardView.OnKeyboardActionListener {
    private PyBoardView keyboardView;
    private Keyboard keyboard_normal, keyboard_symbols;
    private boolean caps = false;
    Vibrator vibra;
    LinearLayout candidates;

    public static final int TAKECHAR_NUM = 15;
    public static final int CAND_TEXT_SIZE = 17;
    public static final int VIB_DURATION = 45;
    public static final String[] dictionary = new String[]{
            // Comparisons
            "<", "<=", ">", ">=", "==", "!=", "is", "is not", "in", "not", "==", ":=",
            // Types
            "int", "float", "complex", "list", "tuple", "range", "str", "set", "frozenset", "dict",
            // Additional types
            "Fraction", "Decimal", "bytes", "bytearray", "memoryview",
            // Keywords and constants
            "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class",
            "continue", "def", "del", "elif", "else", "except", "finally", "for", "from", "global",
            "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return",
            "try", "while", "with", "yield", "NotImplemented", "Ellipsis", "__debug__",
            // Built-in functions
            "abs()", "all()", "any()", "ascii()", "bin()", "bool()", "breakpoint()",
            "bytearray()", "bytes()", "callable()", "chr()", "classmethod()", "compile()", "complex()",
            "delatrre()", "dict()", "dir()", "divmod()", "enumerate()", "eval()", "exec()", "filter()",
            "float()", "format()", "frozenset()", "getattr()", "globals()", "hassattr()", "hash()",
            "help()", "hex()", "id()", "input()", "int()", "isinstance()", "issubclass()", "iter()",
            "len()", "list()", "locals()", "map()", "max()", "memoryview()", "min()", "next()",
            "object()", "oct()", "open()", "ord()", "pow()", "print()", "property()", "range()",
            "repr()", "reversed()", "round()", "set()", "setattr()", "slice()", "sorted()",
            "staticmethod()", "str()", "sum()", "super()", "tuple()", "type()", "vars()", "zip()",
            "__import__()"};

    //init IME
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

    int findSeparator(String str, boolean from_start) {
        char c;
        int inx;
        for(int i=0;i < str.length(); i++)
        {
            inx = from_start?i:str.length()-1-i;
            c = str.charAt(inx);

            if(!Character.isLetter(c) && c != '_' && !Character.isDigit(c)) {
                return inx;
            }
        }
        return -1;
    }

    void updCandidates() {
        Log.println(Log.INFO,"INFO", "updCandidates");
        InputConnection ic = getCurrentInputConnection();
        String before = ic.getTextBeforeCursor(TAKECHAR_NUM,0).toString();
        String after = ic.getTextAfterCursor(TAKECHAR_NUM,0).toString();
        int left_sep = findSeparator(before,false);
        int right_sep = findSeparator(after,true);

        if(left_sep != -1)
            before = before.substring(left_sep+1);

        if(right_sep != -1)
            after = after.substring(0,right_sep);

        String original = before + after;

        Vector<String> possible_cand = new Vector<>();
        if(original.length() > 0) {
            for (String str : dictionary) {
                if (str.length() >= original.length() && str.substring(0, original.length()).equalsIgnoreCase(original)) {
                    possible_cand.add(str);
                }
            }
        }

        String cand;
        for(int i=0;i<3;i++) {
            if(possible_cand.size() > i) {
                cand = possible_cand.get(i);
            } else {
                cand = "";
            }
            ((TextView)candidates.getChildAt(i)).setText(cand);
        }
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
                String before = ic.getTextBeforeCursor(TAKECHAR_NUM,0).toString();
                String after = ic.getTextAfterCursor(TAKECHAR_NUM,0).toString();
                int left_sep = findSeparator(before,false);
                int right_sep = findSeparator(after,true);
                if(left_sep == -1)
                    left_sep = 0;
                if(right_sep == -1)
                    right_sep = TAKECHAR_NUM;

                ic.deleteSurroundingText(left_sep, right_sep);
                ic.commitText(btn.getText(),1);
                updCandidates();
            }
        };

        Button cand1 = new Button(getApplicationContext());
        Button cand2 = new Button(getApplicationContext());
        Button cand3 = new Button(getApplicationContext());
        cand1.setOnClickListener(listener);
        cand2.setOnClickListener(listener);
        cand3.setOnClickListener(listener);
        cand1.setTextSize(CAND_TEXT_SIZE);
        cand2.setTextSize(CAND_TEXT_SIZE);
        cand3.setTextSize(CAND_TEXT_SIZE);
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
        super.onUpdateCursorAnchorInfo(info);
        updCandidates();
    }


    void vibrate_on_tap(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibra.vibrate(VibrationEffect.createOneShot(VIB_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibra.vibrate(VIB_DURATION);
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