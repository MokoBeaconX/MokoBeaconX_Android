package com.moko.beaconx.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.beaconx.able.ISlotDataAction;
import com.moko.beaconx.dialog.UrlSchemeDialog;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.entity.TxPowerEnum;
import com.moko.support.entity.UrlExpansionEnum;
import com.moko.support.entity.UrlSchemeEnum;
import com.moko.support.utils.MokoUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UrlFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "UrlFragment";
    @Bind(R.id.et_url)
    EditText etUrl;
    @Bind(R.id.sb_adv_interval)
    SeekBar sbAdvInterval;
    @Bind(R.id.sb_adv_tx_power)
    SeekBar sbAdvTxPower;
    @Bind(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @Bind(R.id.tv_url_scheme)
    TextView tvUrlScheme;
    @Bind(R.id.tv_adv_interval)
    TextView tvAdvInterval;
    @Bind(R.id.tv_adv_tx_power)
    TextView tvAdvTxPower;
    @Bind(R.id.tv_tx_power)
    TextView tvTxPower;


    private SlotDataActivity activity;

    public UrlFragment() {
    }

    public static UrlFragment newInstance() {
        UrlFragment fragment = new UrlFragment();
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
        View view = inflater.inflate(R.layout.fragment_url, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbAdvInterval.setOnSeekBarChangeListener(this);
        sbAdvTxPower.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        setDefault();
        return view;
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            sbAdvInterval.setProgress(9);
            sbAdvTxPower.setProgress(118);
            sbTxPower.setProgress(5);
            mUrlSchemeHex = MokoUtils.int2HexString(UrlSchemeEnum.HTTP_WWW.getUrlType());
            tvUrlScheme.setText(UrlSchemeEnum.HTTP_WWW.getUrlDesc());
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100 - 1;
            sbAdvInterval.setProgress(advIntervalProgress);
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);
            tvAdvInterval.setText(String.format("%dms", activity.slotData.advInterval));

            int advTxPowerProgress = activity.slotData.rssi_0m + 127;
            sbAdvTxPower.setProgress(advTxPowerProgress);
            advTxPowerBytes = MokoUtils.toByteArray(activity.slotData.rssi_0m, 1);
            tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_0m));

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.URL) {
            mUrlSchemeHex = MokoUtils.int2HexString(activity.slotData.urlSchemeEnum.getUrlType());
            tvUrlScheme.setText(activity.slotData.urlSchemeEnum.getUrlDesc());
            String url = activity.slotData.urlContent;
            String urlExpansionStr = url.substring(url.length() - 2);
            int urlExpansionType = Integer.parseInt(urlExpansionStr, 16);
            UrlExpansionEnum urlEnum = UrlExpansionEnum.fromUrlExpanType(urlExpansionType);
            if (urlEnum == null) {
                etUrl.setText(MokoUtils.hex2String(url));
            } else {
                etUrl.setText(MokoUtils.hex2String(url.substring(0, url.length() - 2)) + urlEnum.getUrlExpanDesc());
            }
            etUrl.setSelection(etUrl.getText().toString().length());
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
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private byte[] advIntervalBytes;
    private byte[] advTxPowerBytes;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.URL) {
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
                sbAdvInterval.setProgress(progress);
                break;
            case R.id.sb_adv_tx_power:
                int advTxPower = progress - 127;
                tvAdvTxPower.setText(String.format("%ddBm", advTxPower));
                advTxPowerBytes = MokoUtils.toByteArray(advTxPower, 1);
                sbAdvTxPower.setProgress(progress);
                break;
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                int txPower = txPowerEnum.getTxPower();
                tvTxPower.setText(String.format("%ddBm", txPower));
                txPowerBytes = MokoUtils.toByteArray(txPower, 1);
                sbTxPower.setProgress(progress);
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

    private String mUrlSchemeHex;

    @OnClick(R.id.tv_url_scheme)
    public void onViewClicked() {
        UrlSchemeDialog dialog = new UrlSchemeDialog(getActivity());
        dialog.setData(tvUrlScheme.getText().toString());
        dialog.setUrlSchemeClickListener(new UrlSchemeDialog.UrlSchemeClickListener() {
            @Override
            public void onEnsureClicked(String urlType) {
                UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlType(Integer.valueOf(urlType));
                tvUrlScheme.setText(urlSchemeEnum.getUrlDesc());
                mUrlSchemeHex = MokoUtils.int2HexString(Integer.valueOf(urlType));
            }
        });
        dialog.show();
    }

    private byte[] urlParamsBytes;

    @Override
    public boolean isValid() {
        String urlContent = etUrl.getText().toString();
        if (TextUtils.isEmpty(urlContent) || TextUtils.isEmpty(mUrlSchemeHex)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        String urlContentHex;
        if (urlContent.indexOf(".") >= 0) {
            String urlExpansion = urlContent.substring(urlContent.lastIndexOf("."));
            UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanDesc(urlExpansion);
            if (urlExpansionEnum == null) {
                // url中有点，但不符合eddystone结尾格式，内容长度不能超过17个字符
                if (urlContent.length() < 2 || urlContent.length() > 17) {
                    ToastUtils.showToast(activity, "Data format incorrect!");
                    return false;
                }
                urlContentHex = MokoUtils.string2Hex(urlContent);
            } else {
                String content = urlContent.substring(0, urlContent.lastIndexOf("."));
                if (content.length() < 1 || content.length() > 16) {
                    ToastUtils.showToast(activity, "Data format incorrect!");
                    return false;
                }
                urlContentHex = MokoUtils.string2Hex(urlContent.substring(0, urlContent.lastIndexOf("."))) + MokoUtils.byte2HexString((byte) urlExpansionEnum.getUrlExpanType());
            }
        } else {
            // url中没有有点，内容长度不能超过17个字符
            if (urlContent.length() < 2 || urlContent.length() > 17) {
                ToastUtils.showToast(activity, "Data format incorrect!");
                return false;
            }
            urlContentHex = MokoUtils.string2Hex(urlContent);
        }
        String urlParamsHex = activity.slotData.frameTypeEnum.getFrameType() + mUrlSchemeHex + urlContentHex;
        urlParamsBytes = MokoUtils.hex2bytes(urlParamsHex);
        return true;
    }

    @Override
    public void sendData() {
        activity.mMokoService.sendOrder(
                // 切换通道，保证通道是在当前设置通道里
                activity.mMokoService.setSlot(activity.slotData.slotEnum),
                activity.mMokoService.setSlotData(urlParamsBytes),
                activity.mMokoService.setRadioTxPower(txPowerBytes),
                activity.mMokoService.setAdvTxPower(advTxPowerBytes),
                activity.mMokoService.setAdvInterval(advIntervalBytes)
        );
    }
}
