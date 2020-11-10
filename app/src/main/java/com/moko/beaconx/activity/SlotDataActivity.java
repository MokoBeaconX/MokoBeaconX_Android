package com.moko.beaconx.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.beaconx.AppConstants;
import com.moko.beaconx.R;
import com.moko.beaconx.able.ISlotDataAction;
import com.moko.beaconx.dialog.LoadingDialog;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderType;
import com.moko.support.entity.SlotData;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends FragmentActivity implements NumberPickerView.OnValueChangeListener {
    @BindView(R.id.tv_slot_title)
    TextView tvSlotTitle;
    @BindView(R.id.iv_save)
    ImageView ivSave;
    @BindView(R.id.frame_slot_container)
    FrameLayout frameSlotContainer;
    @BindView(R.id.npv_slot_type)
    NumberPickerView npvSlotType;
    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    public SlotData slotData;
    private ISlotDataAction slotDataActionImpl;
    private HashMap<Integer, Integer> seekBarProgressHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_data);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotData = (SlotData) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_SLOT_DATA);
            LogModule.i(slotData.toString());
        }
        fragmentManager = getFragmentManager();
        createFragments();
        npvSlotType.setOnValueChangedListener(this);
        npvSlotType.setMinValue(0);
        npvSlotType.setMaxValue(4);
        npvSlotType.setValue(slotData.frameTypeEnum.ordinal());
        tvSlotTitle.setText(slotData.slotEnum.getTitle());
        showFragment(slotData.frameTypeEnum.ordinal());
        seekBarProgressHashMap = new HashMap<>();

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
        }
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

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                ToastUtils.showToast(SlotDataActivity.this, "Successfully configure");
                dismissLoadingProgressDialog();
                SlotDataActivity.this.setResult(SlotDataActivity.this.RESULT_OK);
                SlotDataActivity.this.finish();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case advInterval:
                        break;
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case notifyConfig:
                        String valueHexStr = MokoUtils.bytesToHexString(value);
                        if ("eb63000100".equals(valueHexStr.toLowerCase())) {
                            // 设备上锁
                            ToastUtils.showToast(SlotDataActivity.this, "Locked");
                            SlotDataActivity.this.finish();
                        }
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
                            // 蓝牙断开
                            SlotDataActivity.this.finish();
                            break;

                    }
                }
            }
        }
    };

    @Override
    public void finish() {
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private LoadingDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
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
                    List<OrderTask> orderTasks = new ArrayList<>();
                    orderTasks.add(OrderTaskAssembler.setSlot(slotData.slotEnum));
                    orderTasks.add(OrderTaskAssembler.setSlotData(noData));
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                    return;
                }
                if (!slotDataActionImpl.isValid()) {
                    return;
                }
                showLoadingProgressDialog();
                slotDataActionImpl.sendData();
                break;
        }
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        LogModule.i(newVal + "");
        LogModule.i(picker.getContentByCurrValue());
        showFragment(newVal);
        if (!seekBarProgressHashMap.isEmpty() && slotDataActionImpl != null) {
            for (int key : seekBarProgressHashMap.keySet()) {
                slotDataActionImpl.upgdateProgress(key, seekBarProgressHashMap.get(key));
            }
        }
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


    public void onProgressChanged(int viewId, int progress) {
        seekBarProgressHashMap.put(viewId, progress);
    }
}
