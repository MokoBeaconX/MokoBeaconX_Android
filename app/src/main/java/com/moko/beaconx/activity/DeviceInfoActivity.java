package com.moko.beaconx.activity;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moko.beaconx.AppConstants;
import com.moko.beaconx.R;
import com.moko.beaconx.service.DfuService;
import com.moko.beaconx.service.MokoService;
import com.moko.beaconx.utils.FileUtils;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @Bind(R.id.frame_container)
    FrameLayout frameContainer;
    @Bind(R.id.radioBtn_slot)
    RadioButton radioBtnSlot;
    @Bind(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @Bind(R.id.radioBtn_device)
    RadioButton radioBtnDevice;
    @Bind(R.id.rg_options)
    RadioGroup rgOptions;
    public MokoService mMokoService;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    private FragmentManager fragmentManager;
    private SlotFragment slotFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mPassword;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mIsClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        mPassword = getIntent().getStringExtra(AppConstants.EXTRA_KEY_PASSWORD);
        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        fragmentManager = getFragmentManager();
        showDeviceFragment();
        showSettingFragment();
        showSlotFragment();
        rgOptions.setOnCheckedChangeListener(this);
        radioBtnSlot.setChecked(true);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONNECT_SUCCESS);
            filter.addAction(MokoConstants.ACTION_CONNECT_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_RESPONSE_SUCCESS);
            filter.addAction(MokoConstants.ACTION_RESPONSE_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_RESPONSE_FINISH);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(200);
            registerReceiver(mReceiver, filter);
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
            } else {
                getSlotType();
                getDeviceInfo();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void getSlotType() {
        if (mMokoService == null) {
            return;
        }
        showSyncingProgressDialog();
        mMokoService.sendOrder(mMokoService.getSlotType());
    }

    private void getDeviceInfo() {
        if (mMokoService == null) {
            return;
        }
        mMokoService.sendOrder(mMokoService.getDeviceMac(),
                mMokoService.getDeviceName(), mMokoService.getConnectable(),
                mMokoService.getManufacturer(), mMokoService.getDeviceModel(),
                mMokoService.getProductDate(), mMokoService.getHardwareVersion(),
                mMokoService.getFirmwareVersion(), mMokoService.getSoftwareVersion(),
                mMokoService.getBattery());
    }

    private String unLockResponse;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    abortBroadcast();
                }
                if (MokoConstants.ACTION_CONNECT_SUCCESS.equals(action)) {
                    dismissLoadingProgressDialog();
                    showVerifyingProgressDialog();
                    mMokoService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMokoService.sendOrder(mMokoService.getLockState());
                        }
                    }, 1000);
                }
                if (MokoConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    if (mIsClose) {
                        return;
                    }
                    dismissSyncProgressDialog();
                    dismissLoadingProgressDialog();
                    if (MokoSupport.getInstance().isBluetoothOpen() && !isUpgrade) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
                        builder.setTitle("Dismiss");
                        builder.setCancelable(false);
                        builder.setMessage("The device disconnected!");
                        builder.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMokoService.connDevice(mDeviceMac);
                                showLoadingProgressDialog();
                            }
                        });
                        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                back();
                            }
                        });
                        builder.show();
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                }
                if (MokoConstants.ACTION_RESPONSE_FINISH.equals(action)) {
                    dismissSyncProgressDialog();
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderType orderType = response.orderType;
                    int responseType = response.responseType;
                    byte[] value = response.responseValue;
                    switch (orderType) {
                        case writeConfig:
                            if (value.length >= 2) {
                                int key = value[1] & 0xff;
                                ConfigKeyEnum configKeyEnum = ConfigKeyEnum.fromConfigKey(key);
                                if (configKeyEnum == null) {
                                    return;
                                }
                                switch (configKeyEnum) {
                                    case GET_SLOT_TYPE:
                                        if (value.length >= 9) {
                                            slotFragment.updateSlotType(value);
                                        }
                                        break;
                                    case GET_DEVICE_MAC:
                                        if (value.length >= 10) {
                                            String valueStr = MokoUtils.bytesToHexString(value);
                                            String mac = valueStr.substring(8, valueStr.length()).toUpperCase();
                                            String macShow = String.format("%s:%s:%s:%s:%s:%s", mac.substring(0, 2), mac.substring(2, 4), mac.substring(4, 6), mac.substring(6, 8), mac.substring(8, 10), mac.substring(10, 12));
                                            deviceFragment.setDeviceMac(macShow);
                                            mDeviceMac = macShow;
                                        }
                                        break;
                                    case GET_DEVICE_NAME:
                                        if (value.length >= 4) {
                                            String valueStr = MokoUtils.bytesToHexString(value);
                                            String deviceName = MokoUtils.hex2String(valueStr.substring(8, valueStr.length()));
                                            settingFragment.setDeviceName(deviceName);
                                            mDeviceName = deviceName;
                                        }
                                        break;
                                    case GET_CONNECTABLE:
                                        if (value.length >= 5) {
                                            settingFragment.setConnectable(value);
                                        }
                                        break;
                                    case GET_IBEACON_UUID:
                                        if (value.length >= 20) {
                                            slotFragment.setiBeaconUUID(value);
                                        }
                                        break;
                                    case GET_IBEACON_INFO:
                                        if (value.length >= 9) {
                                            slotFragment.setiBeaconInfo(value);
                                        }
                                        break;
                                    case SET_DEVICE_NAME:
                                        if ("eb58000100".equals(MokoUtils.bytesToHexString(value).toLowerCase())) {
                                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                                        }
                                        break;
                                    case SET_CONNECTABLE:
                                        if ("eb62000100".equals(MokoUtils.bytesToHexString(value).toLowerCase())) {
                                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                                        }
                                        break;
                                    case SET_CLOSE:
                                        if ("eb60000100".equals(MokoUtils.bytesToHexString(value).toLowerCase())) {
                                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                                            settingFragment.setClose();
                                            back();
                                        }
                                        break;
                                }
                            }
                            break;
                        case manufacturer:
                            deviceFragment.setManufacturer(value);
                            break;
                        case deviceModel:
                            deviceFragment.setDeviceModel(value);
                            break;
                        case productDate:
                            deviceFragment.setProductDate(value);
                            break;
                        case hardwareVersion:
                            deviceFragment.setHardwareVersion(value);
                            break;
                        case firmwareVersion:
                            deviceFragment.setFirmwareVersion(value);
                            break;
                        case softwareVersion:
                            deviceFragment.setSoftwareVersion(value);
                            break;
                        case battery:
                            deviceFragment.setBattery(value);
                            break;
                        case advSlotData:
                            if (value.length >= 1) {
                                slotFragment.setSlotData(value);
                            }
                            break;
                        case radioTxPower:
                            if (value.length >= 1) {
                                slotFragment.setTxPower(value);
                            }
                            break;
                        case advInterval:
                            if (value.length >= 2) {
                                slotFragment.setAdvInterval(value);
                            }
                            break;
                        case lockState:
                            String valueStr = MokoUtils.bytesToHexString(value);
                            if ("eb63000100".equals(valueStr.toLowerCase())) {
                                // 设备上锁
                                if (isModifyPassword) {
                                    isModifyPassword = false;
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(DeviceInfoActivity.this, "Modify successfully!");
                                    back();
                                }
                            } else if ("00".equals(valueStr)) {
                                if (!TextUtils.isEmpty(unLockResponse)) {
                                    dismissVerifyProgressDialog();
                                    unLockResponse = "";
                                    ToastUtils.showToast(DeviceInfoActivity.this, "Password error");
                                    back();
                                } else {
                                    LogModule.i("锁定状态，获取unLock，解锁");
                                    mMokoService.sendOrder(mMokoService.getUnLock());
                                }
                            } else if ("01".equals(valueStr)) {
                                dismissVerifyProgressDialog();
                                LogModule.i("解锁成功");
                                unLockResponse = "";
                                getSlotType();
                                getDeviceInfo();
                            }
                            break;
                        case unLock:
                            if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                                unLockResponse = MokoUtils.bytesToHexString(value);
                                LogModule.i("返回的随机数：" + unLockResponse);
                                mMokoService.sendOrder(mMokoService.setConfigNotify(), mMokoService.setUnLock(mPassword, value));
                            }
                            if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                                mMokoService.sendOrder(mMokoService.getLockState());
                            }
                            break;
                        case resetDevice:
                            ToastUtils.showToast(DeviceInfoActivity.this, "Reset successfully!");
                            back();
                            break;
                    }
                }

                if (MokoConstants.ACTION_RESPONSE_NOTIFY.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    switch (orderType) {
                        case notifyConfig:
                            String valueHexStr = MokoUtils.bytesToHexString(value);
                            if ("eb63000100".equals(valueHexStr.toLowerCase())) {
                                ToastUtils.showToast(DeviceInfoActivity.this, "Device Locked!");
                                back();
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
                            builder.setTitle("Dismiss");
                            builder.setCancelable(false);
                            builder.setMessage("The current system of bluetooth is not available!");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    back();
                                }
                            });
                            builder.show();
                            break;

                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MokoConstants.REQUEST_CODE_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                // 未打开蓝牙
                finish();
            }

        }
        if (requestCode == REQUEST_CODE_SELECT_FIRMWARE) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String firmwareFilePath = FileUtils.getPath(this, uri);
                //
                final File firmwareFile = new File(firmwareFilePath);
                if (firmwareFile.exists()) {
                    final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                            .setDeviceName(mDeviceName)
                            .setKeepBond(false)
                            .setDisableNotification(true);
                    starter.setZip(null, firmwareFilePath);
                    starter.start(this, DfuService.class);
                    showDFUProgressDialog("Waiting...");
                } else {
                    Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    private ProgressDialog syncingDialog;

    public void showSyncingProgressDialog() {
        syncingDialog = new ProgressDialog(this);
        syncingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        syncingDialog.setCanceledOnTouchOutside(false);
        syncingDialog.setCancelable(false);
        syncingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        syncingDialog.setMessage("Syncing...");
        if (!isFinishing() && syncingDialog != null && !syncingDialog.isShowing()) {
            syncingDialog.show();
        }
    }

    public void dismissSyncProgressDialog() {
        if (!isFinishing() && syncingDialog != null && syncingDialog.isShowing()) {
            syncingDialog.dismiss();
        }
    }

    @OnClick({R.id.tv_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
        }
    }

    private void back() {
        MokoSupport.getInstance().disConnectBle();
        mIsClose = false;
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showSlotFragment() {
        if (slotFragment == null) {
            slotFragment = SlotFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.frame_container, slotFragment).commit();
        } else {
            fragmentManager.beginTransaction().hide(settingFragment).hide(deviceFragment).show(slotFragment).commit();
        }
        tvTitle.setText(getString(R.string.options_title));
    }

    private void showSettingFragment() {
        if (settingFragment == null) {
            settingFragment = SettingFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.frame_container, settingFragment).commit();
        } else {
            fragmentManager.beginTransaction().hide(slotFragment).hide(deviceFragment).show(settingFragment).commit();
        }
        tvTitle.setText(getString(R.string.setting_title));
    }

    private void showDeviceFragment() {
        if (deviceFragment == null) {
            deviceFragment = DeviceFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.frame_container, deviceFragment).commit();
        } else {
            fragmentManager.beginTransaction().hide(slotFragment).hide(settingFragment).show(deviceFragment).commit();
        }
        tvTitle.setText(getString(R.string.device_title));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioBtn_slot:
                showSlotFragment();
                getSlotType();
                break;
            case R.id.radioBtn_setting:
                showSettingFragment();
                break;
            case R.id.radioBtn_device:
                showDeviceFragment();
                break;
        }
    }

    public void setDeviceName(String deviceName) {
        showSyncingProgressDialog();
        mMokoService.sendOrder(mMokoService.setDeviceName(deviceName), mMokoService.getDeviceName());
    }

    private boolean isModifyPassword;

    public void modifyPassword(String password) {
        isModifyPassword = true;
        showSyncingProgressDialog();
        mMokoService.sendOrder(mMokoService.setLockState(password));
    }

    public void resetDevice() {
        showSyncingProgressDialog();
        mMokoService.sendOrder(mMokoService.resetDevice());
    }


    public void setConnectable(boolean isConneacted) {
        showSyncingProgressDialog();
        mMokoService.sendOrder(mMokoService.setConnectable(isConneacted), mMokoService.getConnectable());
    }

    public void setClose() {
        mIsClose = true;
        showSyncingProgressDialog();
        mMokoService.sendOrder(mMokoService.setClose());
    }

    public void chooseFirmwareFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
        builder.setTitle("Dismiss");
        builder.setCancelable(false);
        builder.setMessage("The device disconnected!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isUpgrade = false;
                back();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private int mDeviceConnectCount;
    private boolean isUpgrade;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            LogModule.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                Toast.makeText(DeviceInfoActivity.this, "Error:DFU Failed", Toast.LENGTH_SHORT).show();
                dismissDFUProgressDialog();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            LogModule.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            isUpgrade = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }


        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            Toast.makeText(DeviceInfoActivity.this, "DfuCompleted!", Toast.LENGTH_SHORT).show();
            dismissDFUProgressDialog();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            mDFUDialog.setMessage("Progress:" + percent + "%");
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            Toast.makeText(DeviceInfoActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
            LogModule.i("Error:" + message);
            dismissDFUProgressDialog();
        }
    };

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new ProgressDialog(DeviceInfoActivity.this);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setMessage("Connecting...");
        if (!isFinishing() && mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    private void dismissLoadingProgressDialog() {
        if (!isFinishing() && mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private ProgressDialog mVerifyingDialog;

    private void showVerifyingProgressDialog() {
        mVerifyingDialog = new ProgressDialog(this);
        mVerifyingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mVerifyingDialog.setCanceledOnTouchOutside(false);
        mVerifyingDialog.setCancelable(false);
        mVerifyingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mVerifyingDialog.setMessage("Verifying...");
        if (!isFinishing() && mVerifyingDialog != null && !mVerifyingDialog.isShowing()) {
            mVerifyingDialog.show();
        }
    }

    private void dismissVerifyProgressDialog() {
        if (!isFinishing() && mVerifyingDialog != null && mVerifyingDialog.isShowing()) {
            mVerifyingDialog.dismiss();
        }
    }
}
