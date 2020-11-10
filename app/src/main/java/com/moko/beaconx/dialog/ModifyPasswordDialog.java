package com.moko.beaconx.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.moko.beaconx.R;
import com.moko.beaconx.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class ModifyPasswordDialog extends BaseDialog {


    @BindView(R.id.et_new_password)
    EditText etNewPassword;
    @BindView(R.id.et_new_password_re)
    EditText etNewPasswordRe;

    public ModifyPasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_change_password;
    }

    @Override
    protected void renderConvertView(View convertView, Object o) {

    }

    @OnClick({R.id.tv_cancel, R.id.tv_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                modifyPasswordClickListener.onDismiss();
                break;
            case R.id.tv_ensure:
                String newPassword = etNewPassword.getText().toString();
                String newPasswordRe = etNewPasswordRe.getText().toString();
                if (TextUtils.isEmpty(newPassword)) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.password_length));
                    return;
                }
                if (newPassword.length() != 8) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.password_length));
                    return;
                }
                if (TextUtils.isEmpty(newPasswordRe)) {
                    ToastUtils.showToast(getContext(), "The two passwords differ.");
                    return;
                }
                if (newPasswordRe.length() != 8) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.password_length));
                    return;
                }
                if (!newPasswordRe.equals(newPassword)) {
                    ToastUtils.showToast(getContext(), "The two passwords differ.");
                    return;
                }
                dismiss();
                modifyPasswordClickListener.onEnsureClicked(etNewPassword.getText().toString());
                break;
        }
    }

    private ModifyPasswordClickListener modifyPasswordClickListener;

    public void setOnModifyPasswordClicked(ModifyPasswordClickListener modifyPasswordClickListener) {
        this.modifyPasswordClickListener = modifyPasswordClickListener;
    }

    public interface ModifyPasswordClickListener {

        void onEnsureClicked(String password);

        void onDismiss();
    }

    public void showKeyboard() {
        if (etNewPassword != null) {
            //设置可获得焦点
            etNewPassword.setFocusable(true);
            etNewPassword.setFocusableInTouchMode(true);
            //请求获得焦点
            etNewPassword.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) etNewPassword
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(etNewPassword, 0);
        }
    }
}
