package com.moko.beaconx.entity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Date 2018/1/16
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconx.entity.BeaconXInfo
 */
public class BeaconXInfo implements Serializable {

    public static final int VALID_DATA_FRAME_TYPE_UID = 0;
    public static final int VALID_DATA_FRAME_TYPE_URL = 1;
    public static final int VALID_DATA_FRAME_TYPE_TLM = 2;
    public static final int VALID_DATA_FRAME_TYPE_IBEACON = 3;
    public static final int VALID_DATA_FRAME_TYPE_INFO = 4;


    public String name;
    public int rssi;
    public String mac;
    public HashMap<String, ValidData> validDataHashMap;

    @Override
    public String toString() {
        return "BeaconXInfo{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }


    public static class ValidData {
        public int type;
        public String data;

        @Override
        public String toString() {
            return "ValidData{" +
                    "type=" + type +
                    ", data='" + data + '\'' +
                    '}';
        }
    }
}
