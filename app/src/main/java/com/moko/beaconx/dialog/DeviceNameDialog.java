package com.moko.beaconx.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.moko.beaconx.R;
import com.moko.beaconx.utils.ToastUtils;

import butterknife.Bind;
import butterknife.OnClick;

public class DeviceNameDialog extends BaseDialog {


    @Bind(R.id.et_device_name)
    EditText etDeviceName;

    public DeviceNameDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_device_name;
    }

    @Override
    protected void renderConvertView(View convertView, Object o) {

    }

    @OnClick({R.id.tv_cancel, R.id.tv_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                deviceNameClickListener.onDismiss();
                break;
            case R.id.tv_ensure:
                dismiss();
                if (TextUtils.isEmpty(etDeviceName.getText().toString())) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.device_name_null));
                    return;
                }
                if (etDeviceName.getText().toString().length() > 8) {
                    ToastUtils.showToast(getContext(), "Data format incorrect!");
                    return;
                }
                deviceNameClickListener.onEnsureClicked(etDeviceName.getText().toString());
                break;
        }
    }

    private DeviceNameClickListener deviceNameClickListener;

    public void setOnDeviceNameClicked(DeviceNameClickListener deviceNameClickListener) {
        this.deviceNameClickListener = deviceNameClickListener;
    }

    public interface DeviceNameClickListener {

        void onEnsureClicked(String deviceName);

        void onDismiss();
    }

    public void showKeyboard() {
        if (etDeviceName != null) {
            //设置可获得焦点
            etDeviceName.setFocusable(true);
            etDeviceName.setFocusableInTouchMode(true);
            //请求获得焦点
            etDeviceName.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) etDeviceName
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(etDeviceName, 0);
        }
    }
}
