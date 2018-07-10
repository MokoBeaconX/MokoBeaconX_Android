#MokoBeaconX Android SDK Instruction DOC（English）

----

## 1. Import project

**1.1 Import "Module mokosupport" to root directory**

**1.2 Edit "settings.gradle" file**

```
include ':app', ':mokosupport'
```

**1.3 Edit "build.gradle" file under the APP project**


	dependencies {
		...
		compile project(path: ':mokosupport')
	}


----

## 2. How to use

**Initialize sdk at project initialization**

```
MokoSupport.getInstance().init(getApplicationContext());
```

**SDK provides three main functions:**

* Scan the device;
* Connect to the device;
* Send and receive data.

### 2.1 Scan the device

 **Start scanning**

```
MokoSupport.getInstance().startScanDevice(callback);
```

 **End scanning**

```
MokoSupport.getInstance().stopScanDevice();
```
 **Implement the scanning callback interface**

```java
/**
 * @ClassPath com.moko.support.callback.MokoScanDeviceCallback
 */
public interface MokoScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
```
* **Analysis `DeviceInfo` ; inferred `BeaconInfo`**

```
BeaconInfo beaconInfo = new BeaconXInfoParseableImpl().parseDeviceInfo(device);
```

Please refer to "Demo Project" to use `BeaconInfoParseableImpl` class. You can get some basic information from `BeaconInfo`, such as "Device Name", "MAC address", "RSSI" .

You can filter devices according to the 5th and 6th bytes in the field of broadcasting ServiceID---0xAAFE stands for Eddystone; 0x20FF stands for iBeacon; 0x10FF stands for custom device information witch includs MAC address, Device Name, Battery information etc.


```
if (((int) scanRecord[5] & 0xff) == 0xAA && ((int) scanRecord[6] & 0xff) == 0xFE) {
            length = (int) scanRecord[7];
            isEddystone = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x20 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isBeacon = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x10 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isDeviceInfo = true;
        }
```

### 2.2 Connect to the device


```
MokoSupport.getInstance().connDevice(context, address, mokoConnStateCallback);
```

When connecting to the device, context, MAC address and callback interface of connection status (`MokoConnStateCallback`) should be transferred in.


```java
public interface MokoConnStateCallback {

    /**
     * @Description  Connecting succeeds
     */
    void onConnectSuccess();

    /**
     * @Description  Disconnect
     */
    void onDisConnected();
}
```

"Demo Project" implements callback interface in Service. It broadcasts status to Activity after receiving the status, and could send and receive data after connecting to the device.

### 2.3 Send and receive data.

All the request data is encapsulated into **TASK**, and sent to the device in a **QUEUE** way.
SDK gets task status from task callback (`MokoOrderTaskCallback`) after sending tasks successfully.

* **Task**

At present, all the tasks sent from the SDK can be divided into 4 types:

> 1.  READ：Readable
> 2.  WRITE：Writable
> 3.  NOTIFY：Can be listened( Need to enable the notification property of the relevant characteristic values)
> 4.  WRITE_NO_RESPONSE：After enabling the notification property, send data to the device and listen to the data returned by device.

Encapsulated tasks are as follows:

|Task Class|Task Type|Function
|----|----|----
|`NotifyConfigTask`|NOTIFY|Enable notification property


Custom device information
--

|Task Class|Task Type|Function
|----|----|----
|`LockStateTask`|READ|Get Lock State; **0x00** stands for LOCKED and needs to be unlocked; **0x01** stands for UNLOCKED; **0x02** stands for Uulocked and automatic relock disabled.
|`LockStateTask`|WRITE|Set new password; AES encryption of 16 byte new password with 16 byte old password ( To prevent the new password from being broadcast in the clear, the client shall AES-128-ECB encrypt the new password with the existing password. The BeaconX shall perform the decryption with its existing password and set that value as the new password. ).
|`UnLockTask`|READ|Get a 128-bit challenge token. This token is for one-time use and cannot be replayed.To securely unlock the BeaconX, the host must write a one-time use unlock_token into the characteristic. To create the unlock_token, it first reads the randomly generated 16-byte challenge and generates it using AES-128-ECB.encrypt (key=password[16], text=challenge[16]).
|`UnLockTask`|WRITE|Unlock，If the result of this calculation matches the unlock_token written to the characteristic, the beacon is unlocked. Sets the LOCK STATE to 0x01 on success.
|`ManufacturerTask`|READ|Get manufacturer.
|`DeviceModelTask` |READ|Get product model.
|`ProductDateTask`|READ|Get production date.
| `HardwareVersionTask`|READ|Get hardware version.
|`FirmwareVersionTask`|READ|Get firmware version.
|`SoftwareVersionTask`|READ|Get software version.
|`BatteryTask`|READ|Get battery capacity.
| `WriteConfigTask`|WRITE_NO_RESPONSE|Write `ConfigKeyEnum.GET_DEVICE_MAC`，get MAC address.
| `WriteConfigTask`|WRITE_NO_RESPONSE|Write`ConfigKeyEnum.GET_DEVICE_NAME`，get device name.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setDeviceName(String deviceName)`，set device name（The length of the name cannot be more than 8）.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write`ConfigKeyEnum.GET_CONNECTABLE`，get evice connection status; 01:Connectable; 00：Unconnectable.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setConnectable(boolean isConnectable)`，Set the connection status.
|`ResetDeviceTask` |WRITE|Reset



iBeacon information
--

|Task Class|Task Type|Function
|----|----|----
|`WriteConfigTask` |WRITE_NO_RESPONSE|Write `ConfigKeyEnum.GET_IBEACON_UUID`，get iBeacon UUID.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setiBeaconUUID(String uuidHex)`，set iBeacon UUID(16bytes).
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write`ConfigKeyEnum.GET_IBEACON_INFO`，get iBeacon Major、Minor and advTxPower(RSSI@1m).
| `WriteConfigTask`|WRITE_NO_RESPONSE|Call `setiBeaconData(int major, int minor, int advTxPower)`，set iBeacon Major(2bytes)、Minor(2bytes) and advTxPower(RSSI@1m, 1bytes).


Eddystone information（URL,UID,TLM）
---

|Task Class|Task Type|Function
|----|----|----
| `AdvSlotTask` |WRITE|Switch SLOT. Please take `SlotEnum` as reference
| `AdvSlotDataTask` |READ|After switching the SLOT, get the current SLOT data and parse the returned data according to the SLOT type.

```
public void setSlotData(byte[] value) {
        int frameType = value[0];
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            switch (slotFrameTypeEnum) {
                case URL:
                    // URL：10cf014c6f766500
                    BeaconXParser.parseUrlData(slotData, value);
                    break;
                case TLM:
                    break;
                case UID:
                    BeaconXParser.parseUidData(slotData, value);
                    break;
            }
        }
    }
```

|Task Class|Task Type|Function
|----|----|----
| `AdvSlotDataTask` |WRITE|After switching the SLOT, set the current SLOT data

	UID data composition：SLOT type(0x00) + Namespace(10bytes) + Instance ID(6bytes)
	URL data composition：SLOT type(0x10) + URLScheme(1bytes) + URLContent(Max 17bytes)
	TLM data composition：SLOT type(0x20)
	NO_DATA data composition：0

|Task Class|Task Type|Function
|----|----|----
|`RadioTxPowerTask` |READ|Get current SLOT Tx Power.
|`RadioTxPowerTask`|WRITE|Set current SLOT Tx Power(1bytes). Please take `TxPowerEnum` as reference
|`AdvIntervalTask`|READ|Get current SLOT broadcasting Interval.
|`AdvIntervalTask`|WRITE|Set current SLOT broadcasting Interval(2bytes). Range：100ms- 4000ms. Example：0x03E8=1000 (Unit:ms).
|`AdvTxPowerTask`|WRITE|Set currnent SLOT advTxPower(RSSI@0m, 1bytes). Range：-127dBm—0dBm. Example：0xED=-19dBm.
|`WriteConfigTask` |WRITE_NO_RESPONSE|Write`ConfigKeyEnum.GET_SLOT_TYPE`，get the SLOT type of the five SLOTs. Please take `SlotFrameTypeEnum` as reference.


* **Create tasks**

The task callback (`MokoOrderTaskCallback`) and task type need to be passed when creating a task. Some tasks also need corresponding parameters to be passed.

Examples of creating tasks are as follows:

```
    /**
     * @Description   get LOCK STATE
     */
    public OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }
	...
    /**
     * @Description  Set Device Name
     */
    public OrderTask setDeviceName(String deviceName) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setDeviceName(deviceName);
        return writeConfigTask;
    }
	...
    /**
     * @Description   Switch SLOT
     */
    public OrderTask setSlot(SlotEnum slot) {
        AdvSlotTask advSlotTask = new AdvSlotTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advSlotTask.setData(slot);
        return advSlotTask;
    }
    }
```

* **Send tasks**

```
MokoSupport.getInstance().sendOrder(OrderTask... orderTasks);
```

The task can be one or more.

* **Task callback**

```java
/**
 * @ClassPath com.moko.support.callback.OrderCallback
 */
public interface MokoOrderTaskCallback {

    void onOrderResult(OrderTaskResponse response);

    void onOrderTimeout(OrderTaskResponse response);

    void onOrderFinish();
}
```
`void onOrderResult(OrderTaskResponse response);`

	After the task is sent to the device, the data returned by the device can be obtained by using the `onOrderResult` function, and you can determine witch class the task is according to the `response.orderType` function. The `response.responseValue` is the returned data.

`void onOrderTimeout(OrderTaskResponse response);`

	Every task has a default timeout of 3 seconds to prevent the device from failing to return data due to a fault and the fail will cause other tasks in the queue can not execute normally. After the timeout, the `onOrderTimeout` will be called back. You can determine witch class the task is according to the `response.orderType` function and then the next task continues.

`void onOrderFinish();`

	When the task in the queue is empty, `onOrderFinish` will be called back.

* **Listening task**

If the task belongs to `NOTIFY` and ` WRITE_NO_RESPONSE` task has been sent, the task is in listening state. When there is data returned from the device, the data will be sent in the form of broadcast, and the action of receiving broadcast is `MokoConstants.ACTION_RESPONSE_NOTIFY`.

```
String action = intent.getAction();
...
if (MokoConstants.ACTION_RESPONSE_NOTIFY.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    ...
                }
```

Get `OrderTaskResponse` from the **intent** of `onReceive`, and the corresponding **key** value is `MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK`.

## 4. Special instructions

> 1. AndroidManifest.xml of SDK has declared to access SD card and get Bluetooth permissions.
> 2. The SDK comes with logging, and if you want to view the log in the SD card, please to use "LogModule". The log path is : root directory of SD card/mokoBeaconX/mokoLog. It only records the log of the day and the day before.
> 3. Just connecting to the device successfully, it needs to delay 1 second before sending data, otherwise the device can not return data normally.
> 4. We suggest that sending and receiving data should be executed in the "Service". There will be a certain delay when the device returns data, and you can broadcast data to the "Activity" after receiving in the "Service". Please refer to the "Demo Project".















