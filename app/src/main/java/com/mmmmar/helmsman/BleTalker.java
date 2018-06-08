package com.mmmmar.helmsman;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class BleTalker {

    private static final String TAG = "BleTalker";

    private final Queue<byte[]> dataQueue = new LinkedList<>();
    private UUID serviceUUID;
    private UUID characteristicUUID;

    private boolean dataWriting = false;

    public BleTalker(String uuidService, String uuidCharacteristic) {
        init(uuidService, uuidCharacteristic);
    }

    public void reset(String uuidService, String uuidCharacteristic) {
        dataQueue.clear();
        init(uuidService, uuidCharacteristic);
    }

    public boolean valid(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) return false;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        return characteristic != null;
    }

    public void send(BluetoothGatt gatt, byte[] data) {
        Log.d(TAG, "data: " + Arrays.toString(data));
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt);
        assert characteristic != null;
        if (dataWriting) {
            dataQueue.add(data);
        } else {
            characteristic.setValue(data);
            gatt.writeCharacteristic(characteristic);
            dataWriting = true;
        }
    }

    public void next(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt);
        assert characteristic != null;
        byte[] data = dataQueue.poll();
        if (data == null) {
            dataWriting = false;
            return;
        }
        characteristic.setValue(data);
        gatt.writeCharacteristic(characteristic);
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) return null;
        return service.getCharacteristic(characteristicUUID);
    }

    private void init(String uuidService, String uuidCharacteristic) {
        serviceUUID = UUID.fromString(uuidService);
        characteristicUUID = UUID.fromString(uuidCharacteristic);
    }
}
