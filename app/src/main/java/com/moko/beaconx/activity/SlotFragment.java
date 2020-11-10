package com.moko.beaconx.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.beaconx.AppConstants;
import com.moko.beaconx.R;
import com.moko.beaconx.utils.BeaconXParser;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.SlotData;
import com.moko.support.entity.SlotEnum;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.task.OrderTask;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SlotFragment extends Fragment {

    private static final String TAG = "SlotFragment";
    @BindView(R.id.iv_slot1)
    ImageView ivSlot1;
    @BindView(R.id.tv_slot1)
    TextView tvSlot1;
    @BindView(R.id.rl_slot1)
    RelativeLayout rlSlot1;
    @BindView(R.id.iv_slot2)
    ImageView ivSlot2;
    @BindView(R.id.tv_slot2)
    TextView tvSlot2;
    @BindView(R.id.rl_slot2)
    RelativeLayout rlSlot2;
    @BindView(R.id.iv_slot3)
    ImageView ivSlot3;
    @BindView(R.id.tv_slot3)
    TextView tvSlot3;
    @BindView(R.id.rl_slot3)
    RelativeLayout rlSlot3;
    @BindView(R.id.iv_slot4)
    ImageView ivSlot4;
    @BindView(R.id.tv_slot4)
    TextView tvSlot4;
    @BindView(R.id.rl_slot4)
    RelativeLayout rlSlot4;
    @BindView(R.id.iv_slot5)
    ImageView ivSlot5;
    @BindView(R.id.tv_slot5)
    TextView tvSlot5;
    @BindView(R.id.rl_slot5)
    RelativeLayout rlSlot5;

    private DeviceInfoActivity activity;
    private SlotData slotData;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        SlotFragment fragment = new SlotFragment();
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
        View view = inflater.inflate(R.layout.fragment_slot, container, false);
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

    @OnClick({R.id.rl_slot1, R.id.rl_slot2, R.id.rl_slot3, R.id.rl_slot4, R.id.rl_slot5})
    public void onViewClicked(View view) {
        slotData = new SlotData();
        SlotFrameTypeEnum frameType = (SlotFrameTypeEnum) view.getTag();
        slotData.frameTypeEnum = frameType;
        // NO DATA直接跳转
        switch (view.getId()) {
            case R.id.rl_slot1:
                createData(frameType, SlotEnum.SLOT_1);
                break;
            case R.id.rl_slot2:
                createData(frameType, SlotEnum.SLOT_2);
                break;
            case R.id.rl_slot3:
                createData(frameType, SlotEnum.SLOT_3);
                break;
            case R.id.rl_slot4:
                createData(frameType, SlotEnum.SLOT_4);
                break;
            case R.id.rl_slot5:
                createData(frameType, SlotEnum.SLOT_5);
                break;
        }
    }

    private void createData(SlotFrameTypeEnum frameType, SlotEnum slot) {
        slotData.slotEnum = slot;
        switch (frameType) {
            case NO_DATA:
                Intent intent = new Intent(getActivity(), SlotDataActivity.class);
                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
                break;
            case IBEACON:
                getiBeaconData(slot);
                break;
            case TLM:
            case URL:
            case UID:
                getEddystoneData(slot);
                break;
        }
    }

    private void getEddystoneData(SlotEnum slotEnum) {
        activity.showLoadingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(slotEnum));
        orderTasks.add(OrderTaskAssembler.getSlotData());
        orderTasks.add(OrderTaskAssembler.getRadioTxPower());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void getiBeaconData(SlotEnum slotEnum) {
        activity.showLoadingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(slotEnum));
        orderTasks.add(OrderTaskAssembler.getiBeaconUUID());
        orderTasks.add(OrderTaskAssembler.getiBeaconInfo());
        orderTasks.add(OrderTaskAssembler.getRadioTxPower());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    // eb 61 00 05 70 50 70 10 70
    public void updateSlotType(byte[] value) {
        changeView((int) value[4] & 0xff, tvSlot1, ivSlot1, rlSlot1);
        changeView((int) value[5] & 0xff, tvSlot2, ivSlot2, rlSlot2);
        changeView((int) value[6] & 0xff, tvSlot3, ivSlot3, rlSlot3);
        changeView((int) value[7] & 0xff, tvSlot4, ivSlot4, rlSlot4);
        changeView((int) value[8] & 0xff, tvSlot5, ivSlot5, rlSlot5);
    }

    private void changeView(int frameType, TextView tvSlot, ImageView ivSlot, RelativeLayout rlSlot) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum == null) {
            return;
        }
        switch (slotFrameTypeEnum) {
            case UID:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case URL:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case TLM:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case IBEACON:
                ivSlot.setImageResource(R.drawable.ibeacon_icon);
                break;
            case NO_DATA:
                ivSlot.setImageResource(R.drawable.no_data_icon);
                break;

        }
        tvSlot.setText(slotFrameTypeEnum.getShowName());
        rlSlot.setTag(slotFrameTypeEnum);
    }

    private String iBeaconUUID;
    private String major;
    private String minor;
    private int rssi_1m;
    private int txPower;
    private int advInterval;

    // eb640010e2c56db5dffb48d2b060d0f5a71096e0
    public void setiBeaconUUID(byte[] value) {
        String valueHex = MokoUtils.bytesToHexString(value);
        iBeaconUUID = valueHex.substring(8);
        slotData.iBeaconUUID = iBeaconUUID;
    }

    // eb6600050000000000
    public void setiBeaconInfo(byte[] value) {
        String valueHex = MokoUtils.bytesToHexString(value);
        major = valueHex.substring(8, 12);
        minor = valueHex.substring(12, 16);
        rssi_1m = Integer.parseInt(valueHex.substring(16), 16);
        slotData.major = major;
        slotData.minor = minor;
        slotData.rssi_1m = 0 - rssi_1m;
    }

    // 00
    public void setTxPower(byte[] value) {
        txPower = value[0];
        slotData.txPower = txPower;
    }

    // 0064
    public void setAdvInterval(byte[] value) {
        advInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
        slotData.advInterval = advInterval;
        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == AppConstants.REQUEST_CODE_SLOT_DATA) {
                Log.i(TAG, "onActivityResult: ");
                activity.getSlotType();
            }
        }
    }

    // 不同类型的数据长度不同
    public void setSlotData(byte[] value) {
        int frameType = value[0];
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            switch (slotFrameTypeEnum) {
                case URL:
                    // URL：10cf014c6f766500
                    BeaconXParser.parseUrlData(slotData, value);
                    break;
                case TLM:
                    break;
                case UID:
                    BeaconXParser.parseUidData(slotData, value);
                    break;
            }
        }
    }
}
