package com.moko.beaconx.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.support.utils.MokoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceFragment extends Fragment {

    private static final String TAG = "DeviceFragment";
    @BindView(R.id.tv_soc)
    TextView tvSoc;
    @BindView(R.id.tv_mac_address)
    TextView tvMacAddress;
    @BindView(R.id.tv_product_date)
    TextView tvProductDate;
    @BindView(R.id.tv_device_model)
    TextView tvDeviceModel;
    @BindView(R.id.tv_software_version)
    TextView tvSoftwareVersion;
    @BindView(R.id.tv_hardware_version)
    TextView tvHardwareVersion;
    @BindView(R.id.tv_manufacturer)
    TextView tvManufacturer;
    @BindView(R.id.tv_firmware_version)
    TextView tvFirmwareVersion;


    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
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
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        ButterKnife.bind(this, view);
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

    public void setDeviceMac(String macShow) {
        tvMacAddress.setText(macShow);
    }

    public void setManufacturer(byte[] value) {
        String manufacturer = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
        tvManufacturer.setText(manufacturer);
    }

    public void setDeviceModel(byte[] value) {
        String deviceModel = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
        tvDeviceModel.setText(deviceModel);
    }

    public void setProductDate(byte[] value) {
        String productDate = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
        tvProductDate.setText(productDate);
    }

    public void setHardwareVersion(byte[] value) {
        String hardwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
        tvHardwareVersion.setText(hardwareVersion);
    }

    public void setFirmwareVersion(byte[] value) {
        String firmwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
        tvFirmwareVersion.setText(firmwareVersion);
    }

    public void setSoftwareVersion(byte[] value) {
        String softwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
        tvSoftwareVersion.setText(softwareVersion);
    }

    public void setBattery(byte[] value) {
        String battery = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "%";
        tvSoc.setText(battery);
    }
}
