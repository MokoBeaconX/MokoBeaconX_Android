package com.moko.support.entity;


import java.io.Serializable;

public enum ConfigKeyEnum implements Serializable {
    GET_SLOT_TYPE(0x61),
    GET_DEVICE_MAC(0x57),
    SET_DEVICE_NAME(0x58),
    GET_DEVICE_NAME(0x59),
    GET_IBEACON_UUID(0x64),
    SET_IBEACON_UUID(0x65),
    GET_IBEACON_INFO(0x66),
    SET_IBEACON_INFO(0x67),
    GET_CONNECTABLE(0x90),
    SET_CONNECTABLE(0x89),
    SET_CLOSE(0x60),
    ;

    private int configKey;

    ConfigKeyEnum(int configKey) {
        this.configKey = configKey;
    }


    public int getConfigKey() {
        return configKey;
    }

    public static ConfigKeyEnum fromConfigKey(int configKey) {
        for (ConfigKeyEnum configKeyEnum : ConfigKeyEnum.values()) {
            if (configKeyEnum.getConfigKey() == configKey) {
                return configKeyEnum;
            }
        }
        return null;
    }
}
