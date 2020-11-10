package com.moko.support;

import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.SlotEnum;
import com.moko.support.log.LogModule;
import com.moko.support.task.AdvIntervalTask;
import com.moko.support.task.AdvSlotDataTask;
import com.moko.support.task.AdvSlotTask;
import com.moko.support.task.AdvTxPowerTask;
import com.moko.support.task.BatteryTask;
import com.moko.support.task.DeviceModelTask;
import com.moko.support.task.FirmwareVersionTask;
import com.moko.support.task.HardwareVersionTask;
import com.moko.support.task.LockStateTask;
import com.moko.support.task.ManufacturerTask;
import com.moko.support.task.NotifyConfigTask;
import com.moko.support.task.OrderTask;
import com.moko.support.task.ProductDateTask;
import com.moko.support.task.RadioTxPowerTask;
import com.moko.support.task.ResetDeviceTask;
import com.moko.support.task.SoftwareVersionTask;
import com.moko.support.task.UnLockTask;
import com.moko.support.task.WriteConfigTask;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class OrderTaskAssembler {

    /**
     * @Description 打开配置通知set config notify
     */
    public static OrderTask setConfigNotify() {
        NotifyConfigTask notifyConfigTask = new NotifyConfigTask(OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyConfigTask;
    }

    /**
     * @Description 获取设备锁状态get lock state
     */
    public static OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }

    /**
     * @Description 设置设备锁状态set lock state
     */
    public static OrderTask setLockState(String newPassword) {
        if (passwordBytes != null) {
            LogModule.i("旧密码：" + MokoUtils.bytesToHexString(passwordBytes));
            byte[] bt1 = newPassword.getBytes();
            byte[] bt2 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            byte[] newPasswordBytes = new byte[bt1.length + bt2.length];
            System.arraycopy(bt1, 0, newPasswordBytes, 0, bt1.length);
            System.arraycopy(bt2, 0, newPasswordBytes, bt1.length, bt2.length);
            LogModule.i("新密码：" + MokoUtils.bytesToHexString(newPasswordBytes));
            // 用旧密码加密新密码
            byte[] newPasswordEncryptBytes = encrypt(newPasswordBytes, passwordBytes);
            if (newPasswordEncryptBytes != null) {
                LockStateTask lockStateTask = new LockStateTask(OrderTask.RESPONSE_TYPE_WRITE);
                byte[] unLockBytes = new byte[newPasswordEncryptBytes.length + 1];
                unLockBytes[0] = 0;
                System.arraycopy(newPasswordEncryptBytes, 0, unLockBytes, 1, newPasswordEncryptBytes.length);
                lockStateTask.setData(unLockBytes);
                return lockStateTask;
            }
        }
        return null;
    }

    /**
     * @Date 2018/1/22
     * @Author wenzheng.liu
     * @Description 加密
     */
    public static byte[] encrypt(byte[] value, byte[] password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");// 转换为AES专用密钥
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化为加密模式的密码器
            byte[] result = cipher.doFinal(value);// 加密
            byte[] data = Arrays.copyOf(result, 16);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description 获取解锁加密内容get unlock
     */
    public static OrderTask getUnLock() {
        UnLockTask unLockTask = new UnLockTask(OrderTask.RESPONSE_TYPE_READ);
        return unLockTask;
    }

    private static byte[] passwordBytes;

    /**
     * @Description 解锁set unlock
     */
    public static OrderTask setUnLock(String password, byte[] value) {
        byte[] bt1 = password.getBytes();
        byte[] bt2 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        passwordBytes = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, passwordBytes, 0, bt1.length);
        System.arraycopy(bt2, 0, passwordBytes, bt1.length, bt2.length);
        LogModule.i("密码：" + MokoUtils.bytesToHexString(passwordBytes));
        byte[] unLockBytes = encrypt(value, passwordBytes);
        if (unLockBytes != null) {
            UnLockTask unLockTask = new UnLockTask(OrderTask.RESPONSE_TYPE_WRITE);
            unLockTask.setData(unLockBytes);
            return unLockTask;
        }
        return null;
    }

    /**
     * @Description 获取通道类型
     */
    public static OrderTask getSlotType() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_SLOT_TYPE);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备MAC
     */
    public static OrderTask getDeviceMac() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_MAC);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备名称
     */
    public static OrderTask getDeviceName() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_NAME);
        return writeConfigTask;
    }

    /**
     * @Description 设置设备名称
     */
    public static OrderTask setDeviceName(String deviceName) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setDeviceName(deviceName);
        return writeConfigTask;
    }

    /**
     * @Description 获取连接状态
     */
    public static OrderTask getConnectable() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_CONNECTABLE);
        return writeConfigTask;
    }

    /**
     * @Description 设置连接状态
     */
    public static OrderTask setConnectable(boolean isConnectable) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setConneactable(isConnectable);
        return writeConfigTask;
    }

    /**
     * @Description 获取制造商
     */
    public static OrderTask getManufacturer() {
        ManufacturerTask manufacturerTask = new ManufacturerTask(OrderTask.RESPONSE_TYPE_READ);
        return manufacturerTask;
    }

    /**
     * @Description 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        DeviceModelTask deviceModelTask = new DeviceModelTask(OrderTask.RESPONSE_TYPE_READ);
        return deviceModelTask;
    }

    /**
     * @Description 获取生产日期
     */
    public static OrderTask getProductDate() {
        ProductDateTask productDateTask = new ProductDateTask(OrderTask.RESPONSE_TYPE_READ);
        return productDateTask;
    }

    /**
     * @Description 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        HardwareVersionTask hardwareVersionTask = new HardwareVersionTask(OrderTask.RESPONSE_TYPE_READ);
        return hardwareVersionTask;
    }

    /**
     * @Description 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        FirmwareVersionTask firmwareVersionTask = new FirmwareVersionTask(OrderTask.RESPONSE_TYPE_READ);
        return firmwareVersionTask;
    }

    /**
     * @Description 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        SoftwareVersionTask softwareVersionTask = new SoftwareVersionTask(OrderTask.RESPONSE_TYPE_READ);
        return softwareVersionTask;
    }

    /**
     * @Description 获取电池电量
     */
    public static OrderTask getBattery() {
        BatteryTask batteryTask = new BatteryTask(OrderTask.RESPONSE_TYPE_READ);
        return batteryTask;
    }

    /**
     * @Description 切换通道
     */
    public static OrderTask setSlot(SlotEnum slot) {
        AdvSlotTask advSlotTask = new AdvSlotTask(OrderTask.RESPONSE_TYPE_WRITE);
        advSlotTask.setData(slot);
        return advSlotTask;
    }

    /**
     * @Description 获取通道数据
     */
    public static OrderTask getSlotData() {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(OrderTask.RESPONSE_TYPE_READ);
        return advSlotDataTask;
    }

    /**
     * @Description 设置通道信息
     */
    public static OrderTask setSlotData(byte[] data) {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(OrderTask.RESPONSE_TYPE_WRITE);
        advSlotDataTask.setData(data);
        return advSlotDataTask;
    }

    /**
     * @Description 获取信号强度
     */
    public static OrderTask getRadioTxPower() {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(OrderTask.RESPONSE_TYPE_READ);
        return radioTxPowerTask;
    }

    /**
     * @Description 设置信号强度
     */
    public static OrderTask setRadioTxPower(byte[] data) {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(OrderTask.RESPONSE_TYPE_WRITE);
        radioTxPowerTask.setData(data);
        return radioTxPowerTask;
    }

    /**
     * @Description 获取广播间隔
     */
    public static OrderTask getAdvInterval() {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(OrderTask.RESPONSE_TYPE_READ);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播间隔
     */
    public static OrderTask setAdvInterval(byte[] data) {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(OrderTask.RESPONSE_TYPE_WRITE);
        advIntervalTask.setData(data);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播强度
     */
    public static OrderTask setAdvTxPower(byte[] data) {
        AdvTxPowerTask advTxPowerTask = new AdvTxPowerTask(OrderTask.RESPONSE_TYPE_WRITE);
        advTxPowerTask.setData(data);
        return advTxPowerTask;
    }

    /**
     * @Description 获取iBeaconUUID
     */
    public static OrderTask getiBeaconUUID() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_UUID);
        return writeConfigTask;
    }

    /**
     * @Description 设置iBeaconUUID
     */
    public static OrderTask setiBeaconUUID(String uuidHex) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setiBeaconUUID(uuidHex);
        return writeConfigTask;
    }

    /**
     * @Description 获取iBeaconInfo
     */
    public static OrderTask getiBeaconInfo() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_INFO);
        return writeConfigTask;
    }

    /**
     * @Description 设置iBeaconInfo
     */
    public static OrderTask setiBeaconInfo(int major, int minor, int advTxPower) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setiBeaconData(major, minor, advTxPower);
        return writeConfigTask;
    }

    /**
     * @Description 关机
     */
    public static OrderTask setClose() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.SET_CLOSE);
        return writeConfigTask;
    }

    /**
     * @Description 恢复出厂设置
     */
    public static OrderTask resetDevice() {
        ResetDeviceTask resetDeviceTask = new ResetDeviceTask(OrderTask.RESPONSE_TYPE_WRITE);
        return resetDeviceTask;
    }
}
