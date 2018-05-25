package com.mmmmar.helmsman;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class TargetToy {

    private final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public static boolean isServiceAccept(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID_SERVICE);
        if (service == null) return false;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHARACTERISTIC);
        return characteristic != null;
    }
}
