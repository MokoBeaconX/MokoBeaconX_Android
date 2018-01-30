package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.RadioTxPowerTask
 */
public class RadioTxPowerTask extends OrderTask {

    public byte[] data;

    public RadioTxPowerTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.radioTxPower, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
