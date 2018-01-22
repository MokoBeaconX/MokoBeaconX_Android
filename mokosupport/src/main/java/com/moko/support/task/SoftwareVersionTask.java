package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SoftwareVersionTask
 */
public class SoftwareVersionTask extends OrderTask {

    public byte[] data;

    public SoftwareVersionTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.softwareVersion, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
