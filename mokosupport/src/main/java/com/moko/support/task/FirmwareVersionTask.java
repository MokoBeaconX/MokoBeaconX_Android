package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.FirmwareVersionTask
 */
public class FirmwareVersionTask extends OrderTask {

    public byte[] data;

    public FirmwareVersionTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.firmwareVersion, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
