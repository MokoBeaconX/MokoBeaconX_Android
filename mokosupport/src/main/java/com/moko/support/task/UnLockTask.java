package com.moko.support.task;

import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.UnLockTask
 */
public class UnLockTask extends OrderTask {

    public byte[] data;

    public UnLockTask(int responseType) {
        super(OrderType.unLock, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] unLockBytes) {
        data = unLockBytes;
    }
}
