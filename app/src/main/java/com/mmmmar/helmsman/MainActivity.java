package com.mmmmar.helmsman;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int TIME_STATE_LOG_ANIMATION = 250;
    private static final int REQUEST_ENABLE_BT = 1;

    private TextView tv_log1;
    private TextView tv_log2;

    private float log1TextSize;
    private float log2Alpha;

    private AnimatorSet logAnimatorSet;

    private DeviceDialog deviceDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView btn_setting = findViewById(R.id.btn_setting);
        btn_setting.setBackgroundDrawable(createBtnDrawable());
        btn_setting.setOnClickListener(this::onSettingButtonClick);

        TextView btn_device = findViewById(R.id.btn_device);
        btn_device.setBackgroundDrawable(createBtnDrawable());
        btn_device.setOnClickListener(this::onDeviceButtonClick);

        tv_log1 = findViewById(R.id.tv_log1);
        tv_log2 = findViewById(R.id.tv_log2);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (deviceDialog != null && deviceDialog.isShowing()) {
            deviceDialog.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            showDeviceDialog();
        }
    }

    private void onDeviceButtonClick(View view) {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        } else {
            showDeviceDialog();
//        }
    }

    private void onSettingButtonClick(View view) {

    }

    private void showDeviceDialog() {
        deviceDialog = new DeviceDialog(this, null);
        deviceDialog.show();
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
}
