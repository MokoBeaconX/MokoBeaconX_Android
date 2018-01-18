package com.moko.beaconx.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;

import com.moko.beaconx.R;

import butterknife.ButterKnife;

/**
 * @Date 2017/12/11 0011
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.dialog.BaseDialog
 */
public abstract class BaseDialog<T> extends Dialog {
    protected T t;
    private boolean dismissEnable;
    private Animation animation;

    public BaseDialog(Context context) {
        super(context, R.style.BaseDialogTheme);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View convertView = LayoutInflater.from(getContext()).inflate(getLayoutResId(), null);
        ButterKnife.bind(this, convertView);
        renderConvertView(convertView, t);
        if (animation != null) {
            convertView.setAnimation(animation);
        }
        if (dismissEnable) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        setContentView(convertView);
    }

    protected abstract int getLayoutResId();

    protected abstract void renderConvertView(final View convertView, final T t);


    @Override
    public void show() {
        super.show();
        //set the dialog fullscreen
        final Window window = getWindow();
        assert window != null;
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        //设置窗口高度为包裹内容
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
    }

    public void setData(T t) {
        this.t = t;
    }

    protected void setAnimation(Animation animation) {
        this.animation = animation;
    }

    protected void setDismissEnable(boolean dismissEnable) {
        this.dismissEnable = dismissEnable;
    }
}
