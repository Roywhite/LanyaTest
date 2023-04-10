package com.xiaobai.bookStore.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastNoLooperUtil {
    public static void showToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
