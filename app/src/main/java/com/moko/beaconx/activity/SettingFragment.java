package com.moko.beaconx.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.beaconx.dialog.BeaconAlertDialog;
import com.moko.beaconx.dialog.DeviceNameDialog;
import com.moko.beaconx.dialog.ModifyPasswordDialog;
import com.moko.beaconx.dialog.ResetDeviceDialog;
import com.moko.support.utils.MokoUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";
    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;
    @BindView(R.id.iv_connectable)
    ImageView ivConnectable;
    @BindView(R.id.iv_power)
    ImageView ivPower;

    private DeviceInfoActivity activity;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick({R.id.rl_device_name, R.id.rl_password, R.id.rl_update_firmware, R.id.rl_reset_facotry, R.id.iv_connectable
            , R.id.iv_power})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_device_name:
                final DeviceNameDialog dialog = new DeviceNameDialog(activity);
                dialog.setOnDeviceNameClicked(new DeviceNameDialog.DeviceNameClickListener() {
                    @Override
                    public void onEnsureClicked(String deviceName) {
                        activity.setDeviceName(deviceName);
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                dialog.show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.showKeyboard();
                            }
                        });
                    }
                }, 200);
                break;
            case R.id.rl_password:
                final ModifyPasswordDialog modifyPasswordDialog = new ModifyPasswordDialog(activity);
                modifyPasswordDialog.setOnModifyPasswordClicked(new ModifyPasswordDialog.ModifyPasswordClickListener() {
                    @Override
                    public void onEnsureClicked(String password) {
                        activity.modifyPassword(password);
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                modifyPasswordDialog.show();
                Timer modifyTimer = new Timer();
                modifyTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                modifyPasswordDialog.showKeyboard();
                            }
                        });
                    }
                }, 200);
                break;
            case R.id.rl_update_firmware:
                activity.chooseFirmwareFile();
                break;
            case R.id.rl_reset_facotry:
                final ResetDeviceDialog resetDeviceDialog = new ResetDeviceDialog(activity);
                resetDeviceDialog.setResetDeviceClickListener(new ResetDeviceDialog.ResetDeviceClickListener() {
                    @Override
                    public void onEnsureClicked() {
                        activity.resetDevice();
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                resetDeviceDialog.show();
                break;
            case R.id.iv_connectable:
                final BeaconAlertDialog connectAlertDialog = new BeaconAlertDialog(activity);
                connectAlertDialog.setData(isConneacted ? "Are you sure to make device disconnectable?" : "Are you sure to make device connectable?");
                connectAlertDialog.setConnectAlertClickListener(new BeaconAlertDialog.ConnectAlertClickListener() {
                    @Override
                    public void onEnsureClicked() {
                        isConneacted = !isConneacted;
                        activity.setConnectable(isConneacted);
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                connectAlertDialog.show();
                break;
            case R.id.iv_power:
                final BeaconAlertDialog powerAlertDialog = new BeaconAlertDialog(activity);
                powerAlertDialog.setData("Are you sure to turn off the BeaconX?Please make sure the device has a button to turn on!");
                powerAlertDialog.setConnectAlertClickListener(new BeaconAlertDialog.ConnectAlertClickListener() {
                    @Override
                    public void onEnsureClicked() {
                        activity.setClose();
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                powerAlertDialog.show();
                break;
        }
    }

    public void setDeviceName(String deviceName) {
        tvDeviceName.setText(deviceName);
    }

    boolean isConneacted;

    public void setConnectable(byte[] value) {
        int connectable = Integer.parseInt(MokoUtils.byte2HexString(value[4]), 16);
        isConneacted = connectable == 1;
        if (connectable == 1) {
            ivConnectable.setImageResource(R.drawable.connectable_checked);
        } else {
            ivConnectable.setImageResource(R.drawable.connectable_unchecked);
        }
    }

    public void setClose() {
        ivPower.setImageResource(R.drawable.connectable_unchecked);
    }
}
