package com.mmmmar.helmsman;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceDialog extends AlertDialog {

    private static final String TAG = "DeviceDialog";

    private static final int TIME_PROGRESS_ANIMATION = 1000;

    private View progressOuter;
    private View progressInner;

    private AnimatorSet progressAnimatorSet;

    private DeviceAdapter deviceAdapter;

    private Button btn_positive;

    private DeviceListener deviceListener;

    public interface DeviceListener {
        void onChooseDevice(FooDevice device);
    }

    DeviceDialog(@NonNull Context context, DeviceListener listener) {
        super(context);
        deviceListener = listener;
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
            if (deviceListener != null) {
                deviceListener.onChooseDevice(deviceAdapter.getDevice(deviceAdapter.getSelectItem()));
            }
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
        // 开启动画。
        if (progressAnimatorSet == null && hasFocus) {
            int translation = progressOuter.getWidth() - progressInner.getWidth();
            ObjectAnimator translationRight = ObjectAnimator.ofFloat(progressInner, "translationX", translation);
            translationRight.setDuration(TIME_PROGRESS_ANIMATION);
            translationRight.setRepeatCount(ValueAnimator.INFINITE);
            translationRight.setRepeatMode(ValueAnimator.REVERSE);
            progressAnimatorSet = new AnimatorSet();
            progressAnimatorSet.play(translationRight);
            progressAnimatorSet.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 关闭动画。
        if (progressAnimatorSet != null) {
            progressAnimatorSet.end();
        }
    }

    private void onDeviceItemClick(AdapterView<?> parent, View view, int position, long id) {
        btn_positive.setEnabled(true);
        deviceAdapter.setSelectItem(position);
    }


    private static class FooDevice {
        private String name;

        FooDevice(String deviceName) {
            this.name = deviceName;
        }

        public String getName() {
            return name;
        }
    }

    private static class DeviceAdapter extends BaseAdapter {

        private List<FooDevice> devices = new ArrayList<>();
        private Context context;
        private LayoutInflater layoutInflater;
        private int selectItem = -1;

        DeviceAdapter(Context context) {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);
        }

        void addDevice(FooDevice device) {
            devices.add(device);
            notifyDataSetChanged();
        }

        FooDevice getDevice(int position) {
            return devices.get(position);
        }

        void setSelectItem(int position) {
            selectItem = position;
            notifyDataSetChanged();
        }

        int getSelectItem() {
            return selectItem;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                View layout = layoutInflater.inflate(R.layout.item_device, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.tv_device = layout.findViewById(R.id.tv_device);
                convertView = layout;
                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            FooDevice device = devices.get(position);
            String deviceName = device.getName() != null ? device.getName() : "未知设备";

            Drawable drawable = context.getResources()
                    .getDrawable(position == selectItem
                            ? R.drawable.ic_radio_button_checked : R.drawable.ic_radio_button_unchecked);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            holder.tv_device.setCompoundDrawables(drawable, null, null, null);

            holder.tv_device.setText(deviceName);

            return convertView;
        }

        private static class ViewHolder {
            TextView tv_device;
        }

    }
}
