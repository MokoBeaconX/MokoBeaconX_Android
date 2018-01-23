package com.moko.support.entity;

import android.bluetooth.BluetoothGattCharacteristic;

import com.moko.support.utils.MokoUtils;

import java.io.Serializable;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.entity.MokoCharacteristic
 */
public class MokoCharacteristic implements Serializable {
    public BluetoothGattCharacteristic characteristic;
    public String charPropertie;
    public OrderType orderType;

    public MokoCharacteristic(BluetoothGattCharacteristic characteristic, String charPropertie, OrderType orderType) {
        this.characteristic = characteristic;
        this.charPropertie = charPropertie;
        this.orderType = orderType;
    }

    public MokoCharacteristic(BluetoothGattCharacteristic characteristic, OrderType orderType) {
        this(characteristic, MokoUtils.getCharPropertie(characteristic.getProperties()), orderType);
    }
}
