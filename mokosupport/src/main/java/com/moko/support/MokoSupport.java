package com.moko.support;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;

import com.moko.support.callback.MokoConnStateCallback;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.callback.MokoResponseCallback;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.MokoCharacteristic;
import com.moko.support.entity.OrderType;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.handler.MokoCharacteristicHandler;
import com.moko.support.handler.MokoConnStateHandler;
import com.moko.support.handler.MokoLeScanHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.utils.BleConnectionCompat;
import com.moko.support.utils.MokoUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.beacon.MokoSupport
 */
public class MokoSupport implements MokoResponseCallback {
    public static final int HANDLER_MESSAGE_WHAT_CONNECTED = 1;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECTED = 2;
    public static final int HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED = 3;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECT = 4;
    public static final UUID DESCRIPTOR_UUID_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MokoLeScanHandler mMokoLeScanHandler;
    private HashMap<OrderType, MokoCharacteristic> mCharacteristicMap;
    private BlockingQueue<OrderTask> mQueue;
    private MokoScanDeviceCallback mMokoScanDeviceCallback;

    private static volatile MokoSupport INSTANCE;

    private Context mContext;

    private MokoSupport() {
        //no instance
        mQueue = new LinkedBlockingQueue<>();
    }

    public static MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        LogModule.init(context);
        mContext = context;
        mHandler = new ServiceMessageHandler(this);
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private ServiceMessageHandler mHandler;

    public class ServiceMessageHandler extends BaseMessageHandler<MokoSupport> {
        private MokoConnStateCallback mokoConnStateCallback;

        public ServiceMessageHandler(MokoSupport module) {
            super(module);
        }

        @Override
        protected void handleMessage(MokoSupport module, Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_WHAT_CONNECTED:
                    mBluetoothGatt.discoverServices();
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECTED:
                    disConnectBle();
                    mokoConnStateCallback.onDisConnected();
                    break;
                case HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED:
                    LogModule.i("连接成功！");
                    mCharacteristicMap = MokoCharacteristicHandler.getInstance().getCharacteristics(mBluetoothGatt);
                    mokoConnStateCallback.onConnectSuccess();
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECT:
                    if (mBluetoothGatt != null) {
                        if (refreshDeviceCache()) {
                            LogModule.i("清理GATT层蓝牙缓存");
                        }
                        LogModule.i("断开连接");
                        mBluetoothGatt.close();
                        mBluetoothGatt.disconnect();
                    }
                    break;
            }
        }

        public void setMokoConnStateCallback(MokoConnStateCallback mokoConnStateCallback) {
            this.mokoConnStateCallback = mokoConnStateCallback;
        }
    }

    public void setConnStateCallback(final MokoConnStateCallback mokoConnStateCallback) {
        mHandler.setMokoConnStateCallback(mokoConnStateCallback);
    }

    /**
     * @Date 2017/12/12 0012
     * @Author wenzheng.liu
     * @Description 蓝牙是否打开
     */
    public boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * @Date 2018/1/16
     * @Author wenzheng.liu
     * @Description 打开蓝牙
     */
    public void openBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * @Date 2018/1/16
     * @Author wenzheng.liu
     * @Description 关闭蓝牙
     */
    public void closeBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 是否连接设备
     */
    public boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED;
    }

    public void startScanDevice(MokoScanDeviceCallback mokoScanDeviceCallback) {
        LogModule.i("开始扫描Beacon");
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        mMokoLeScanHandler = new MokoLeScanHandler(mokoScanDeviceCallback);
        scanner.startScan(mMokoLeScanHandler);
        mMokoScanDeviceCallback = mokoScanDeviceCallback;
        mokoScanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (mMokoLeScanHandler != null && mMokoScanDeviceCallback != null) {
            LogModule.i("结束扫描Beacon");
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mMokoLeScanHandler);
            mMokoScanDeviceCallback.onStopScan();
            mMokoLeScanHandler = null;
            mMokoScanDeviceCallback = null;
        }
    }

    public void connDevice(final Context context, final String address, final MokoConnStateCallback mokoConnStateCallback) {
        if (TextUtils.isEmpty(address)) {
            LogModule.i("connDevice: 地址为空");
            return;
        }
        if (!isBluetoothOpen()) {
            LogModule.i("connDevice: 蓝牙未打开");
            return;
        }
        if (isConnDevice(context, address)) {
            LogModule.i("connDevice: 设备已连接");
            return;
        }
        final MokoConnStateHandler gattCallback = MokoConnStateHandler.getInstance();
        gattCallback.setBeaconResponseCallback(this);
        setConnStateCallback(mokoConnStateCallback);
        gattCallback.setMessageHandler(mHandler);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogModule.i("开始尝试连接");
                    mBluetoothGatt = (new BleConnectionCompat(context)).connectGatt(device, false, gattCallback);
                }
            });
        } else {
            LogModule.i("获取蓝牙设备失败");
        }
    }

    /**
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description 断开连接
     */
    public void disConnectBle() {
        mHandler.sendEmptyMessage(MokoSupport.HANDLER_MESSAGE_WHAT_DISCONNECT);
    }

    /**
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    private boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                LogModule.i("An exception occured while refreshing device");
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    public void sendOrder(OrderTask... orderTasks) {
        if (orderTasks.length == 0) {
            return;
        }
        if (!isSyncData()) {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
            executeTask(null);
        } else {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
        }
    }

    private void executeTask(MokoOrderTaskCallback callback) {
        if (callback != null && !isSyncData()) {
            callback.onOrderFinish();
            return;
        }
        final OrderTask orderTask = mQueue.peek();
        if (mBluetoothGatt == null) {
            LogModule.i("executeTask : BluetoothGatt is null");
            return;
        }
        if (orderTask == null) {
            LogModule.i("executeTask : orderTask is null");
            return;
        }
        final MokoCharacteristic mokoCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (mokoCharacteristic == null) {
            LogModule.i("executeTask : mokoCharacteristic is null");
            return;
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_READ) {
            sendReadOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE) {
            sendWriteOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE) {
            sendWriteNoResponseOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_NOTIFY) {
            sendNotifyOrder(orderTask, mokoCharacteristic);
        }
        orderTimeoutHandler(orderTask);
    }

    // 发送可监听命令
    private void sendNotifyOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app set device notify : " + orderTask.orderType.getName());
        final BluetoothGattDescriptor descriptor = mokoCharacteristic.characteristic.getDescriptor(DESCRIPTOR_UUID_NOTIFY);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        });
    }

    // 发送可写命令
    private void sendWriteOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device write : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 发送可写无应答命令
    private void sendWriteNoResponseOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mokoCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 发送可读命令
    private void sendReadOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device read : " + orderTask.orderType.getName());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.readCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 直接发送命令(升级专用)
    public void sendDirectOrder(OrderTask orderTask) {
        final MokoCharacteristic mokoCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (mokoCharacteristic == null) {
            LogModule.i("executeTask : mokoCharacteristic is null");
            return;
        }
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mokoCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    private void orderTimeoutHandler(final OrderTask orderTask) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (orderTask.orderStatus != OrderTask.ORDER_STATUS_SUCCESS) {
                    LogModule.i("应答超时");
                    mQueue.poll();
                    orderTask.mokoOrderTaskCallback.onOrderTimeout(orderTask.response);
                    executeTask(orderTask.mokoOrderTaskCallback);
                }
            }
        }, orderTask.delayTime);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (isSyncData()) {
            // 非延时应答
            OrderTask orderTask = mQueue.peek();
            if (value != null && value.length > 0) {
                switch (orderTask.orderType) {
                    case writeConfig:
                        formatCommonOrder(orderTask, value);
                        break;
                }
            }
        } else {
            OrderType orderType = null;
            // 延时应答
            if (orderType != null) {
                LogModule.i(orderType.getName());
                Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_NOTIFY);
                intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
                intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE, value);
                mContext.sendOrderedBroadcast(intent, null);
            }
        }

    }

    @Override
    public void onCharacteristicWrite(byte[] value) {
        if (!isSyncData()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case advSlot:
                case advInterval:
                case radioTxPower:
                case advTxPower:
                case lockState:
                case unLock:
                case advSlotData:
                case resetDevice:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @Override
    public void onCharacteristicRead(byte[] value) {
        if (!isSyncData()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case manufacturer:
                case deviceModel:
                case productDate:
                case hardwareVersion:
                case firmwareVersion:
                case softwareVersion:
                case battery:
                case advSlot:
                case advInterval:
                case radioTxPower:
                case advTxPower:
                case lockState:
                case unLock:
                case advSlotData:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @Override
    public void onDescriptorWrite() {
        if (!isSyncData()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        LogModule.i("device to app notify : " + orderTask.orderType.getName());
        orderTask.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        mQueue.poll();
        executeTask(orderTask.mokoOrderTaskCallback);
    }

    private void formatCommonOrder(OrderTask task, byte[] value) {
        task.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        task.response.responseValue = value;
        mQueue.poll();
        task.mokoOrderTaskCallback.onOrderResult(task.response);
        executeTask(task.mokoOrderTaskCallback);
    }

    public synchronized boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
    }
}
