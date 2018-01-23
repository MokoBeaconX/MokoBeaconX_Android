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

    public static final String SERVICE_UUID_HEADER_DEVICE = "0000180a";
    public static final String SERVICE_UUID_HEADER_BATTERY = "0000180f";
    public static final String SERVICE_UUID_HEADER_NOTIFY = "e62a0001";
    public static final String SERVICE_UUID_HEADER_EDDYSTONE = "a3c87500";
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
            if (serviceUuid.startsWith("00001800") || serviceUuid.startsWith("00001801")) {
                continue;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_DEVICE)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }

                    if (characteristicUuid.equals(OrderType.manufacturer.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.manufacturer, new MokoCharacteristic(characteristic, OrderType.manufacturer));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.deviceModel.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.deviceModel, new MokoCharacteristic(characteristic, OrderType.deviceModel));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.productDate.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.productDate, new MokoCharacteristic(characteristic, OrderType.productDate));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.hardwareVersion.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.hardwareVersion, new MokoCharacteristic(characteristic, OrderType.hardwareVersion));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.firmwareVersion.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.firmwareVersion, new MokoCharacteristic(characteristic, OrderType.firmwareVersion));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.softwareVersion.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.softwareVersion, new MokoCharacteristic(characteristic, OrderType.softwareVersion));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_BATTERY)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.battery.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.battery, new MokoCharacteristic(characteristic, OrderType.battery));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_NOTIFY)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.notifyConfig.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.notifyConfig, new MokoCharacteristic(characteristic, OrderType.notifyConfig));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.writeConfig.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.writeConfig, new MokoCharacteristic(characteristic, OrderType.writeConfig));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_EDDYSTONE)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advSlot.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.advSlot, new MokoCharacteristic(characteristic, OrderType.advSlot));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advInterval.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.advInterval, new MokoCharacteristic(characteristic, OrderType.advInterval));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.radioTxPower.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.radioTxPower, new MokoCharacteristic(characteristic, OrderType.radioTxPower));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advTxPower.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.advTxPower, new MokoCharacteristic(characteristic, OrderType.advTxPower));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.lockState.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.lockState, new MokoCharacteristic(characteristic, OrderType.lockState));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.unLock.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.unLock, new MokoCharacteristic(characteristic, OrderType.unLock));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advSlotData.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.advSlotData, new MokoCharacteristic(characteristic, OrderType.advSlotData));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.resetDevice.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.resetDevice, new MokoCharacteristic(characteristic, OrderType.resetDevice));
                        continue;
                    }
                }
            }
//            LogModule.i("service uuid:" + service.getUuid().toString());
//            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristicList) {
//                LogModule.i("   characteristic uuid:" + characteristic.getUuid().toString());
//                LogModule.i("   characteristic properties:" + MokoUtils.getCharPropertie(characteristic.getProperties()));
//                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : descriptors) {
//                    LogModule.i("       descriptor uuid:" + descriptor.getUuid().toString());
//                    LogModule.i("       descriptor value:" + MokoUtils.bytesToHexString(descriptor.getValue()));
//                }
//            }
        }
        return mokoCharacteristicMap;
    }
}
