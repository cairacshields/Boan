package com.example.cairashields.boan.Helper;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

public class Utils{
    public static int fetchColor(@ColorRes int color, Context context) {
        return ContextCompat.getColor(context, color);
    }
}
