package com.moko.beaconx.activity;


import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
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
import com.moko.beaconx.dialog.AlertMessageDialog;
import com.moko.beaconx.dialog.LoadingDialog;
import com.moko.beaconx.dialog.LoadingMessageDialog;
import com.moko.beaconx.entity.ValidParams;
import com.moko.beaconx.service.DfuService;
import com.moko.beaconx.utils.FileUtils;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IdRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @BindView(R.id.frame_container)
    FrameLayout frameContainer;
    @BindView(R.id.radioBtn_slot)
    RadioButton radioBtnSlot;
    @BindView(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @BindView(R.id.radioBtn_device)
    RadioButton radioBtnDevice;
    @BindView(R.id.rg_options)
    RadioGroup rgOptions;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    private FragmentManager fragmentManager;
    private SlotFragment slotFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mPassword;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mIsClose;
    private ValidParams validParams;
    private int validCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        validParams = new ValidParams();
        mPassword = getIntent().getStringExtra(AppConstants.EXTRA_KEY_PASSWORD);
        fragmentManager = getFragmentManager();
        showDeviceFragment();
        showSettingFragment();
        showSlotFragment();
        rgOptions.setOnCheckedChangeListener(this);
        radioBtnSlot.setChecked(true);

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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

    public void getSlotType() {
        showLoadingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSlotType());
    }

    private void getDeviceInfo() {
        validParams.reset();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
        orderTasks.add(OrderTaskAssembler.getDeviceName());
        orderTasks.add(OrderTaskAssembler.getConnectable());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getProductDate());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getBattery());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                if (mIsClose) {
                    return;
                }
                dismissLoadingProgressDialog();
                dismissLoadingMessageDialog();
                if (MokoSupport.getInstance().isBluetoothOpen() && !isUpgrade) {
                    AlertMessageDialog dialog = new AlertMessageDialog();
                    dialog.setTitle("Dismiss");
                    dialog.setMessage("The device disconnected!");
                    dialog.setConfirm("Reconnect");
                    dialog.setCancel("Exit");
                    dialog.setOnAlertConfirmListener(() -> {
                        MokoSupport.getInstance().connDevice(DeviceInfoActivity.this, mDeviceMac);
                        showLoadingProgressDialog();
                    });
                    dialog.setOnAlertCancelListener(() -> {
                        back();
                    });
                    dialog.show(getSupportFragmentManager());
                }
            }
            if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                dismissLoadingProgressDialog();
                showLoadingMessageDialog();
                tvTitle.postDelayed(() -> {
                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState());
                }, 500);

            }
        });
    }

    private String unLockResponse;

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
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
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissLoadingProgressDialog();
                if (validParams.isEmpty() && validCount < 2) {
                    validCount++;
                    showLoadingProgressDialog();
                    getDeviceInfo();
                } else {
                    validCount = 0;
                }
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
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
                                        validParams.mac = macShow;
                                    }
                                    break;
                                case GET_DEVICE_NAME:
                                    if (value.length >= 4) {
                                        String valueStr = MokoUtils.bytesToHexString(value);
                                        String deviceName = MokoUtils.hex2String(valueStr.substring(8, valueStr.length()));
                                        settingFragment.setDeviceName(deviceName);
                                        mDeviceName = deviceName;
                                        validParams.name = deviceName;
                                    }
                                    break;
                                case GET_CONNECTABLE:
                                    if (value.length >= 5) {
                                        settingFragment.setConnectable(value);
                                        validParams.connectable = MokoUtils.byte2HexString(value[4]);
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
                        validParams.manufacture = "1";
                        break;
                    case deviceModel:
                        deviceFragment.setDeviceModel(value);
                        validParams.productModel = "1";
                        break;
                    case productDate:
                        deviceFragment.setProductDate(value);
                        validParams.manufactureDate = "1";
                        break;
                    case hardwareVersion:
                        deviceFragment.setHardwareVersion(value);
                        validParams.hardwareVersion = "1";
                        break;
                    case firmwareVersion:
                        deviceFragment.setFirmwareVersion(value);
                        validParams.firmwareVersion = "1";
                        break;
                    case softwareVersion:
                        deviceFragment.setSoftwareVersion(value);
                        validParams.softwareVersion = "1";
                        break;
                    case battery:
                        deviceFragment.setBattery(value);
                        validParams.battery = "1";
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
                                dismissLoadingProgressDialog();
                                ToastUtils.showToast(DeviceInfoActivity.this, "Modify successfully!");
                                back();
                            }
                        } else if ("00".equals(valueStr)) {
                            if (!TextUtils.isEmpty(unLockResponse)) {
                                dismissLoadingMessageDialog();
                                unLockResponse = "";
                                ToastUtils.showToast(DeviceInfoActivity.this, "Password error");
                                back();
                            } else {
                                LogModule.i("锁定状态，获取unLock，解锁");
                                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getUnLock());
                            }
                        } else if ("01".equals(valueStr)) {
                            dismissLoadingMessageDialog();
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
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setUnLock(mPassword, value));
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState());
                        }
                        break;
                    case resetDevice:
                        ToastUtils.showToast(DeviceInfoActivity.this, "Reset successfully!");
                        break;
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissLoadingProgressDialog();
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setTitle("Dismiss");
                            dialog.setMessage("The current system of bluetooth is not available!");
                            dialog.setConfirm("OK");
                            dialog.setCancelGone();
                            dialog.setOnAlertConfirmListener(() -> {
                                back();
                            });
                            dialog.show(getSupportFragmentManager());
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
        EventBus.getDefault().unregister(this);
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
                showLoadingProgressDialog();
                getDeviceInfo();
                break;
            case R.id.radioBtn_device:
                showDeviceFragment();
                showLoadingProgressDialog();
                getDeviceInfo();
                break;
        }
    }

    public void setDeviceName(String deviceName) {
        showLoadingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        // setting
        orderTasks.add(OrderTaskAssembler.setDeviceName(deviceName));
        orderTasks.add(OrderTaskAssembler.getDeviceName());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isModifyPassword;

    public void modifyPassword(String password) {
        isModifyPassword = true;
        showLoadingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLockState(password));
    }

    public void resetDevice() {
        showLoadingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice());
    }


    public void setConnectable(boolean isConneacted) {
        showLoadingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        // setting
        orderTasks.add(OrderTaskAssembler.setConnectable(isConneacted));
        orderTasks.add(OrderTaskAssembler.getConnectable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setClose() {
        mIsClose = true;
        showLoadingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
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
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Dismiss");
        dialog.setMessage("The device disconnected!");
        dialog.setConfirm("OK");
        dialog.setCancelGone();
        dialog.setOnAlertConfirmListener(() -> {
            isUpgrade = false;
            back();
        });
        dialog.show(getSupportFragmentManager());
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


    private LoadingDialog mLoadingDialog;

    public void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());

    }

    public void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Verifying..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }
}
