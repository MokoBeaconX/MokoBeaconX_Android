package com.moko.beaconx;

public class AppConstants {
    // data time pattern
    public static final String PATTERN_HH_MM = "HH:mm";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_MM_DD = "MM/dd";
    public static final String PATTERN_MM_DD_2 = "MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    // sp
    public static final String SP_NAME = "sp_name_beacon";

    public static final String SP_KEY_DEVICE_ADDRESS = "sp_key_device_address";
    // extra_key
    // 设备列表
    public static final String EXTRA_KEY_RESPONSE_ORDER_TYPE = "EXTRA_KEY_RESPONSE_ORDER_TYPE";
    public static final String EXTRA_KEY_RESPONSE_VALUE = "EXTRA_KEY_RESPONSE_VALUE";
    public static final String EXTRA_KEY_DEVICE_CONFIG = "EXTRA_KEY_DEVICE_CONFIG";
    public static final String EXTRA_KEY_TEMP_TARGET = "EXTRA_KEY_TEMP_TARGET";
    public static final String EXTRA_KEY_TEMP_HOUR = "EXTRA_KEY_TEMP_HOUR";
    public static final String EXTRA_KEY_TEMP_MINUTE = "EXTRA_KEY_TEMP_MINUTE";
    // request_code
    public static final int REQUEST_CODE_TEMP_TARGET = 100;
    public static final int REQUEST_CODE_TIMER = 101;
    public static final int REQUEST_CODE_DELAY = 102;
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 103;

    // result_code
    public static final int RESULT_CONN_DISCONNECTED = 2;
}
