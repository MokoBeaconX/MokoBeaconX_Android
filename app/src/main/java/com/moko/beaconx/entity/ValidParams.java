package com.moko.beaconx.entity;

import android.text.TextUtils;

public class ValidParams {
    public String name;
    public String connectable;
    public String battery;
    public String mac;
    public String manufactureDate;
    public String productModel;
    public String softwareVersion;
    public String firmwareVersion;
    public String hardwareVersion;
    public String manufacture;

    public void reset() {
        name = "";
        connectable = "";
        battery = "";
        mac = "";
        manufactureDate = "";
        productModel = "";
        softwareVersion = "";
        firmwareVersion = "";
        hardwareVersion = "";
        manufacture = "";
    }

    public boolean isEmpty() {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(connectable)
                || TextUtils.isEmpty(battery)
                || TextUtils.isEmpty(mac)
                || TextUtils.isEmpty(manufactureDate)
                || TextUtils.isEmpty(productModel)
                || TextUtils.isEmpty(softwareVersion)
                || TextUtils.isEmpty(firmwareVersion)
                || TextUtils.isEmpty(hardwareVersion)
                || TextUtils.isEmpty(manufacture)) {
            return true;
        }
        return false;
    }
}
