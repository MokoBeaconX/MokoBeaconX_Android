package com.moko.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.entity.OrderType
 */
public enum OrderType implements Serializable {
    deviceInfo("deviceInfo", "0000ffc2-0000-1000-8000-00805f9b34fb"),
    deviceInfoWrite("deviceInfoWrite", "0000ffc1-0000-1000-8000-00805f9b34fb"),
    temperature("temperature", "0000fff0-0000-1000-8000-00805f9b34fb"),
    hour("hour", "0000fff1-0000-1000-8000-00805f9b34fb"),
    minute("minute", "0000fff2-0000-1000-8000-00805f9b34fb"),
    delayHour("delayHour", "0000fff3-0000-1000-8000-00805f9b34fb"),
    delayMinute("delayMinute", "0000fff4-0000-1000-8000-00805f9b34fb"),
    scale("scale", "0000fff5-0000-1000-8000-00805f9b34fb"),
    error("error", "0000fff6-0000-1000-8000-00805f9b34fb"),
    isRunning("isRunning", "0000fff7-0000-1000-8000-00805f9b34fb"),
    temperatureTarget("temperatureTarget", "0000fff8-0000-1000-8000-00805f9b34fb"),
    isConnectedToWifi("isConnectedToWifi", "0000fff9-0000-1000-8000-00805f9b34fb"),
    wifiSSID("wifiSSID", "0000fffa-0000-1000-8000-00805f9b34fb"),
    wifiPassword("wifiPassword", "0000fffb-0000-1000-8000-00805f9b34fb"),
    wifiSecurity("wifiSecurity", "0000fffc-0000-1000-8000-00805f9b34fb"),
    mqttId("mqttId", "0000fffd-0000-1000-8000-00805f9b34fb");


    private String uuid;
    private String name;

    OrderType(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
