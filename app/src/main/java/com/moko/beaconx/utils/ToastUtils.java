package com.moko.beaconx.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

/**
 * toast方法
 *
 * @author jianweiwang
 */
final public class ToastUtils {

    public static final void showToast(Context context, String tip) {
        showToast(context, tip, true);
    }

    public static final void showToast(Context context, int tipID) {
        showToast(context, tipID, true);
    }

    public static final void showToast(Context context, int tipID,
                                       boolean isCenter) {
        String tip = (String) context.getResources().getText(tipID);
        showToast(context, tip, isCenter);
    }

    /**
     * toast n个字以上 LENGTH_LONG
     *
     * @param context
     * @param tip
     * @param isCenter
     */
    public static final void showToast(Context context, String tip,
                                       boolean isCenter) {
        int duration = Toast.LENGTH_SHORT;
        if (TextUtils.isEmpty(tip)) {
            return;
        }
        if (tip.length() >= 15) {
            duration = Toast.LENGTH_LONG;
        }
        Toast toast = Toast.makeText(context, tip, duration);
        if (isCenter) {
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        toast.show();
    }
}
