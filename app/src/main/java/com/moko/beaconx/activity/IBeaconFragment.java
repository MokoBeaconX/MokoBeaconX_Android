package com.moko.beaconx.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.beaconx.able.ISlotDataAction;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.entity.TxPowerEnum;
import com.moko.support.task.OrderTask;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IBeaconFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "IBeaconFragment";

    public static final String UUID_PATTERN = "[A-Fa-f0-9]{8}-(?:[A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12}";

    @BindView(R.id.sb_adv_interval)
    SeekBar sbAdvInterval;
    @BindView(R.id.sb_adv_tx_power)
    SeekBar sbAdvTxPower;
    @BindView(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R.id.et_major)
    EditText etMajor;
    @BindView(R.id.et_minor)
    EditText etMinor;
    @BindView(R.id.et_uuid)
    EditText etUuid;
    @BindView(R.id.tv_adv_interval)
    TextView tvAdvInterval;
    @BindView(R.id.tv_adv_tx_power)
    TextView tvAdvTxPower;
    @BindView(R.id.tv_tx_power)
    TextView tvTxPower;
    private Pattern pattern;

    private SlotDataActivity activity;

    public IBeaconFragment() {
    }

    public static IBeaconFragment newInstance() {
        IBeaconFragment fragment = new IBeaconFragment();
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
        View view = inflater.inflate(R.layout.fragment_ibeacon, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbAdvInterval.setOnSeekBarChangeListener(this);
        sbAdvTxPower.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        pattern = Pattern.compile(UUID_PATTERN);
        //限制只输入大写，自动小写转大写
        etUuid.setTransformationMethod(new A2bigA());
        etUuid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().toUpperCase();
                if (!pattern.matcher(input).matches()) {
                    if (input.length() == 9 && !input.endsWith("-")) {
                        String show = input.substring(0, 8) + "-" + input.substring(8, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                    if (input.length() == 14 && !input.endsWith("-")) {
                        String show = input.substring(0, 13) + "-" + input.substring(13, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                    if (input.length() == 19 && !input.endsWith("-")) {
                        String show = input.substring(0, 18) + "-" + input.substring(18, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                    if (input.length() == 24 && !input.endsWith("-")) {
                        String show = input.substring(0, 23) + "-" + input.substring(23, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                }
            }
        });
        setDefault();
        return view;
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            sbAdvInterval.setProgress(9);
            sbAdvTxPower.setProgress(68);
            sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100 - 1;
            sbAdvInterval.setProgress(advIntervalProgress);
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);
            tvAdvInterval.setText(String.format("%dms", activity.slotData.advInterval));

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
                int advTxPowerProgress = activity.slotData.rssi_1m + 127;
                sbAdvTxPower.setProgress(advTxPowerProgress);
                advTxPower = activity.slotData.rssi_1m;
                tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_1m));
            } else if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                sbAdvTxPower.setProgress(68);
                advTxPower = -59;
                tvAdvTxPower.setText(String.format("%ddBm", -59));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 127;
                sbAdvTxPower.setProgress(advTxPowerProgress);
                advTxPower = activity.slotData.rssi_0m;
                tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_0m));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
            etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            StringBuilder stringBuilder = new StringBuilder(activity.slotData.iBeaconUUID);
            stringBuilder.insert(8, "-");
            stringBuilder.insert(13, "-");
            stringBuilder.insert(18, "-");
            stringBuilder.insert(23, "-");
            etUuid.setText(stringBuilder.toString().toUpperCase());
            etMajor.setSelection(etMajor.getText().toString().length());
            etMinor.setSelection(etMinor.getText().toString().length());
            etUuid.setSelection(etUuid.getText().toString().length());
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
    private int advTxPower;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
            upgdateData(seekBar.getId(), progress);
            activity.onProgressChanged(seekBar.getId(), progress);
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            upgdateData(seekBar.getId(), progress);
        }
    }


    public void upgdateData(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_adv_interval:
                int advInterval = (progress + 1) * 100;
                tvAdvInterval.setText(String.format("%dms", advInterval));
                advIntervalBytes = MokoUtils.toByteArray(advInterval, 2);
                break;
            case R.id.sb_adv_tx_power:
                int advTxPower = progress - 127;
                tvAdvTxPower.setText(String.format("%ddBm", advTxPower));
                this.advTxPower = advTxPower;
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
            case R.id.sb_adv_tx_power:
                sbAdvTxPower.setProgress(progress);
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

    private int major;
    private int minor;
    private String uuidHex;

    @Override
    public boolean isValid() {
        String majorStr = etMajor.getText().toString();
        String minorStr = etMinor.getText().toString();
        String uuidStr = etUuid.getText().toString().replaceAll("-", "");
        if (TextUtils.isEmpty(majorStr) || TextUtils.isEmpty(minorStr) || TextUtils.isEmpty(uuidStr)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (Integer.valueOf(majorStr) > 65535 || Integer.valueOf(minorStr) > 65535 || uuidStr.length() != 32) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        major = Integer.valueOf(majorStr);
        minor = Integer.valueOf(minorStr);
        uuidHex = uuidStr;
        return true;
    }

    @Override
    public void sendData() {
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(activity.slotData.slotEnum));
        orderTasks.add(OrderTaskAssembler.setiBeaconInfo(major, minor, advTxPower));
        orderTasks.add(OrderTaskAssembler.setiBeaconUUID(uuidHex));
        orderTasks.add(OrderTaskAssembler.setRadioTxPower(txPowerBytes));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(advIntervalBytes));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public class A2bigA extends ReplacementTransformationMethod {

        @Override
        protected char[] getOriginal() {
            char[] aa = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            return aa;
        }

        @Override
        protected char[] getReplacement() {
            char[] cc = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            return cc;
        }

    }
}
