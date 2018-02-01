package com.moko.beaconx.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.beaconx.AppConstants;
import com.moko.beaconx.R;
import com.moko.beaconx.able.ISlotDataAction;
import com.moko.beaconx.service.MokoService;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;
import com.moko.support.entity.SlotData;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends FragmentActivity implements NumberPickerView.OnValueChangeListener {
    public MokoService mMokoService;
    @Bind(R.id.tv_slot_title)
    TextView tvSlotTitle;
    @Bind(R.id.iv_save)
    ImageView ivSave;
    @Bind(R.id.frame_slot_container)
    FrameLayout frameSlotContainer;
    @Bind(R.id.npv_slot_type)
    NumberPickerView npvSlotType;
    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    public SlotData slotData;
    private ISlotDataAction slotDataActionImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_data);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotData = (SlotData) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_SLOT_DATA);
            LogModule.i(slotData.toString());
        }
        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        fragmentManager = getFragmentManager();
        createFragments();
        npvSlotType.setOnValueChangedListener(this);
        npvSlotType.setMinValue(0);
        npvSlotType.setMaxValue(4);
        npvSlotType.setValue(slotData.frameTypeEnum.ordinal());
        tvSlotTitle.setText(slotData.slotEnum.getTitle());
        showFragment(slotData.frameTypeEnum.ordinal());
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        uidFragment = UidFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
        urlFragment = UrlFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
        tlmFragment = TlmFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
        iBeaconFragment = IBeaconFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
        fragmentTransaction.commit();
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
            filter.setPriority(300);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            if (intent != null) {
                String action = intent.getAction();
                if (MokoConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    ToastUtils.showToast(SlotDataActivity.this, "Disconnected");
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                }
                if (MokoConstants.ACTION_RESPONSE_FINISH.equals(action)) {
                    ToastUtils.showToast(SlotDataActivity.this, "Successfully configure");
                    dismissSyncProgressDialog();
                    SlotDataActivity.this.setResult(SlotDataActivity.this.RESULT_OK);
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderType orderType = response.orderType;
                    byte[] value = response.responseValue;
                    switch (orderType) {
                        case advInterval:
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
                                // 设备上锁
                                ToastUtils.showToast(SlotDataActivity.this, "Locked");
                                finish();
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            // 蓝牙断开
//                            ToastUtils.showToast(SlotDataActivity.this, "Disconnected");
//                            SlotDataActivity.this.finish();
                            break;

                    }
                }
            }
        }
    };

    @Override
    public void finish() {
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @OnClick({R.id.tv_back, R.id.iv_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.iv_save:
                if (slotDataActionImpl == null) {
                    byte[] noData = new byte[]{0};
                    mMokoService.sendOrder(
                            // 切换通道，保证通道是在当前设置通道里
                            mMokoService.setSlot(slotData.slotEnum),
                            mMokoService.setSlotData(noData)
                    );
                    return;
                }
                if (!slotDataActionImpl.isValid()) {
                    return;
                }
                showSyncingProgressDialog();
                slotDataActionImpl.sendData();
                break;
        }
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        LogModule.i(newVal + "");
        LogModule.i(picker.getContentByCurrValue());
        showFragment(newVal);
    }

    private void showFragment(int newVal) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (newVal) {
            case 0:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).show(tlmFragment).commit();
                slotDataActionImpl = tlmFragment;
                break;
            case 1:
                fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).show(uidFragment).commit();
                slotDataActionImpl = uidFragment;
                break;
            case 2:
                fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).show(urlFragment).commit();
                slotDataActionImpl = urlFragment;
                break;
            case 3:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).show(iBeaconFragment).commit();
                slotDataActionImpl = iBeaconFragment;
                break;
            case 4:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).commit();
                slotDataActionImpl = null;
                break;

        }
        slotData.frameTypeEnum = SlotFrameTypeEnum.fromEnumOrdinal(newVal);
    }
}
