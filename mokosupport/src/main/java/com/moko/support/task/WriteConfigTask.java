package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.WriteConfigTask
 */
public class WriteConfigTask extends OrderTask {

    public static final int CONFIG_TYPE_GET_SLOT = 0x61;
    public static final int CONFIG_TYPE_GET_MAC = 0x57;
    public static final int CONFIG_TYPE_GET_NAME = 0x59;
    public static final int CONFIG_TYPE_GET_CONNECTABLE = 0x90;
    public byte[] data;

    public WriteConfigTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.writeConfig, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int type) {
        switch (type) {
            case CONFIG_TYPE_GET_SLOT:
                createGetSlot();
                break;
            case CONFIG_TYPE_GET_MAC:
                createGetMac();
                break;
            case CONFIG_TYPE_GET_NAME:
                createGetName();
                break;
            case CONFIG_TYPE_GET_CONNECTABLE:
                createGetConnectable();
                break;
        }
    }

    private void createGetSlot() {
        data = new byte[]{(byte) 0xEA, (byte) CONFIG_TYPE_GET_SLOT, (byte) 0x00, (byte) 0x00};
    }

    private void createGetMac() {
        data = new byte[]{(byte) 0xEA, (byte) CONFIG_TYPE_GET_MAC, (byte) 0x00, (byte) 0x00};
    }

    private void createGetName() {
        data = new byte[]{(byte) 0xEA, (byte) CONFIG_TYPE_GET_NAME, (byte) 0x00, (byte) 0x00};
    }
    private void createGetConnectable() {
        data = new byte[]{(byte) 0xEA, (byte) CONFIG_TYPE_GET_CONNECTABLE, (byte) 0x00, (byte) 0x00};
    }
}
