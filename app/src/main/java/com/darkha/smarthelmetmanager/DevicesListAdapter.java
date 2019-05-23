package com.darkha.smarthelmetmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ListDataWrapper {
    private String deviceAddress;
    private String deviceName;
    private boolean recognized;
    private long lastConnectedTime;

    public ListDataWrapper(String deviceName, String deviceAddress, boolean recognized, long lastConnectedTime) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.recognized = recognized;
        this.lastConnectedTime = lastConnectedTime;
    }

    public ListDataWrapper(JSONObject object) {
        try {
            this.deviceName = object.getString("name");
            this.deviceAddress = object.getString("address");
            this.recognized = object.getBoolean("recognized");
            this.lastConnectedTime = object.getLong("last_time");
        } catch (JSONException e) {
//            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj != null) {
            if (obj.getClass().equals(this.getClass())) {
                return this.getAddress().equals(((ListDataWrapper) obj).getAddress());
            }
        }
        return false;
    }

    String getName() {
        return deviceName;
    }

    String getAddress() {
        return deviceAddress;
    }

    long getLastConnectedTimestamp() {
        return lastConnectedTime;
    }

    String getLastConnectedTime() {
        return DateFormat.format("hh:mm dd/MM/yyyy", new Date(lastConnectedTime)).toString();
    }

    public boolean isRecognized() {
        return recognized;
    }

    @NonNull
    @Override
    public String toString() {
        return toJSON().toString();
    }

    @NonNull
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", deviceName);
            jsonObject.put("address", deviceAddress);
            jsonObject.put("recognized", recognized);
            jsonObject.put("last_time", lastConnectedTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}

public class DevicesListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Context context;
    private List<ListDataWrapper> data;
    private OnListItemButtonClickListener onInfoClickListener, onDeleteClickListener;
    private boolean withButtons;

    public DevicesListAdapter(Context context, @Nullable List<ListDataWrapper> data, boolean withButtons) {
        if (data != null) {
            this.data = new ArrayList<>(data);
        } else {
            this.data = new ArrayList<>();
        }
        this.context = context;
        this.withButtons = withButtons;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private boolean contains(ListDataWrapper wrapper) {
        for (ListDataWrapper item : data) {
            if (item.equals(wrapper)) {
                return true;
            }
        }
        return false;
    }

    public void addDevice(ListDataWrapper wrapper) {
        if (!contains(wrapper)) {
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
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.layout_list_item, null);
        ListDataWrapper device = data.get(position);
        ((TextView) view.findViewById(R.id.text_device_name)).setText(device.getName());
        ((TextView) view.findViewById(R.id.text_device_mac)).setText(device.getAddress());

        if (device.isRecognized()) {
            ((ImageView) view.findViewById(R.id.image_device_icon)).setImageResource(R.drawable.ic_smart_helmet);
            view.findViewById(R.id.button_info).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.image_device_icon)).setImageResource(R.drawable.ic_devices);
            view.findViewById(R.id.button_info).setVisibility(View.GONE);
        }
        view.findViewById(R.id.button_delete).setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onEvent(device);
            }
        });
        view.findViewById(R.id.button_info).setOnClickListener(v -> {
            if (onInfoClickListener != null) {
                onInfoClickListener.onEvent(device);
            }
        });
        if (!withButtons) {
            view.findViewById(R.id.button_info).setVisibility(View.GONE);
            view.findViewById(R.id.button_delete).setVisibility(View.GONE);
        }
        return view;
    }

    List<ListDataWrapper> getData() {
        return this.data;
    }

    void setData(List<ListDataWrapper> data) {
        this.data = data;
    }

    public void clear() {
        this.data.clear();
    }

    public void setOnInfoClickListener(OnListItemButtonClickListener onInfoClickListener) {
        this.onInfoClickListener = onInfoClickListener;
    }

    public void setOnDeleteClickListener(OnListItemButtonClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }
}
