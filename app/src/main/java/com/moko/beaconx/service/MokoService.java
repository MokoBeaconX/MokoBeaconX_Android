package com.moko.beaconx.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoConnStateCallback;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.SlotEnum;
import com.moko.support.handler.BaseMessageHandler;
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
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.task.ProductDateTask;
import com.moko.support.task.RadioTxPowerTask;
import com.moko.support.task.ResetDeviceTask;
import com.moko.support.task.SoftwareVersionTask;
import com.moko.support.task.UnLockTask;
import com.moko.support.task.WriteConfigTask;


/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.service.MokoService
 */
public class MokoService extends Service implements MokoConnStateCallback, MokoOrderTaskCallback {
    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MokoService getService() {
            return MokoService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogModule.i("启动后台服务");
        mHandler = new ServiceHandler(this);

    }

    @Override
    public void onDestroy() {
        LogModule.i("关闭后台服务");
        MokoSupport.getInstance().disConnectBle();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理扫描
    ///////////////////////////////////////////////////////////////////////////

    public void startScanDevice(MokoScanDeviceCallback callback) {
        MokoSupport.getInstance().startScanDevice(callback);
    }

    public void stopScanDevice() {
        MokoSupport.getInstance().stopScanDevice();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理连接
    ///////////////////////////////////////////////////////////////////////////

    public void connDevice(String address) {
        MokoSupport.getInstance().connDevice(this, address, this);
    }

    @Override
    public void onConnectSuccess() {
        Intent intent = new Intent(MokoConstants.ACTION_CONNECT_SUCCESS);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onDisConnected() {
        Intent intent = new Intent(MokoConstants.ACTION_CONNECT_DISCONNECTED);
        sendOrderedBroadcast(intent, null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理应答
    ///////////////////////////////////////////////////////////////////////////

    public void sendOrder(OrderTask... orderTasks) {
        MokoSupport.getInstance().sendOrder(orderTasks);
    }

    public void sendDirectOrder(OrderTask orderTask) {
        MokoSupport.getInstance().sendDirectOrder(orderTask);
    }

    /**
     * @Description 打开配置通知set config notify
     */
    public OrderTask setConfigNotify() {
        NotifyConfigTask notifyConfigTask = new NotifyConfigTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyConfigTask;
    }

    /**
     * @Description 获取设备锁状态get lock state
     */
    public OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }

    /**
     * @Description 设置设备锁状态set lock state
     */
    public OrderTask setLockState(byte[] lockState) {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        lockStateTask.setData(lockState);
        return lockStateTask;
    }

    /**
     * @Description 获取解锁加密内容get unlock
     */
    public OrderTask getUnLock() {
        UnLockTask unLockTask = new UnLockTask(this, OrderTask.RESPONSE_TYPE_READ);
        return unLockTask;
    }

    /**
     * @Description 解锁set unlock
     */
    public OrderTask setUnLock(byte[] unlockBytes) {
        UnLockTask unLockTask = new UnLockTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        unLockTask.setData(unlockBytes);
        return unLockTask;
    }

    /**
     * @Description 获取通道类型
     */
    public OrderTask getSlotType() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_SLOT_TYPE);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备MAC
     */
    public OrderTask getDeviceMac() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_MAC);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备名称
     */
    public OrderTask getDeviceName() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_NAME);
        return writeConfigTask;
    }

    /**
     * @Description 设置设备名称
     */
    public OrderTask setDeviceName(String deviceNameHex) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setDeviceName(deviceNameHex);
        return writeConfigTask;
    }

    /**
     * @Description 获取连接状态
     */
    public OrderTask getConnectable() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_CONNECTABLE);
        return writeConfigTask;
    }

    /**
     * @Description 获取连接状态
     */
    public OrderTask setConnectable(boolean isConnectable) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setConneactable(isConnectable);
        return writeConfigTask;
    }

    /**
     * @Description 获取制造商
     */
    public OrderTask getManufacturer() {
        ManufacturerTask manufacturerTask = new ManufacturerTask(this, OrderTask.RESPONSE_TYPE_READ);
        return manufacturerTask;
    }

    /**
     * @Description 获取设备型号
     */
    public OrderTask getDeviceModel() {
        DeviceModelTask deviceModelTask = new DeviceModelTask(this, OrderTask.RESPONSE_TYPE_READ);
        return deviceModelTask;
    }

    /**
     * @Description 获取生产日期
     */
    public OrderTask getProductDate() {
        ProductDateTask productDateTask = new ProductDateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return productDateTask;
    }

    /**
     * @Description 获取硬件版本
     */
    public OrderTask getHardwareVersion() {
        HardwareVersionTask hardwareVersionTask = new HardwareVersionTask(this, OrderTask.RESPONSE_TYPE_READ);
        return hardwareVersionTask;
    }

    /**
     * @Description 获取固件版本
     */
    public OrderTask getFirmwareVersion() {
        FirmwareVersionTask firmwareVersionTask = new FirmwareVersionTask(this, OrderTask.RESPONSE_TYPE_READ);
        return firmwareVersionTask;
    }

    /**
     * @Description 获取软件版本
     */
    public OrderTask getSoftwareVersion() {
        SoftwareVersionTask softwareVersionTask = new SoftwareVersionTask(this, OrderTask.RESPONSE_TYPE_READ);
        return softwareVersionTask;
    }

    /**
     * @Description 获取电池电量
     */
    public OrderTask getBattery() {
        BatteryTask batteryTask = new BatteryTask(this, OrderTask.RESPONSE_TYPE_READ);
        return batteryTask;
    }

    /**
     * @Description 切换通道
     */
    public OrderTask setSlot(SlotEnum slot) {
        AdvSlotTask advSlotTask = new AdvSlotTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advSlotTask.setData(slot);
        return advSlotTask;
    }

    /**
     * @Description 切换通道
     */
    public OrderTask getSlotData() {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(this, OrderTask.RESPONSE_TYPE_READ);
        return advSlotDataTask;
    }

    /**
     * @Description 设置通道信息
     */
    public OrderTask setSlotData(byte[] data) {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advSlotDataTask.setData(data);
        return advSlotDataTask;
    }

    /**
     * @Description 获取信号强度
     */
    public OrderTask getRadioTxPower() {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(this, OrderTask.RESPONSE_TYPE_READ);
        return radioTxPowerTask;
    }

    /**
     * @Description 设置信号强度
     */
    public OrderTask setRadioTxPower(byte[] data) {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        radioTxPowerTask.setData(data);
        return radioTxPowerTask;
    }

    /**
     * @Description 获取广播间隔
     */
    public OrderTask getAdvInterval() {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(this, OrderTask.RESPONSE_TYPE_READ);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播间隔
     */
    public OrderTask setAdvInterval(byte[] data) {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advIntervalTask.setData(data);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播强度
     */
    public OrderTask setAdvTxPower(byte[] data) {
        AdvTxPowerTask advTxPowerTask = new AdvTxPowerTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advTxPowerTask.setData(data);
        return advTxPowerTask;
    }

    /**
     * @Description 获取iBeaconUUID
     */
    public OrderTask getiBeaconUUID() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_UUID);
        return writeConfigTask;
    }

    /**
     * @Description 设置iBeaconUUID
     */
    public OrderTask setiBeaconUUID(String uuidHex) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setiBeaconUUID(uuidHex);
        return writeConfigTask;
    }

    /**
     * @Description 获取iBeaconInfo
     */
    public OrderTask getiBeaconInfo() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_INFO);
        return writeConfigTask;
    }

    /**
     * @Description 设置iBeaconInfo
     */
    public OrderTask setiBeaconInfo(int major, int minor, int advTxPower) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setiBeaconData(major, minor, advTxPower);
        return writeConfigTask;
    }

    /**
     * @Description 恢复出厂设置
     */
    public OrderTask resetDevice() {
        ResetDeviceTask resetDeviceTask = new ResetDeviceTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        return resetDeviceTask;
    }

    @Override
    public void onOrderResult(OrderTaskResponse response) {
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_SUCCESS);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderTaskResponse response) {
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_TIMEOUT);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderFinish() {
        LogModule.i("任务完成");
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_FINISH);
        sendOrderedBroadcast(intent, null);
    }

    public ServiceHandler mHandler;

    public class ServiceHandler extends BaseMessageHandler<MokoService> {

        public ServiceHandler(MokoService service) {
            super(service);
        }

        @Override
        protected void handleMessage(MokoService service, Message msg) {
        }
    }
}
