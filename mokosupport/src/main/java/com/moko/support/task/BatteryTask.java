package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.BatteryTask
 */
public class BatteryTask extends OrderTask {

    public byte[] data;

    public BatteryTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.battery, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
