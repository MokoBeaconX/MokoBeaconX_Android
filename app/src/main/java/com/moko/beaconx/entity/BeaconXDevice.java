package com.moko.beaconx.entity;

import java.io.Serializable;


public class BeaconXDevice implements Serializable {
    // 广播间隔
    public String advInterval;
    // 发射功率
    public String txPower;
    public String mac;
    public String version;
    public String isConnected;
    public String battery;
    public String deviceName;


    @Override
    public String toString() {
        return "BeaconXDevice{" +
                "advInterval='" + advInterval + '\'' +
                ", txPower='" + txPower + '\'' +
                ", mac='" + mac + '\'' +
                ", version='" + version + '\'' +
                ", isConnected='" + isConnected + '\'' +
                ", battery='" + battery + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
