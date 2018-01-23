package com.moko.beaconx.utils;

import com.moko.beaconx.entity.BeaconXInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.service.DeviceInfoParseable;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @Date 2018/1/16
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconx.utils.BeaconXInfoParseableImpl
 */
public class BeaconXInfoParseableImpl implements DeviceInfoParseable<BeaconXInfo> {
    private HashMap<String, BeaconXInfo> beaconXInfoHashMap = new HashMap<>();

    @Override
    public BeaconXInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        byte[] scanRecord = MokoUtils.hex2bytes(deviceInfo.scanRecord);
        // filter
        boolean isEddystone = false;
        boolean isBeacon = false;
        boolean isDeviceInfo = false;
        int length = 0;
        if (((int) scanRecord[5] & 0xff) == 0xAA && ((int) scanRecord[6] & 0xff) == 0xFE) {
            length = (int) scanRecord[7];
            isEddystone = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x20 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isBeacon = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x10 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isDeviceInfo = true;
        }
        if (!isEddystone && !isBeacon && !isDeviceInfo) {
            return null;
        }
        // avoid repeat
        BeaconXInfo beaconXInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            beaconXInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            beaconXInfo.rssi = deviceInfo.rssi;
        } else {
            beaconXInfo = new BeaconXInfo();
            beaconXInfo.name = deviceInfo.name;
            beaconXInfo.mac = deviceInfo.mac;
            beaconXInfo.rssi = deviceInfo.rssi;
            beaconXInfo.scanRecord = deviceInfo.scanRecord;
            beaconXInfo.validDataHashMap = new HashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, beaconXInfo);
        }
        String data = null;
        if (isBeacon || isDeviceInfo) {
            data = MokoUtils.bytesToHexString(Arrays.copyOfRange(scanRecord, 7, length + 4));
        }
        if (isEddystone) {
            data = MokoUtils.bytesToHexString(Arrays.copyOfRange(scanRecord, 11, length + 8));
        }
        if (beaconXInfo.validDataHashMap.containsKey(data)) {
            return beaconXInfo;
        } else {
            BeaconXInfo.ValidData validData = new BeaconXInfo.ValidData();
            if (isBeacon) {
                validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON;
            }
            if (isDeviceInfo) {
                validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
            }
            if (isEddystone) {
                String frameType = data.substring(0, 2);
                if ("00".equals(frameType)) {
                    // UID
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_UID;
                } else if ("10".equals(frameType)) {
                    // URL
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_URL;
                } else if ("20".equals(frameType)) {
                    // TLM（only one）
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM;
                    validData.data = data;
                    beaconXInfo.validDataHashMap.put(frameType, validData);
                    return beaconXInfo;
                }
            }
            validData.data = data;
            beaconXInfo.validDataHashMap.put(data, validData);
        }
        return beaconXInfo;
    }
}
