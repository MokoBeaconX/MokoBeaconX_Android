package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ManufacturerTask
 */
public class ManufacturerTask extends OrderTask {

    public byte[] data;

    public ManufacturerTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.manufacturer, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
