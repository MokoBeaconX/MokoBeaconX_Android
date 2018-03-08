package com.moko.beaconx.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.beaconx.AppConstants;
import com.moko.beaconx.R;
import com.moko.beaconx.adapter.BeaconXListAdapter;
import com.moko.beaconx.dialog.PasswordDialog;
import com.moko.beaconx.dialog.ScanFilterDialog;
import com.moko.beaconx.entity.BeaconXInfo;
import com.moko.beaconx.service.MokoService;
import com.moko.beaconx.utils.BeaconXInfoParseableImpl;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends Activity implements MokoScanDeviceCallback, BeaconXListAdapter.OnConnectListener {


    @Bind(R.id.iv_refresh)
    ImageView ivRefresh;
    @Bind(R.id.lv_devices)
    ListView lvDevices;
    @Bind(R.id.tv_device_num)
    TextView tvDeviceNum;
    @Bind(R.id.rl_edit_filter)
    RelativeLayout rl_edit_filter;
    @Bind(R.id.rl_filter)
    RelativeLayout rl_filter;
    @Bind(R.id.tv_filter)
    TextView tv_filter;
    private MokoService mMokoService;
    private HashMap<String, BeaconXInfo> beaconXInfoHashMap;
    private ArrayList<BeaconXInfo> beaconXInfos;
    private BeaconXListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Intent intent = new Intent(this, MokoService.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        beaconXInfoHashMap = new HashMap<>();
        beaconXInfos = new ArrayList<>();
        adapter = new BeaconXListAdapter(this);
        adapter.setListener(this);
        adapter.setItems(beaconXInfos);
        lvDevices.setAdapter(adapter);
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
            filter.setPriority(100);
            registerReceiver(mReceiver, filter);
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    private String unLockResponse;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (MokoConstants.ACTION_CONNECT_SUCCESS.equals(action)) {
                    dismissLoadingProgressDialog();
                    showVerifyingProgressDialog();
                    mMokoService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMokoService.sendOrder(mMokoService.setConfigNotify(), mMokoService.getLockState());
                        }
                    }, 1000);
                }
                if (MokoConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    dismissLoadingProgressDialog();
                    dismissVerifyProgressDialog();
                    ToastUtils.showToast(MainActivity.this, "Disconnected");
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                }
                if (MokoConstants.ACTION_RESPONSE_FINISH.equals(action)) {
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderType orderType = response.orderType;
                    int responseType = response.responseType;
                    byte[] value = response.responseValue;
                    switch (orderType) {
                        case lockState:
                            String valueStr = MokoUtils.bytesToHexString(value);
                            if ("00".equals(valueStr)) {
                                if (!TextUtils.isEmpty(unLockResponse)) {
                                    dismissVerifyProgressDialog();
                                    unLockResponse = "";
                                    MokoSupport.getInstance().disConnectBle();
                                    ToastUtils.showToast(MainActivity.this, "Password error");
                                } else {
                                    LogModule.i("锁定状态，获取unLock，解锁");
                                    mMokoService.sendOrder(mMokoService.getUnLock());
                                }
                            } else {
                                dismissVerifyProgressDialog();
                                LogModule.i("解锁成功");
                                unLockResponse = "";
                                Intent deviceInfoIntent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                                deviceInfoIntent.putExtra(AppConstants.EXTRA_KEY_PASSWORD, mPassword);
                                startActivity(deviceInfoIntent);
                            }
                            break;
                        case unLock:
                            if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                                unLockResponse = MokoUtils.bytesToHexString(value);
                                LogModule.i("返回的随机数：" + unLockResponse);
                                mMokoService.sendOrder(mMokoService.setUnLock(mPassword, value));
                            }
                            if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                                mMokoService.sendOrder(mMokoService.getLockState());
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            mMokoService.mHandler.removeMessages(0);
                            mMokoService.stopScanDevice();
                            break;

                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:

                    break;

            }
        } else {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:
                    // 未打开蓝牙
                    MainActivity.this.finish();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }


    @Override
    public void onStartScan() {
    }

    private BeaconXInfoParseableImpl beaconXInfoParseable;

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        final BeaconXInfo beaconXInfo = beaconXInfoParseable.parseDeviceInfo(deviceInfo);
        if (beaconXInfo == null) {
            return;
        }
        beaconXInfoHashMap.put(beaconXInfo.mac, beaconXInfo);
        updateDevices();
    }

    @Override
    public void onStopScan() {
        findViewById(R.id.iv_refresh).clearAnimation();
        animation = null;
        updateDevices();
    }

    private void updateDevices() {
        beaconXInfos.clear();
        if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
            ArrayList<BeaconXInfo> beaconXInfosFilter = new ArrayList<>(beaconXInfoHashMap.values());
            Iterator<BeaconXInfo> iterator = beaconXInfosFilter.iterator();
            while (iterator.hasNext()) {
                BeaconXInfo beaconXInfo = iterator.next();
                if (beaconXInfo.rssi > filterRssi
                        && (TextUtils.isEmpty(filterName)
                        || (!TextUtils.isEmpty(filterName) && (!TextUtils.isEmpty(beaconXInfo.name) && (!TextUtils.isEmpty(beaconXInfo.mac))
                        && (beaconXInfo.name.toLowerCase().contains(filterName.toLowerCase()) || beaconXInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase())))))) {
                    continue;
                } else {
                    iterator.remove();
                }
            }
            beaconXInfos.addAll(beaconXInfosFilter);
        } else {
            beaconXInfos.addAll(beaconXInfoHashMap.values());
        }
        Collections.sort(beaconXInfos, new Comparator<BeaconXInfo>() {
            @Override
            public int compare(BeaconXInfo lhs, BeaconXInfo rhs) {
                if (lhs.rssi > rhs.rssi) {
                    return -1;
                } else if (lhs.rssi < rhs.rssi) {
                    return 1;
                }
                return 0;
            }
        });
        adapter.notifyDataSetChanged();
        tvDeviceNum.setText(String.format("Devices(%d)", beaconXInfos.size()));
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

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new ProgressDialog(MainActivity.this);
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

    private Animation animation = null;
    public String filterName;
    public int filterRssi = -127;

    @OnClick({R.id.iv_refresh, R.id.iv_about, R.id.rl_edit_filter, R.id.rl_filter,R.id.iv_filter_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_refresh:
                if (animation == null) {
                    if (!MokoSupport.getInstance().isBluetoothOpen()) {
                        // 蓝牙未打开，开启蓝牙
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
                        return;
                    }
                    beaconXInfoHashMap.clear();
                    animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                    view.startAnimation(animation);
                    beaconXInfoParseable = new BeaconXInfoParseableImpl();
                    mMokoService.startScanDevice(this);
                    mMokoService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMokoService.stopScanDevice();
                        }
                    }, 1000 * 60);
                } else {
                    mMokoService.mHandler.removeMessages(0);
                    mMokoService.stopScanDevice();
                }
                break;
            case R.id.iv_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.rl_edit_filter:
            case R.id.rl_filter:
                ScanFilterDialog scanFilterDialog = new ScanFilterDialog(this);
                scanFilterDialog.setFilterName(filterName);
                scanFilterDialog.setFilterRssi(filterRssi);
                scanFilterDialog.setOnScanFilterListener(new ScanFilterDialog.OnScanFilterListener() {
                    @Override
                    public void onDone(String filterName, int filterRssi) {
                        MainActivity.this.filterName = filterName;
                        MainActivity.this.filterRssi = filterRssi;
                        if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
                            rl_filter.setVisibility(View.VISIBLE);
                            rl_edit_filter.setVisibility(View.GONE);
                            StringBuilder stringBuilder = new StringBuilder();
                            if (!TextUtils.isEmpty(filterName)) {
                                stringBuilder.append(filterName);
                                stringBuilder.append(";");
                            }
                            if (filterRssi != -127) {
                                stringBuilder.append(String.format("%sdBm", filterRssi + ""));
                                stringBuilder.append(";");
                            }
                            tv_filter.setText(stringBuilder.toString());
                            updateDevices();
                        } else {
                            rl_filter.setVisibility(View.GONE);
                            rl_edit_filter.setVisibility(View.VISIBLE);
                        }
                    }
                });
                scanFilterDialog.show();
                break;
            case R.id.iv_filter_delete:
                rl_filter.setVisibility(View.GONE);
                rl_edit_filter.setVisibility(View.VISIBLE);
                filterName = "";
                filterRssi = -127;
                updateDevices();
                break;
        }
    }

    private String mPassword;

    @Override
    public void onConnectClick(final BeaconXInfo beaconXInfo) {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
            return;
        }
//        if (MokoSupport.getInstance().isConnDevice(this, beaconXInfo.mac)) {
//            mMokoService.mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mMokoService.getReadableData();
//                }
//            }, 1000);
//            return;
//        }
        if (beaconXInfo != null && !isFinishing()) {
            final PasswordDialog dialog = new PasswordDialog(this);
            dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
                @Override
                public void onEnsureClicked(String password) {
                    if (!MokoSupport.getInstance().isBluetoothOpen()) {
                        // 蓝牙未打开，开启蓝牙
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
                        return;
                    }
                    LogModule.i(password);
                    mPassword = password;
                    LogModule.i("选中的设备：" + beaconXInfo.mac);
                    mMokoService.connDevice(beaconXInfo.mac);
                    showLoadingProgressDialog();
                    mMokoService.mHandler.removeMessages(0);
                    mMokoService.stopScanDevice();
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
                    dialog.showKeyboard();
                }
            }, 200);
        }
    }
}
