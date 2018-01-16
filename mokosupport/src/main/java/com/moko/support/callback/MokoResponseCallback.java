package com.moko.support.callback;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * @Date 2017/12/12 0012
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.callback.MokoResponseCallback
 */
public interface MokoResponseCallback {

    void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value);

    void onCharacteristicWrite(byte[] value);

    void onCharacteristicRead(byte[] value);

    void onDescriptorWrite();
}
