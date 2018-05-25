package com.mmmmar.helmsman;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

public class BleService extends Service {

    private static final String TAG = "BleService";

    public static final int STATE_UNCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    @IntDef({STATE_CONNECTED, STATE_CONNECTING, STATE_UNCONNECTED})
    public  @interface ServiceState {}

    private Op op = new Op();
    private BleGattCallback gattCallback = new BleGattCallback();
    private BluetoothGatt gattService;

    @ServiceState
    private int serviceState = STATE_UNCONNECTED;

    private Handler handler = new Handler();

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

        public StateListener getStateListener() {
            return stateListener;
        }

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
            gattService.disconnect();
        }

        public void send(byte[] data) {

        }
    }

    private class BleGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange -> (" + status + ", " + newState + ")");
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                handler.post(() -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        serviceState = STATE_UNCONNECTED;
                        op.stateListener.onServerDisconnect();
                    } else {
                        op.stateListener.onServerError(getString(R.string.msg_disconnect));
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered -> (" + status + ")");
            if (status == BluetoothGatt.GATT_SUCCESS && TargetToy.isServiceAccept(gatt)) {
                handler.post(() -> {
                    serviceState = STATE_CONNECTED;
                    op.stateListener.onServerConnect();
                });
            } else {
                gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }
    }



}
