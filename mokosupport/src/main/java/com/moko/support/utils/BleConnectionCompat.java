package com.moko.support.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Build;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class BleConnectionCompat {

    private final Context context;

    public BleConnectionCompat(Context context) {
        this.context = context;
    }

    public BluetoothGatt connectGatt(BluetoothDevice remoteDevice, boolean autoConnect, BluetoothGattCallback bluetoothGattCallback) {
        if (remoteDevice == null) {
            return null;
        }
        return connectGattCompat(bluetoothGattCallback, remoteDevice, autoConnect);
    }

    private BluetoothGatt connectGattCompat(BluetoothGattCallback bluetoothGattCallback, BluetoothDevice device, boolean autoConnect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return device.connectGatt(context, autoConnect, bluetoothGattCallback, TRANSPORT_LE);
        } else {
            return device.connectGatt(context, autoConnect, bluetoothGattCallback);
        }
    }
}