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

public class BleService extends Service {

    private static final String TAG = "BleService";

    public static final int STATE_UNCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    @IntDef({STATE_CONNECTED, STATE_CONNECTING, STATE_UNCONNECTED})
    public @interface ServiceState {}

    @ServiceState
    private int serviceState = STATE_UNCONNECTED;

    private Impl impl = new Impl();

    private BluetoothGattCallback gattCallback = new BleGattCbSynWrapper(new BleGattCallback());
    private BluetoothGatt gattService;

    private BleTalker bleTalker;

    @Override
    public void onCreate() {
        super.onCreate();
        String service = getString(R.string.uuid_service);
        String characteristic = getString(R.string.uuid_characteristic);
        bleTalker = new BleTalker(service, characteristic);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return impl;
    }

    public interface BleListener {
        void onServerConnect();
        void onServerConnecting();
        void onServerDisconnect();
        void onServerError(String msg);
    }

    public class Impl extends Binder {

        private BleListener bleListener;

        public void setBleListener(BleListener bleListener) {
            this.bleListener = bleListener;
        }

        public void connect(BluetoothDevice device) {
            if (serviceState == STATE_UNCONNECTED) {
                serviceState = STATE_CONNECTING;
                impl.bleListener.onServerConnecting();
                gattService = device.connectGatt(BleService.this, true, gattCallback);
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
            // TODO: 2018/6/1 不清楚参数status，暂不使用。
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                serviceState = STATE_UNCONNECTED;
                impl.bleListener.onServerDisconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // ignore "status".
            SettingManager manager = SettingManager.getInstance(BleService.this);
            bleTalker.reset(manager.getServiceValue(), manager.getCharacteristicValue());
            if (bleTalker.valid(gatt)) {
                serviceState = STATE_CONNECTED;
                impl.bleListener.onServerConnect();
            } else {
                impl.bleListener.onServerError(getString(R.string.err_device_irrelevant));
                gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // ignore "status".
            bleTalker.next(gatt);
        }
    }
}
