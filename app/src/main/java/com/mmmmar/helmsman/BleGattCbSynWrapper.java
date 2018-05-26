package com.mmmmar.helmsman;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public class BleGattCbSynWrapper extends BluetoothGattCallback {

    private static final String TAG = "BleGattCbSynWrapper";

    private Handler handler = new Handler(Looper.getMainLooper());

    private BluetoothGattCallback callback;

    public BleGattCbSynWrapper(BluetoothGattCallback callback) {
        if (callback == null) {
            throw new NullPointerException("BluetoothGattCallback is null");
        }
        this.callback = callback;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "onConnectionStateChange -> (" + status + ", " + newState + ")");
        handler.post(() -> callback.onConnectionStateChange(gatt, status, newState));
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onServicesDiscovered -> (" + status + ")");
        handler.post(() ->  callback.onServicesDiscovered(gatt, status));
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead -> (" + status + ")");
        handler.post(() -> callback.onCharacteristicRead(gatt, characteristic, status));
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite -> (" + status + ")");
        handler.post(() -> callback.onCharacteristicWrite(gatt, characteristic, status));
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged");
        handler.post(() -> callback.onCharacteristicChanged(gatt, characteristic));
    }
}
