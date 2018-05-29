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

    private final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private final Queue<byte[]> dataQueue = new LinkedList<>();
    private boolean dataWriting = false;

    public static boolean isServiceAccept(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID_SERVICE);
        if (service == null) return false;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHARACTERISTIC);
        return characteristic != null;
    }

    public void send(BluetoothGatt gatt, byte[] data) {
        Log.d(TAG, "data: " + Arrays.toString(data));
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt);
        if (characteristic == null) {
            Log.e(TAG, "send: can't find characteristic");
            return;
        }
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
        if (characteristic == null) {
            Log.e(TAG, "next: can't find characteristic");
            return;
        }
        byte[] data = dataQueue.poll();
        if (data == null) {
            dataWriting = false;
            return;
        }
        characteristic.setValue(data);
        gatt.writeCharacteristic(characteristic);
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID_SERVICE);
        if (service == null) return null;
        return service.getCharacteristic(UUID_CHARACTERISTIC);
    }
}
