package com.moko.support.entity;

import java.io.Serializable;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

/**
 * @Date 2017/12/21
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.support.entity.DeviceInfo
 */
public class DeviceInfo implements Serializable {
    public String name;
    public int rssi;
    public String mac;
    public ScanRecord scanRecord;

    @Override
    public String toString() {
        return "BeaconInfo{" +
                "name='" + name + '\'' +
                ", rssi=" + rssi +
                ", mac='" + mac + '\'' +
                ", scanRecord='" + scanRecord + '\'' +
                '}';
    }
}
