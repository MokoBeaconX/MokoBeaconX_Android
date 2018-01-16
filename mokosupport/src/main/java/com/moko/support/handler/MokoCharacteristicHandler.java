package com.moko.support.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;

import com.moko.support.entity.MokoCharacteristic;
import com.moko.support.entity.OrderType;

import java.util.HashMap;
import java.util.List;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.handler.MokoCharacteristicHandler
 */
public class MokoCharacteristicHandler {
    private static MokoCharacteristicHandler INSTANCE;

    public static final String SERVICE_UUID_HEADER_SYSTEM = "0000ffc0";
    public static final String SERVICE_UUID_HEADER_PARAMS = "0000fff0";
    public HashMap<OrderType, MokoCharacteristic> mokoCharacteristicMap;

    private MokoCharacteristicHandler() {
        //no instance
        mokoCharacteristicMap = new HashMap<>();
    }

    public static MokoCharacteristicHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoCharacteristicHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoCharacteristicHandler();
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<OrderType, MokoCharacteristic> getCharacteristics(BluetoothGatt gatt) {
        if (mokoCharacteristicMap != null && !mokoCharacteristicMap.isEmpty()) {
            mokoCharacteristicMap.clear();
        }
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            String serviceUuid = service.getUuid().toString();
            if (TextUtils.isEmpty(serviceUuid)) {
                continue;
            }
            if (serviceUuid.startsWith("00001800")||serviceUuid.startsWith("00001801")) {
                continue;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_SYSTEM)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.deviceInfo.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.deviceInfo, new MokoCharacteristic(characteristic, OrderType.deviceInfo));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.deviceInfoWrite.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.deviceInfoWrite, new MokoCharacteristic(characteristic, OrderType.deviceInfoWrite));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_PARAMS)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.temperature.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.temperature, new MokoCharacteristic(characteristic, OrderType.temperature));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.hour.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.hour, new MokoCharacteristic(characteristic, OrderType.hour));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.minute.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.minute, new MokoCharacteristic(characteristic, OrderType.minute));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.delayHour.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.delayHour, new MokoCharacteristic(characteristic, OrderType.delayHour));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.delayMinute.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.delayMinute, new MokoCharacteristic(characteristic, OrderType.delayMinute));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.scale.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.scale, new MokoCharacteristic(characteristic, OrderType.scale));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.error.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.error, new MokoCharacteristic(characteristic, OrderType.error));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.isRunning.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.isRunning, new MokoCharacteristic(characteristic, OrderType.isRunning));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.temperatureTarget.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.temperatureTarget, new MokoCharacteristic(characteristic, OrderType.temperatureTarget));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.isConnectedToWifi.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.isConnectedToWifi, new MokoCharacteristic(characteristic, OrderType.isConnectedToWifi));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.wifiSSID.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.wifiSSID, new MokoCharacteristic(characteristic, OrderType.wifiSSID));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.wifiPassword.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.wifiPassword, new MokoCharacteristic(characteristic, OrderType.wifiPassword));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.wifiSecurity.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.wifiSecurity, new MokoCharacteristic(characteristic, OrderType.wifiSecurity));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.mqttId.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.mqttId, new MokoCharacteristic(characteristic, OrderType.mqttId));
                        continue;
                    }

                }
            }
//            LogModule.i("service uuid:" + service.getUuid().toString());
//            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristics) {
//                LogModule.i("characteristic uuid:" + characteristic.getUuid().toString());
//                LogModule.i("characteristic properties:" + Utils.getCharPropertie(characteristic.getProperties()));
//                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : descriptors) {
//                    LogModule.i("descriptor uuid:" + descriptor.getUuid().toString());
//                    LogModule.i("descriptor value:" + Utils.bytesToHexString(descriptor.getValue()));
//                }
//            }
        }
        return mokoCharacteristicMap;
    }
}
