package com.moko.support.entity;


import java.io.Serializable;

public class SlotData implements Serializable {
    public SlotEnum slotEnum;
    public SlotFrameTypeEnum frameTypeEnum;
    // iBeacon
    public String iBeaconUUID;
    public String major;
    public String minor;
    public int rssi_1m;
    // URL
    public UrlSchemeEnum urlSchemeEnum;
    public String urlContent;
    // UID
    public String namespace;
    public String instanceId;
    // TLM
    // NO DATA

    // BaseParam
    public int rssi_0m;
    public int txPower;
    public int advInterval;


    @Override
    public String toString() {
        return "SlotData{" +
                "slotEnum=" + slotEnum.getTitle() +
                ", frameTypeEnum=" + frameTypeEnum.getShowName() +
                ", iBeaconUUID='" + iBeaconUUID + '\'' +
                ", major='" + major + '\'' +
                ", minor='" + minor + '\'' +
                ", rssi_1m='" + rssi_1m + '\'' +
                ", urlContent='" + urlContent + '\'' +
                ", namespace='" + namespace + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", rssi_0m='" + rssi_0m + '\'' +
                ", txPower=" + txPower +
                ", advInterval=" + advInterval +
                '}';
    }
}
