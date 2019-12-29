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
    // it is the keyboard listener
    private PyBoardView keyboardView;
    // these are two tabs of PYB
    // normal -- regular keyboard with a few features good for python
    // symbols -- for snippets and special symbols
    private Keyboard keyboard_normal, keyboard_symbols;
    // vibrator on touch
    Vibrator vibra;
    // layout with three buttonts, each button represents a possible command user might use
    LinearLayout candidates;

    // num of chars taken on each side to find candidates (should be greater than the longest candidate from the dictionary)
    private static final int TAKECHAR_NUM = 15;
    // text size for candidates buttons
    private static final int CAND_TEXT_SIZE = 15;
    // duration of vibration
    private static final int VIB_DURATION = 45;
    // number of candidates to draw and find
    private static final int CANDIDATES_NUM = 3;
    // dictionary with possible candidates
    private static final String[] dictionary = new String[]{
            // Comparisons
            "is", "is not", "in", "not",
            // Keywords and constants
            "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class",
            "continue", "def", "del", "elif", "else", "except", "finally", "for", "from", "global",
            "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return",
            "try", "while", "with", "yield", "NotImplemented", "Ellipsis", "__debug__",
            // Types
            "int", "float", "complex", "list", "tuple", "range", "str", "set", "frozenset", "dict",
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
            "__import__()",
            // Additional types
            "Fraction", "Decimal", "bytes", "bytearray", "memoryview"};

    // todo add dictionary for lines with dots, for example arr.sort()
    // todo handle this: print() -> [print()][ ][ ], should be [ ][ ][ ]
    // todo make candidates view prettier
    // todo add spaces after some candidates
    // todo move cursor into brackets after adding a function candidate

    // init IME
    @Override
    public View onCreateInputView() {
        Log.println(Log.INFO,"INFO", "onCreateInputView");
        // create keyboardview from layout
        keyboardView = (PyBoardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);

        // create two keyboards
        // normal -- regular keyboard with a few features good for python
        // symbols -- for snipets and special symbols
        keyboard_normal = new Keyboard(this, R.xml.keyboard_normal);
        keyboard_symbols = new Keyboard(this, R.xml.symbols_normal);
        // Our keyboard is filled with capitalized characters, so we unshift them;)
        keyboard_normal.setShifted(false);

        // set normal keyboard as default and make keyboardView the listener
        keyboardView.setKeyboard(keyboard_normal);
        keyboardView.setOnKeyboardActionListener(this);

        // init vibrator on touch
        vibra = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        return keyboardView;
    }

    // find position of first or last separating symbol which separates a string into words
    // separating symbols -- ' ', '\n', '.', etc
    int findSeparator(String str, boolean from_start) {
        char c;
        int inx;
        for(int i=0;i < str.length(); i++)
        {
            inx = from_start?i:str.length()-1-i;
            c = str.charAt(inx);

            if(!Character.isLetterOrDigit(c) && c != '_' && c != '(' && c != ')') {
                return inx;
            }
        }
        return -1;
    }

    // update candidates
    void updCandidates() {
        Log.println(Log.INFO,"INFO", "updCandidates");
        InputConnection ic = getCurrentInputConnection();

        // find the selected word
        String before = ic.getTextBeforeCursor(TAKECHAR_NUM,0).toString();
        String after = ic.getTextAfterCursor(TAKECHAR_NUM,0).toString();
        int left_sep = findSeparator(before,false);
        int right_sep = findSeparator(after,true);

        if(left_sep != -1)
            before = before.substring(left_sep+1);

        if(right_sep != -1)
            after = after.substring(0,right_sep);

        // original is the selected word
        String original = before + after;

        // check dictionary on possible candidates
        Vector<String> possible_cand = new Vector<>();
        if(original.length() > 0) {
            for (String str : dictionary) {
                // the str from dictionary is a candidate if it is longer or equal to the original str
                // and it's starts with the same letters
                if (str.length() >= original.length() &&
                        str.substring(0, original.length()).equalsIgnoreCase(original))
                    possible_cand.add(str);
                // we've already found all candidates we need, stop the algorithm
                if(possible_cand.size() == CANDIDATES_NUM)
                    break;
            }
        }

        // fill candidates layout with the found candidates
        String cand;
        for(int i=0;i<CANDIDATES_NUM;i++) {
            if(possible_cand.size() > i) {
                cand = possible_cand.get(i);
            } else {
                cand = "";
            }
            ((TextView)candidates.getChildAt(i)).setText(cand);
        }
    }

    // todo comment this
    @Override
    public View onCreateCandidatesView () {
        Log.println(Log.INFO,"INFO", "onCreateCandidatesView");
        // create linear layout
        candidates = new LinearLayout(getApplicationContext());
        candidates.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        // create listener for buttons on the layout
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Button btn = (Button) view;

                // everything can be a candidate for an empty string, so do nothing if you get one
                if(btn.getText() == "")
                    return;

                // find left and right separate characters
                InputConnection ic = getCurrentInputConnection();
                String before = ic.getTextBeforeCursor(TAKECHAR_NUM,0).toString();
                String after = ic.getTextAfterCursor(TAKECHAR_NUM,0).toString();
                int left_sep = findSeparator(before,false);
                int right_sep = findSeparator(after,true);
                // if we cant find the right separator, use the whole right part
                if(right_sep == -1)
                    right_sep = after.length();

                // erase original str
                ic.deleteSurroundingText(before.length()-left_sep-1, right_sep);
                // add chosen candidate
                ic.commitText(btn.getText(),1);

                // upd candidates
                updCandidates();
            }
        };

        // create three buttons and add it to the layout
        int key_height = (int) getResources().getDimension(R.dimen.CandidatesHeight);
        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(0,key_height,1);
        for(int i = 0; i<CANDIDATES_NUM; i++) {
            Button btn = new Button(getApplicationContext());
            btn.setOnClickListener(listener);
            btn.setTextSize(CAND_TEXT_SIZE);
            btn.setTransformationMethod(null);
            candidates.addView(btn,i,par);
        }
        // update candidates
        updCandidates();
        // show candidates
        setCandidatesViewShown(true);

        // return candidates view
        return candidates;
    }

    // if text cursor is moved, update candidates
    @Override
    public void onUpdateCursorAnchorInfo(CursorAnchorInfo info) {
        Log.println(Log.INFO,"INFO", "cursor position update");
        super.onUpdateCursorAnchorInfo(info);
        updCandidates();
    }

    // vibrate on tap
    void vibrate_on_tap(){
        Log.println(Log.INFO,"INFO", "vibrate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibra.vibrate(VibrationEffect.createOneShot(VIB_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibra.vibrate(VIB_DURATION);
        }
    }

    // special commit for snippets, it also moves cursor to the area of user interest
    void CommitSnippet(InputConnection ic, String str)
    {
        Log.println(Log.INFO,"INFO", "CommitSnippet");
        String buff = "";
        int pos = 0;
        int i = 0;
        // find position of the area of user interest (it is represented by a smiley face)
        // todo it might be done through search
        for (char ch: str.toCharArray()) {
            if(ch == '☺') {
                pos = i;
            }
            else {
                buff += ch;
                i++;
            }
        }
        // commit text of the snippet
        ic.commitText(buff, 1);
        // move cursor to the area of the interest
        for(int j=0; j<i-pos; j++) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
        }
        // get back to the normal keyboard
        keyboardView.setKeyboard(keyboard_normal);
    }

    // softkeyboard's key press handler
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.println(Log.INFO,"INFO", "onKey");
        InputConnection ic = getCurrentInputConnection();

        if (ic == null) return;

        // switch-case for possible keys
        switch (primaryCode) {
            case Keyboard.KEYCODE_SHIFT:
                // trigger shift
                keyboardView.setShifted(!keyboardView.isShifted());
                break;
            case Keyboard.KEYCODE_DELETE:
                CharSequence selectedText = ic.getSelectedText(0);
                if (TextUtils.isEmpty(selectedText)) {
                    // no selection, delete previous character
                    ic.deleteSurroundingText(1, 0);
                } else {
                    // delete the selected text
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
            // shift
            case -100:
                keyboardView.setShifted(false);
                keyboardView.setKeyboard(keyboard_symbols);
                break;
            // get back from symbols tab
            case -101:
                keyboardView.setKeyboard(keyboard_normal);
                break;
            // snippets
            // smiley face shows where the pointer will be
            case -102:
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
                // todo may i have copyright issues for doing this?
                CommitSnippet(ic, "#PYB Stepik easy input\ndef gen_in(n):\n\tfor _ in range(n):\n" +
                        "\t\tyield int(input())\n\na, b, c = gen_in(3)☺");
                break;
            case -108:
                CommitSnippet(ic, "print(☺)");
                break;
            // fast access to characters from the normal keyboard
            case -200:
                ic.commitText("True", 1);
                break;
            case -201:
                ic.commitText("False", 1);
                break;
            // regular characters
            default:
                char code = (char) primaryCode;
                // handle CapsLock (original characters are Capitalized)
                if(Character.isLetter(code) && !keyboardView.isShifted())
                    code = Character.toLowerCase(code);

                // send character
                ic.commitText(String.valueOf(code), 1);

                // Caps is used only for 1 letter
                keyboardView.setShifted(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ic.requestCursorUpdates(CURSOR_UPDATE_IMMEDIATE);
        }
    }

    @Override
    public void onPress(int primaryCode) {
        Log.println(Log.INFO,"INFO", "onPress");
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