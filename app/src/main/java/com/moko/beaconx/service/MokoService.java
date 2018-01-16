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
import com.moko.support.entity.OrderType;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;


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

    public void getReadableData() {
        sendOrder();
    }

    public void sendOrder(OrderTask... orderTasks) {
        MokoSupport.getInstance().sendOrder(orderTasks);
    }

    public void sendDirectOrder(OrderTask orderTask) {
        MokoSupport.getInstance().sendDirectOrder(orderTask);
    }



    @Override
    public void onOrderResult(OrderType orderType, byte[] value) {
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_SUCCESS);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE, value);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderType orderType) {
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_TIMEOUT);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
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
