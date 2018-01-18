package com.moko.beaconx.utils;

import android.text.TextUtils;

import com.moko.beaconx.entity.BeaconXDevice;
import com.moko.beaconx.entity.BeaconXTLM;
import com.moko.beaconx.entity.BeaconXUID;
import com.moko.beaconx.entity.BeaconXURL;
import com.moko.beaconx.entity.BeaconXiBeacon;
import com.moko.support.utils.Utils;

import java.util.HashMap;


public class BeaconXParser {
    public static HashMap<String, String> urlScheme = new HashMap<>();
    public static HashMap<String, String> urlExpansion = new HashMap<>();

    static {
        urlScheme.put("00", "http://www.");
        urlScheme.put("01", "https://www.");
        urlScheme.put("02", "http://");
        urlScheme.put("03", "https://");
    }

    static {
        urlExpansion.put("00", ".com/");
        urlExpansion.put("01", ".org/");
        urlExpansion.put("02", ".edu/");
        urlExpansion.put("03", ".net/");
        urlExpansion.put("04", ".info/");
        urlExpansion.put("05", ".biz/");
        urlExpansion.put("06", ".gov/");
        urlExpansion.put("07", ".com");
        urlExpansion.put("08", ".org");
        urlExpansion.put("09", ".edu");
        urlExpansion.put("0a", ".net");
        urlExpansion.put("0b", ".info");
        urlExpansion.put("0c", ".biz");
        urlExpansion.put("0d", ".gov");
    }

    public static BeaconXUID getUID(String data) {
        BeaconXUID uid = new BeaconXUID();
        int txPower = Integer.parseInt(data.substring(2, 4), 16);
        uid.rangingData = (txPower > 128 ? txPower - 256 : txPower) + "";
        uid.namespace = data.substring(4, 24);
        uid.instanceId = data.substring(24, 36);
        return uid;
    }

    public static BeaconXURL getURL(String data) {
        BeaconXURL url = new BeaconXURL();
        int txPower = Integer.parseInt(data.substring(2, 4), 16);
        url.rangingData = (txPower > 128 ? txPower - 256 : txPower) + "";
        String urlSchemeStr = urlScheme.get(data.substring(4, 6));
        String urlExpansionStr = urlExpansion.get(data.substring(data.length() - 2, data.length()));
        String urlStr;
        if (TextUtils.isEmpty(urlExpansionStr)) {
            urlStr = urlSchemeStr + Utils.hex2String(data.substring(6, data.length()));
        } else {
            urlStr = urlSchemeStr + Utils.hex2String(data.substring(6, data.length() - 2)) + urlExpansionStr;
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
        int seconds = Integer.parseInt(data.substring(20, 28), 16);
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
        iBeacon.rangingData = (txPower > 128 ? txPower - 256 : txPower) + "";
        return iBeacon;
    }

    public static BeaconXDevice getDevice(String data) {
        BeaconXDevice device = new BeaconXDevice();
        device.advInterval = Integer.parseInt(data.substring(0, 2), 16) + "";
        int txPower = Integer.parseInt(data.substring(2, 4), 16);
        device.txPower = (txPower > 128 ? txPower - 256 : txPower) + "";
        device.mac = data.substring(4, 16);
        device.version = Integer.parseInt(data.substring(16, 18), 16) + "";
        device.isConnected = Integer.parseInt(data.substring(18, 20), 16) + "";
        device.battery = Integer.parseInt(data.substring(20, 22), 16) + "";
        device.deviceName = Utils.hex2String(data.substring(22, data.length()));
        return device;
    }
}
