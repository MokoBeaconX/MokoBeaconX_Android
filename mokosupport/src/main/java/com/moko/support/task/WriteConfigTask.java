package com.moko.support.task;

import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.WriteConfigTask
 */
public class WriteConfigTask extends OrderTask {
    public byte[] data;

    public WriteConfigTask(int responseType) {
        super(OrderType.writeConfig, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ConfigKeyEnum key) {
        switch (key) {
            case GET_SLOT_TYPE:
            case GET_DEVICE_MAC:
            case GET_DEVICE_NAME:
            case GET_CONNECTABLE:
            case GET_IBEACON_UUID:
            case GET_IBEACON_INFO:
            case SET_CLOSE:
                createGetConfigData(key.getConfigKey());
                break;
        }
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{(byte) 0xEA, (byte) configKey, (byte) 0x00, (byte) 0x00};
    }

    public void setiBeaconData(int major, int minor, int advTxPower) {
        String value = "EA" + MokoUtils.int2HexString(ConfigKeyEnum.SET_IBEACON_INFO.getConfigKey()) + "0005"
                + String.format("%04X", major) + String.format("%04X", minor) + MokoUtils.int2HexString(Math.abs(advTxPower));
        data = MokoUtils.hex2bytes(value);
    }

    public void setiBeaconUUID(String uuidHex) {
        String value = "EA" + MokoUtils.int2HexString(ConfigKeyEnum.SET_IBEACON_UUID.getConfigKey()) + "0010"
                + uuidHex;
        data = MokoUtils.hex2bytes(value);
    }

    public void setDeviceName(String deviceName) {
        String deviceNameHex = MokoUtils.string2Hex(deviceName);
        String value = "EA" + MokoUtils.int2HexString(ConfigKeyEnum.SET_DEVICE_NAME.getConfigKey()) + "00"
                + MokoUtils.int2HexString(deviceNameHex.length() / 2) + deviceNameHex;
        data = MokoUtils.hex2bytes(value);
    }

    public void setConneactable(boolean isConnectable) {
        String value = "EA" + MokoUtils.int2HexString(ConfigKeyEnum.SET_CONNECTABLE.getConfigKey()) + "0001"
                + (isConnectable ? "01" : "00");
        data = MokoUtils.hex2bytes(value);
    }
}
