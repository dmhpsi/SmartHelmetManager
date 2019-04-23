package com.darkha.smarthelmetmanager;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ListDataWrapper {
    private BluetoothDevice device;
    private boolean recognized;

    public ListDataWrapper(BluetoothDevice device, boolean recognized) {
        this.device = device;
        this.recognized = recognized;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj != null) {
            if (obj.getClass().equals(this.device.getClass())) {
                return this.device.equals(obj);
            }
        }
        return false;
    }

    String getName() {
        return device.getName();
    }

    String getAddress() {
        return device.getAddress();
    }

    public boolean isRecognized() {
        return recognized;
    }

    BluetoothDevice getDevice() {
        return device;
    }
}

public class DevicesListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Context context;
    private List<ListDataWrapper> data;

    public DevicesListAdapter(Context context, @Nullable List<ListDataWrapper> data) {
        if (data != null) {
            this.data = new ArrayList<>(data);
        } else {
            this.data = new ArrayList<>();
        }
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addDevice(ListDataWrapper wrapper) {
        if (!data.contains(wrapper)) {
            this.data.add(wrapper);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ListDataWrapper getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.layout_list_item, null);
        ((TextView) vi.findViewById(R.id.text_device_name)).setText(data.get(position).getName());
        ((TextView) vi.findViewById(R.id.text_device_mac)).setText(data.get(position).getAddress());
        return vi;
    }

    public void clear() {
        this.data.clear();
    }
}
