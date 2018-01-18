package com.moko.beaconx.entity;

import java.io.Serializable;


public class BeaconXTLM implements Serializable {
    // 电池电压
    public String vbatt;
    // 芯片内部温度
    public String temp;
    // 广播次数统计
    public String adv_cnt;
    // 设备运行时间
    public String sec_cnt;
}
