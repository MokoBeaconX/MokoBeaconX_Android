package com.moko.beaconx.utils;

import android.os.ParcelUuid;
import android.text.TextUtils;

import com.moko.beaconx.entity.BeaconXInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.service.DeviceInfoParseable;
import com.moko.support.utils.MokoUtils;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

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
        ScanRecord scanRecord = deviceInfo.scanRecord;
        String unanalysedData = null;
        // filter
        boolean isEddystone = false;
        boolean isBeacon = false;
        boolean isDeviceInfo = false;

        Map<ParcelUuid, byte[]> map = scanRecord.getServiceData();
        if (map != null && !map.isEmpty()) {
            for (ParcelUuid uuid : map.keySet()) {
                String serviceDataUuid = uuid.getUuid().toString().toLowerCase();
                if (TextUtils.isEmpty(serviceDataUuid)) {
                    continue;
                }
                String serviceData = MokoUtils.bytesToHexString(scanRecord.getServiceData(uuid));
                if (TextUtils.isEmpty(serviceData)) {
                    continue;
                }
                if (serviceDataUuid.contains("feaa")) {
                    unanalysedData = serviceData;
                    isEddystone = true;
                    continue;
                }
                if (serviceDataUuid.contains("ff10")) {
                    unanalysedData = serviceData;
                    isDeviceInfo = true;
                    continue;
                }
                if (serviceDataUuid.contains("ff20")) {
                    unanalysedData = serviceData;
                    isBeacon = true;
                    continue;
                }
            }
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
            beaconXInfo.validDataHashMap = new HashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, beaconXInfo);
        }
        if (beaconXInfo.validDataHashMap.containsKey(unanalysedData)) {
            return beaconXInfo;
        } else {
            BeaconXInfo.ValidData validData = new BeaconXInfo.ValidData();
            validData.data = unanalysedData;
            if (isBeacon) {
                validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON;
            }
            if (isDeviceInfo) {
                validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
                beaconXInfo.name = MokoUtils.hex2String(unanalysedData.substring(22));
            }
            if (isEddystone) {
                String frameType = unanalysedData.substring(0, 2);
                if ("00".equals(frameType)) {
                    // UID
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_UID;
                } else if ("10".equals(frameType)) {
                    // URL
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_URL;
                } else if ("20".equals(frameType)) {
                    // TLM（only one）
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM;
                    beaconXInfo.validDataHashMap.put(frameType, validData);
                    return beaconXInfo;
                }
            }
            beaconXInfo.validDataHashMap.put(unanalysedData, validData);
        }
        return beaconXInfo;
    }
}
