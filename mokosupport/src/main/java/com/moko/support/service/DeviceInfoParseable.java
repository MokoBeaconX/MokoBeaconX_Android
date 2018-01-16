package com.moko.support.service;

import com.moko.support.entity.DeviceInfo;

/**
 * @Date 2018/1/11
 * @Author wenzheng.liu
 * @Description 设备解析接口
 * @ClassPath com.moko.support.service.DeviceInfoParseable
 */
public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
