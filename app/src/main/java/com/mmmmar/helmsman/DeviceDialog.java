package com.mmmmar.helmsman;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class DeviceDialog extends AlertDialog {

    private static final String TAG = "DeviceDialog";

    private static final int TIME_PROGRESS_ANIMATION = 1000;

    private BleScanner bleScanner;

    private View progressOuter;
    private View progressInner;

    private AnimatorSet progressAnimatorSet;

    private DeviceAdapter deviceAdapter;

    private Button btn_positive;

    private DeviceChooseListener deviceChooseListener;


    public interface DeviceChooseListener {
        void onChooseDevice(BluetoothDevice device);
    }

    DeviceDialog(@NonNull Context context, @NonNull BleScanner scanner, DeviceChooseListener listener) {
        super(context);
        bleScanner = scanner;
        deviceChooseListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device);
        progressInner = findViewById(R.id.progress_inner);
        progressOuter = findViewById(R.id.progress_outer);

        Button negativeBtn = findViewById(R.id.btn_negative);
        assert negativeBtn != null;
        negativeBtn.setOnClickListener(v -> cancel());

        btn_positive = findViewById(R.id.btn_positive);
        assert btn_positive != null;
        btn_positive.setOnClickListener(v -> {
            if (deviceChooseListener != null) {
                deviceChooseListener.onChooseDevice(
                        deviceAdapter.getDevice(deviceAdapter.getSelectItem()));
            }
            cancel();
        });

        ListView deviceListView = findViewById(R.id.list_devices);
        assert deviceListView != null;
        deviceListView.setEmptyView(findViewById(R.id.empty_list));
        deviceAdapter = new DeviceAdapter(getContext());
        deviceListView.setAdapter(deviceAdapter);
        deviceListView.setOnItemClickListener(this::onDeviceItemClick);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (progressAnimatorSet == null && hasFocus) {
            // 开启动画。
            int translation = progressOuter.getWidth() - progressInner.getWidth();
            ObjectAnimator translationRight = ObjectAnimator.ofFloat(progressInner, "translationX", translation);
            translationRight.setDuration(TIME_PROGRESS_ANIMATION);
            translationRight.setRepeatCount(ValueAnimator.INFINITE);
            translationRight.setRepeatMode(ValueAnimator.REVERSE);
            progressAnimatorSet = new AnimatorSet();
            progressAnimatorSet.play(translationRight);
            progressAnimatorSet.start();
            // 启动蓝牙设备搜索。
            bleScanner.start(deviceAdapter::addDevice);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 关闭动画。
        if (progressAnimatorSet != null) {
            progressAnimatorSet.end();
        }
        bleScanner.stop();
    }

    private void onDeviceItemClick(AdapterView<?> parent, View view, int position, long id) {
        btn_positive.setEnabled(true);
        deviceAdapter.setSelectItem(position);
    }
}
