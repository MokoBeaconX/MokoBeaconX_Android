package com.moko.beaconx.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.beaconx.able.ISlotDataAction;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.entity.TxPowerEnum;
import com.moko.support.task.OrderTask;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TlmFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "TlmFragment";

    @BindView(R.id.sb_adv_interval)
    SeekBar sbAdvInterval;
    @BindView(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R.id.tv_adv_interval)
    TextView tvAdvInterval;
    @BindView(R.id.tv_tx_power)
    TextView tvTxPower;

    private SlotDataActivity activity;

    public TlmFragment() {
    }

    public static TlmFragment newInstance() {
        TlmFragment fragment = new TlmFragment();
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
        View view = inflater.inflate(R.layout.fragment_tlm, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbAdvInterval.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        setValue();
        return view;
    }

    private void setValue() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            sbAdvInterval.setProgress(9);
            sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100 - 1;
            sbAdvInterval.setProgress(advIntervalProgress);
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);
            tvAdvInterval.setText(String.format("%dms", activity.slotData.advInterval));

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
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

    private byte[] advIntervalBytes;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
            upgdateData(seekBar.getId(), progress);
            activity.onProgressChanged(seekBar.getId(), progress);
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            upgdateData(seekBar.getId(), progress);
        }
    }

    private void upgdateData(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_adv_interval:
                int advInterval = (progress + 1) * 100;
                tvAdvInterval.setText(String.format("%dms", advInterval));
                advIntervalBytes = MokoUtils.toByteArray(advInterval, 2);
                break;
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                int txPower = txPowerEnum.getTxPower();
                tvTxPower.setText(String.format("%ddBm", txPower));
                txPowerBytes = MokoUtils.toByteArray(txPower, 1);
                break;
        }
    }

    @Override
    public void upgdateProgress(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_adv_interval:
                sbAdvInterval.setProgress(progress);
                break;
            case R.id.sb_tx_power:
                sbTxPower.setProgress(progress);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void sendData() {
        byte[] tlmBytes = MokoUtils.hex2bytes(SlotFrameTypeEnum.TLM.getFrameType());
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(activity.slotData.slotEnum));
        orderTasks.add(OrderTaskAssembler.setSlotData(tlmBytes));
        orderTasks.add(OrderTaskAssembler.setRadioTxPower(txPowerBytes));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(advIntervalBytes));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
