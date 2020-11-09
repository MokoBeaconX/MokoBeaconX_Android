package com.moko.support.handler;

import android.bluetooth.BluetoothDevice;

import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @Date 2017/12/12 0012
 * @Author wenzheng.liu
 * @Description 搜索设备回调类
 * @ClassPath com.moko.support.handler.MokoLeScanHandler
 */
public class MokoLeScanHandler extends ScanCallback {
    private MokoScanDeviceCallback callback;

    public MokoLeScanHandler(MokoScanDeviceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if (result != null) {
            BluetoothDevice device = result.getDevice();
            byte[] scanRecord = result.getScanRecord().getBytes();
            int rssi = result.getRssi();
            if (scanRecord.length == 0 || rssi < -127 || rssi == 127) {
                return;
            }
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.name = result.getScanRecord().getDeviceName();
            deviceInfo.rssi = rssi;
            deviceInfo.mac = device.getAddress();
            deviceInfo.scanRecord = result.getScanRecord();
            callback.onScanDevice(deviceInfo);
        }
    }
}