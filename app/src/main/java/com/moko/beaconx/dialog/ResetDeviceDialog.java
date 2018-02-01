package com.moko.beaconx.dialog;

import android.content.Context;
import android.view.View;

import com.moko.beaconx.R;

import butterknife.OnClick;

public class ResetDeviceDialog extends BaseDialog {

    public ResetDeviceDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_reset_alert;
    }

    @Override
    protected void renderConvertView(View convertView, Object o) {

    }

    @OnClick({R.id.tv_cancel, R.id.tv_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                resetDeviceClickListener.onDismiss();
                break;
            case R.id.tv_ensure:
                dismiss();
                resetDeviceClickListener.onEnsureClicked();
                break;
        }
    }

    private ResetDeviceClickListener resetDeviceClickListener;

    public void setResetDeviceClickListener(ResetDeviceClickListener resetDeviceClickListener) {
        this.resetDeviceClickListener = resetDeviceClickListener;
    }

    public interface ResetDeviceClickListener {

        void onEnsureClicked();

        void onDismiss();
    }
}
