package com.mmmmar.helmsman;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BleScanner {

    private static final String TAG = "BleScanner";

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> deviceSet = new HashSet<>();
    private ScanCallback scanCallback;

    private BluetoothAdapter.LeScanCallback leScanCallback = (device, rssi, scanRecord) -> {
        if (deviceSet.contains(device)) return;
        Log.d(TAG, "device : " + device + ", rssi : " + rssi + ", record : " + Arrays.toString(scanRecord));
        if (scanCallback != null) {
            scanCallback.onScan(device);
        }
        deviceSet.add(device);
    };

    public BleScanner() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.enable()) {
            throw new IllegalStateException("无法访问蓝牙设备！");
        }
    }

    public void start(ScanCallback callback) {
        scanCallback = callback;
        // TODO: 2018/5/25 按照服务类型过滤设备。
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    public void stop() {
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    public interface ScanCallback {
        void onScan(BluetoothDevice device);
    }
}
