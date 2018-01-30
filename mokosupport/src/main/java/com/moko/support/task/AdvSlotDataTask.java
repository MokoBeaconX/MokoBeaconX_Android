package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.AdvSlotDataTask
 */
public class AdvSlotDataTask extends OrderTask {

    public byte[] data;

    public AdvSlotDataTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.advSlotData, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
