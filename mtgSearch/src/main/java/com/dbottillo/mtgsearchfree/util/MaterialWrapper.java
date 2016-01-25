package com.dbottillo.mtgsearchfree.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

public final class MaterialWrapper {

    private MaterialWrapper() {

    }

    public static void setElevation(View view, float value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(value);
        }
    }

    public static void copyElevation(View targetView, View copiedView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetView.setElevation(copiedView.getElevation());
        }
    }

    public static Drawable getRippleDrawable(Context context, String drawableName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawableName = "ripple_" + drawableName;
        }
        int id = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
        return context.getResources().getDrawable(id);
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static void setNavigationBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(color);
        }
    }

}