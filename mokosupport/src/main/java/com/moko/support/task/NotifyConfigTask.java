package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.NotifyConfigTask
 */
public class NotifyConfigTask extends OrderTask {

    public byte[] data;

    public NotifyConfigTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.notifyConfig, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
