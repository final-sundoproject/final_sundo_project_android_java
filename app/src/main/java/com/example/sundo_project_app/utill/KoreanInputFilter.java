package com.example.sundo_project_app.utill;

import android.text.InputFilter;
import android.text.Spanned;

public class KoreanInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (c < 0xAC00 || c > 0xD7A3) {
                return "";
            }
        }
        return null;
    }
}