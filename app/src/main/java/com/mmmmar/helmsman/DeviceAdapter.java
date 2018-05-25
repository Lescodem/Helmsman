package com.mmmmar.helmsman;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private List<BluetoothDevice> devices = new ArrayList<>();

    private Context context;

    private LayoutInflater layoutInflater;

    private int selectItem = -1;

    DeviceAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device) {
        devices.add(device);
        notifyDataSetChanged();
    }

    public BluetoothDevice getDevice(int position) {
        return devices.get(position);
    }

    public void setSelectItem(int position) {
        selectItem = position;
        notifyDataSetChanged();
    }

    public int getSelectItem() {
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
        BluetoothDevice device = devices.get(position);
        String deviceName = device.getName() != null ? device.getName() : device.getAddress();

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
