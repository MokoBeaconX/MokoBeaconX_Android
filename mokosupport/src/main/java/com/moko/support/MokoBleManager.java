package com.moko.support;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;

import com.moko.support.callback.MokoResponseCallback;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

public class MokoBleManager extends BleManager<BleManagerCallbacks> {

    private MokoResponseCallback mMokoResponseCallback;
    private static MokoBleManager managerInstance = null;
    private final static UUID SERVICE_UUID = UUID.fromString("E62A0001-1362-4F28-9327-F5B74E970801");
    private final static UUID LOCKED_UUID = UUID.fromString("E62A0003-1362-4F28-9327-F5B74E970801");
    private BluetoothGattCharacteristic lockedCharacteristic;

    public static synchronized MokoBleManager getMokoBleManager(final Context context) {
        if (managerInstance == null) {
            managerInstance = new MokoBleManager(context);
        }
        return managerInstance;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        LogModule.v(message);
    }

    public MokoBleManager(@NonNull Context context) {
        super(context);
    }

    public void setBeaconResponseCallback(MokoResponseCallback mMokoResponseCallback) {
        this.mMokoResponseCallback = mMokoResponseCallback;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MokoBleManagerGattCallback();
    }

    public class MokoBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {

        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service != null) {
                lockedCharacteristic = service.getCharacteristic(LOCKED_UUID);
                enableLockedNotify();
                return true;
            }
            return false;
        }

        @Override
        protected void onDeviceDisconnected() {

        }

        @Override
        protected void onCharacteristicWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("onCharacteristicWrite");
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicWrite(characteristic.getValue());
        }

        @Override
        protected void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("onCharacteristicRead");
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicRead(characteristic.getValue());
        }

        @Override
        protected void onDescriptorWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor) {
            LogModule.e("onDescriptorWrite");
            String characteristicUUIDStr = descriptor.getCharacteristic().getUuid().toString().toLowerCase();
            if (lockedCharacteristic.getUuid().toString().toLowerCase().equals(characteristicUUIDStr))
                mMokoResponseCallback.onServicesDiscovered(gatt);
        }
    }

    public void enableLockedNotify() {
        setIndicationCallback(lockedCharacteristic).with(new DataReceivedCallback() {
            @Override
            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                final byte[] value = data.getValue();
                LogModule.e("onDataReceived");
                LogModule.e("device to app : " + MokoUtils.bytesToHexString(value));
                mMokoResponseCallback.onCharacteristicChanged(lockedCharacteristic, value);
            }
        });
        enableNotifications(lockedCharacteristic).enqueue();
    }

    public void disableLockedNotify() {
        disableNotifications(lockedCharacteristic).enqueue();
    }
}
