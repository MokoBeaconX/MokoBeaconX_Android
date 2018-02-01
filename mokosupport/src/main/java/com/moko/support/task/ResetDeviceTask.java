package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ResetDeviceTask
 */
public class ResetDeviceTask extends OrderTask {

    public byte[] data = {(byte) 0x0b};

    public ResetDeviceTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.resetDevice, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
