package com.mmmmar.helmsman;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String BUNDLE_LOG1 = "log1";
    private static final String BUNDLE_LOG2 = "log2";
    private static final String BUNDLE_STATE = "state";

    private static final int TIME_STATE_LOG_ANIMATION = 250;
    private static final int REQUEST_ENABLE_BT = 1;

    private TextView tv_log1;
    private TextView tv_log2;
    private float log1TextSize;
    private float log2Alpha;
    private AnimatorSet logAnimatorSet;

    private TextView btn_device;

    private DeviceDialog deviceDialog;

    private BleService.Op bleOp;
    private ServiceConnection bleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleOp = (BleService.Op) service;
            bleOp.setStateListener(new BleStateListener());
            toyLogic.setCommandConsumer(bleOp::send);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    private ToyLogic toyLogic = new ToyLogic();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView btn_setting = findViewById(R.id.btn_setting);
        btn_setting.setBackground(createBtnDrawable());
        btn_setting.setOnClickListener(this::onSettingButtonClick);

        btn_device = findViewById(R.id.btn_device);
        btn_device.setBackground(createBtnDrawable());
        btn_device.setOnClickListener(this::onDeviceConnectClick);

        tv_log1 = findViewById(R.id.tv_log1);
        tv_log2 = findViewById(R.id.tv_log2);

        ControlView cl_left = findViewById(R.id.ctl_left);
        ControlView cl_right = findViewById(R.id.ctl_right);
        cl_left.setProgressListener(toyLogic::handleLeft);
        cl_right.setProgressListener(toyLogic::handleRight);

        if (savedInstanceState  != null) {
            tv_log1.setText(savedInstanceState.getString(BUNDLE_LOG1));
            tv_log2.setText(savedInstanceState.getString(BUNDLE_LOG2));
            switchDeviceBtn(savedInstanceState.getInt(BUNDLE_STATE));
        }

        Intent intent = new Intent(this, BleService.class);
        // 先启动服务，再绑定服务，防止Activity重建时Service被销毁。
        startService(intent);
        bindService(intent, bleServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            showDeviceDialog();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "save instance, log1 : " + tv_log1.getText().toString()
                + ", log2 : " + tv_log2.getText().toString()
                + ", state : " + bleOp.getState());
        outState.putString(BUNDLE_LOG1, tv_log1.getText().toString());
        outState.putString(BUNDLE_LOG2, tv_log2.getText().toString());
        outState.putInt(BUNDLE_STATE, bleOp.getState());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (deviceDialog != null && deviceDialog.isShowing()) {
            deviceDialog.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(bleServiceConnection);
    }

    private void onDeviceConnectClick(View view) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            showDeviceDialog();
        }
    }

    private void onDeviceDisconnectClick(View view) {
        bleOp.disconnect();
    }

    private void onSettingButtonClick(View view) {

    }

    private void showDeviceDialog() {
        deviceDialog = new DeviceDialog(this, new BleScanner(), this::onDeviceChoose);
        deviceDialog.show();
    }

    private void onDeviceChoose(BluetoothDevice device) {
        bleOp.connect(device);
    }

    private Drawable createBtnDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, new ButtonDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        drawable.addState(new int[]{}, new ButtonDrawable(getResources().getColor(R.color.colorPrimary)));
        return drawable;
    }

    private void showStateLog(String state) {
        String log1 = tv_log1.getText().toString();

        if (logAnimatorSet == null) {
            logAnimatorSet = new AnimatorSet();
            // 计算动画数据。
            log2Alpha = tv_log2.getAlpha();
            float log2TextSize = DensityUtil.px2sp(this, tv_log2.getTextSize());
            log1TextSize = DensityUtil.px2sp(this, tv_log1.getTextSize());
            float translateLog1 = tv_log2.getTop() - tv_log1.getTop();

            ObjectAnimator log1TextSizeAn = ObjectAnimator.ofFloat(tv_log1, "textSize", log1TextSize, log2TextSize);
            ObjectAnimator log1TranslateAn = ObjectAnimator.ofFloat(tv_log1, "translationY", translateLog1);
            ObjectAnimator log2AlphaAn = ObjectAnimator.ofFloat(tv_log2, "alpha", 0);

            logAnimatorSet.play(log1TextSizeAn).with(log1TranslateAn).with(log2AlphaAn);
            logAnimatorSet.setDuration(TIME_STATE_LOG_ANIMATION);
        }
        if (logAnimatorSet.isRunning()) {
            logAnimatorSet.end();
            logAnimatorSet.removeAllListeners();
        }
        logAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO: 2018/5/23 恢复View初始状态然后重新设置文字。有更好的实现方法？
                tv_log1.setTextSize(log1TextSize);
                tv_log1.setTranslationY(0);
                tv_log1.setText(state);
                tv_log2.setAlpha(log2Alpha);
                tv_log2.setText(log1);
            }
        });
        logAnimatorSet.start();
    }

    private void switchDeviceBtn(@BleService.ServiceState int state) {
        switch (state) {
            case BleService.STATE_UNCONNECTED:
                btn_device.setEnabled(true);
                btn_device.setText(R.string.btn_device);
                btn_device.setOnClickListener(MainActivity.this::onDeviceConnectClick);
                break;
            case BleService.STATE_CONNECTING:
                btn_device.setEnabled(false);
                break;
            case BleService.STATE_CONNECTED:
                btn_device.setEnabled(true);
                btn_device.setText(R.string.btn_disconnect);
                btn_device.setOnClickListener(MainActivity.this::onDeviceDisconnectClick);
                break;
        }
    }

    private class BleStateListener implements BleService.StateListener {

        @Override
        public void onServerConnect() {
            showStateLog(getString(R.string.state_connected));
            switchDeviceBtn(BleService.STATE_CONNECTED);
        }

        @Override
        public void onServerConnecting() {
            showStateLog(getString(R.string.state_connecting));
            switchDeviceBtn(BleService.STATE_CONNECTING);
        }

        @Override
        public void onServerDisconnect() {
            showStateLog(getString(R.string.state_not_connect));
            switchDeviceBtn(BleService.STATE_UNCONNECTED);
        }

        @Override
        public void onServerError(String msg) {

        }

        @Override
        public void onServerReply(byte[] data) {

        }
    }
}
