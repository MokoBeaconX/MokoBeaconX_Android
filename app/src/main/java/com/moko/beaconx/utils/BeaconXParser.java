package com.moko.beaconx.utils;

import android.text.TextUtils;

import com.moko.beaconx.entity.BeaconXDevice;
import com.moko.beaconx.entity.BeaconXTLM;
import com.moko.beaconx.entity.BeaconXUID;
import com.moko.beaconx.entity.BeaconXURL;
import com.moko.beaconx.entity.BeaconXiBeacon;
import com.moko.support.entity.SlotData;
import com.moko.support.entity.UrlExpansionEnum;
import com.moko.support.entity.UrlSchemeEnum;
import com.moko.support.utils.MokoUtils;


public class BeaconXParser {

    public static BeaconXUID getUID(String data) {
        BeaconXUID uid = new BeaconXUID();
        int txPower = Integer.parseInt(data.substring(2, 4), 16);
        uid.rangingData = (byte) txPower + "";
        uid.namespace = data.substring(4, 24);
        uid.instanceId = data.substring(24, 36);
        return uid;
    }

    public static BeaconXURL getURL(String data) {
        BeaconXURL url = new BeaconXURL();
        int txPower = Integer.parseInt(data.substring(2, 4), 16);
        url.rangingData = (byte) txPower + "";

        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlType(Integer.parseInt(data.substring(4, 6), 16));
        String urlSchemeStr = "";
        if (urlSchemeEnum != null) {
            urlSchemeStr = urlSchemeEnum.getUrlDesc();
        }
        String urlExpansionStr = "";
        UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanType(Integer.parseInt(data.substring(data.length() - 2), 16));
        if (urlExpansionEnum != null) {
            urlExpansionStr = urlExpansionEnum.getUrlExpanDesc();
        }
        String urlStr;
        if (TextUtils.isEmpty(urlExpansionStr)) {
            urlStr = urlSchemeStr + MokoUtils.hex2String(data.substring(6));
        } else {
            urlStr = urlSchemeStr + MokoUtils.hex2String(data.substring(6, data.length() - 2)) + urlExpansionStr;
        }
        url.url = urlStr;
        return url;
    }

    public static BeaconXTLM getTLM(String data) {
        BeaconXTLM tlm = new BeaconXTLM();
        tlm.vbatt = Integer.parseInt(data.substring(4, 8), 16) + "";
        String temp1 = Integer.parseInt(data.substring(8, 10), 16) + "";
        String temp2 = Integer.parseInt(data.substring(10, 12), 16) + "";
        tlm.temp = String.format("%s.%s°C", temp1, temp2);
        tlm.adv_cnt = Integer.parseInt(data.substring(12, 20), 16) + "";
        int seconds = Integer.parseInt(data.substring(20, 28), 16) / 10;
        int day = 0, hours = 0, minutes = 0;
        day = seconds / (60 * 60 * 24);
        seconds -= day * 60 * 60 * 24;
        hours = seconds / (60 * 60);
        seconds -= hours * 60 * 60;
        minutes = seconds / 60;
        seconds -= minutes * 60;
        tlm.sec_cnt = String.format("%dD%dh%dm%ds", day, hours, minutes, seconds);
        return tlm;
    }

    public static BeaconXiBeacon getiBeacon(String data) {
        BeaconXiBeacon iBeacon = new BeaconXiBeacon();
        iBeacon.uuid = data.substring(0, 32);
        iBeacon.major = Integer.parseInt(data.substring(32, 36), 16) + "";
        iBeacon.minor = Integer.parseInt(data.substring(36, 40), 16) + "";
        int txPower = Integer.parseInt(data.substring(40, 42), 16);
        iBeacon.rangingData = (byte) txPower + "";
        return iBeacon;
    }

    public static BeaconXDevice getDevice(String data) {
        BeaconXDevice device = new BeaconXDevice();
        device.advInterval = Integer.parseInt(data.substring(0, 2), 16) + "";
        int txPower = Integer.parseInt(data.substring(2, 4), 16);
        device.txPower = (byte) txPower + "";
        device.mac = data.substring(4, 16);
        device.version = Integer.parseInt(data.substring(16, 18), 16) + "";
        device.isConnected = Integer.parseInt(data.substring(18, 20), 16) + "";
        device.battery = Integer.parseInt(data.substring(20, 22), 16) + "";
        device.deviceName = MokoUtils.hex2String(data.substring(22, data.length()));
        return device;
    }

    public static void parseUrlData(SlotData slotData, byte[] value) {
        if (value.length > 3) {
            int rssi_0m = value[1];
            int urlType = (int) value[2] & 0xff;
            slotData.rssi_0m = rssi_0m;
            slotData.urlSchemeEnum = UrlSchemeEnum.fromUrlType(urlType);
            slotData.urlContent = MokoUtils.bytesToHexString(value).substring(6);
        }
    }

    public static void parseUidData(SlotData slotData, byte[] value) {
        if (value.length >= 18) {
            int rssi_0m = value[1];
            slotData.rssi_0m = rssi_0m;
            slotData.namespace = MokoUtils.bytesToHexString(value).substring(4, 24);
            slotData.instanceId = MokoUtils.bytesToHexString(value).substring(24);
        }
    }
}
