package com.moko.beaconx.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconx.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Date 2018/1/18
 * @Author wenzheng.liu
 * @Description 扫描筛选对话框
 * @ClassPath com.moko.beaconx.dialog.ScanFilterDialog
 */
public class ScanFilterDialog extends BaseDialog {
    @BindView(R.id.et_filter_name)
    EditText etFilterName;
    @BindView(R.id.tv_rssi)
    TextView tvRssi;
    @BindView(R.id.sb_rssi)
    SeekBar sbRssi;

    private int filterRssi;
    private String filterName;

    public ScanFilterDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_scan_filter;
    }

    @Override
    protected void renderConvertView(View convertView, Object o) {
        tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = progress - 127;
                tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbRssi.setProgress(filterRssi + 127);
        if (!TextUtils.isEmpty(filterName)) {
            etFilterName.setText(filterName);
            etFilterName.setSelection(filterName.length());
        }
        setDismissEnable(true);
    }

    @OnClick({R.id.iv_filter_delete, R.id.tv_done})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_filter_delete:
                etFilterName.setText("");
                break;
            case R.id.tv_done:
                listener.onDone(etFilterName.getText().toString(), filterRssi);
                dismiss();
                break;
        }
    }

    private OnScanFilterListener listener;

    public void setOnScanFilterListener(OnScanFilterListener listener) {
        this.listener = listener;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterRssi(int filterRssi) {
        this.filterRssi = filterRssi;
    }

    public interface OnScanFilterListener {
        void onDone(String filterName, int filterRssi);
    }
}
