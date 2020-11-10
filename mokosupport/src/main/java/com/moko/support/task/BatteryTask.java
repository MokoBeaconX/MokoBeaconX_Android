package com.moko.support.task;

import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.BatteryTask
 */
public class BatteryTask extends OrderTask {

    public byte[] data;

    public BatteryTask(int responseType) {
        super(OrderType.battery, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
