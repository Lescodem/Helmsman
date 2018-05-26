package com.mmmmar.helmsman;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import java.util.Arrays;

public class BleService extends Service {

    private static final String TAG = "BleService";

    public static final int STATE_UNCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    @IntDef({STATE_CONNECTED, STATE_CONNECTING, STATE_UNCONNECTED})
    public @interface ServiceState {}

    @ServiceState
    private int serviceState = STATE_UNCONNECTED;

    private Op op = new Op();
    private BluetoothGattCallback gattCallback = new BleGattCbSynWrapper(new BleGattCallback());
    private BluetoothGatt gattService;

    private BleTalker bleTalker = new BleTalker();

    @Override
    public IBinder onBind(Intent intent) {
        return op;
    }

    public interface StateListener {
        void onServerConnect();
        void onServerConnecting();
        void onServerDisconnect();
        void onServerError(String msg);
        void onServerReply(byte[] data);
    }

    public class Op extends Binder {

        private StateListener stateListener;

        public void setStateListener(StateListener stateListener) {
            this.stateListener = stateListener;
        }

        public void connect(BluetoothDevice device) {
            if (serviceState == STATE_UNCONNECTED) {
                serviceState = STATE_CONNECTING;
                gattService = device.connectGatt(BleService.this, true, gattCallback);
                op.stateListener.onServerConnecting();
            }
        }

        public void disconnect() {
            if (serviceState == STATE_CONNECTED) {
                gattService.disconnect();
            }
        }

        public void send(byte[] data) {
            if (serviceState == STATE_CONNECTED) {
                bleTalker.send(gattService, data);
            }
        }

        public @ServiceState int getState() {
            return serviceState;
        }
    }

    private class BleGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                serviceState = STATE_UNCONNECTED;
                op.stateListener.onServerDisconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && BleTalker.isServiceAccept(gatt)) {
                serviceState = STATE_CONNECTED;
                op.stateListener.onServerConnect();
            } else {
                gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            bleTalker.next(gatt);
        }
    }
}
