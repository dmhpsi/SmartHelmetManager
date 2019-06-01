package com.darkha.smarthelmetmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.google.android.gms.common.api.CommonStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DevicesFragment extends Fragment {

    private static final int RC_BARCODE_CAPTURE = 9001;
    BluetoothHandler bluetooth;
    DevicesListAdapter adapter;
    private OnFragmentInteractionListener mListener;
    private TinyDB tinyDB;

    public DevicesFragment() {
        // Required empty public constructor
    }

    public void setServices(BluetoothHandler bluetooth, TinyDB tinyDB) {
        this.bluetooth = bluetooth;
        this.tinyDB = tinyDB;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        Switch button = view.findViewById(R.id.button_discover);
//        ProgressBar progressBar = view.findViewById(R.id.progress_discovery);
        ProgressBar loadingProgressBar = view.findViewById(R.id.content_loading);
        loadingProgressBar.setIndeterminate(true);

        DevicesListAdapter knownAdapter = new DevicesListAdapter(getActivity(), null, true);
        knownAdapter.setOnInfoClickListener((device) -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Device detail")
                    .setMessage("Name: " + device.getName() + "\nAddress: " + device.getAddress() + "\nLast connected: " + device.getLastConnectedTime())
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });

        knownAdapter.setOnDeleteClickListener(device -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm delete")
                    .setMessage("Are you sure want to delete " + device.getName() + "?")
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        List<String> authenticatedDevices = tinyDB.getListString(getString(R.string.key_authenticated_devices));
                        ArrayList<String> listDevices = new ArrayList<>();
                        try {
                            JSONArray jsonObjects = new JSONArray(authenticatedDevices.toString());
                            for (int i = 0; i < jsonObjects.length(); i++) {
                                JSONObject object = jsonObjects.getJSONObject(i);
                                if (!object.getString("address").equals(device.getAddress())) {
                                    listDevices.add(object.toString());
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        tinyDB.putListString(getString(R.string.key_authenticated_devices), listDevices);

                        authenticatedDevices = tinyDB.getListString(getString(R.string.key_authenticated_devices));
                        knownAdapter.clear();
                        try {
                            JSONArray jsonObjects = new JSONArray(authenticatedDevices.toString());
                            for (int i = 0; i < jsonObjects.length(); i++) {
                                JSONObject object = jsonObjects.getJSONObject(i);
                                knownAdapter.addDevice(new ListDataWrapper(object.getString("name"), object.getString("address"), true, object.getLong("last_time")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        knownAdapter.notifyDataSetChanged();

                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });

        ListView knownDevicesList = view.findViewById(R.id.list_connected_devices);
        knownDevicesList.setAdapter(knownAdapter);
        List<String> authenticatedDevices = tinyDB.getListString(getString(R.string.key_authenticated_devices));
        try {
            JSONArray jsonObjects = new JSONArray(authenticatedDevices.toString());
            for (int i = 0; i < jsonObjects.length(); i++) {
                JSONObject object = jsonObjects.getJSONObject(i);
                knownAdapter.addDevice(new ListDataWrapper(object.getString("name"), object.getString("address"), true, object.getLong("last_time")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        knownAdapter.notifyDataSetChanged();
        knownDevicesList.setOnItemClickListener((parent, _view, position, id) -> {
            bluetooth.connectToDevice(knownAdapter.getItem(position).getName(), knownAdapter.getItem(position).getAddress());
        });

        bluetooth.setOnDeviceConnected(device -> {
            knownAdapter.clear();
            List<String> knownDevices = tinyDB.getListString(getString(R.string.key_authenticated_devices));
            try {
                JSONArray jsonObjects = new JSONArray(knownDevices.toString());
                for (int i = 0; i < jsonObjects.length(); i++) {
                    JSONObject object = jsonObjects.getJSONObject(i);
                    knownAdapter.addDevice(new ListDataWrapper(object.getString("name"), object.getString("address"), true, object.getLong("last_time")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            getActivity().runOnUiThread(() -> {
                ViewPager viewPager = getActivity().findViewById(R.id.viewpager);
                viewPager.setCurrentItem(1);
                knownAdapter.notifyDataSetChanged();
            });
        });

        ListView scanDevicesList = view.findViewById(R.id.list_found_devices);
        scanDevicesList.setAdapter(adapter);

        scanDevicesList.setOnItemClickListener((parent, view1, position, id) -> {
            try {
                bluetooth.stopScanning();
                loadingProgressBar.setVisibility(View.GONE);
//                view1.findViewById(R.id.progress_discovery).setVisibility(View.INVISIBLE);
            } catch (Exception ignored) {

            }
            bluetooth.connectToDevice(adapter.getItem(position).getName(), adapter.getItem(position).getAddress());
        });

        bluetooth.setOnDiscoveryStarted(() -> {
            button.setChecked(true);
            loadingProgressBar.setVisibility(View.VISIBLE);
//            progressBar.setVisibility(View.VISIBLE);
        });

        bluetooth.setOnDiscoveryFinished(() -> {
            button.setChecked(false);
            loadingProgressBar.setVisibility(View.GONE);
//            progressBar.setVisibility(View.INVISIBLE);
        });

        if (adapter != null) {
            try {
                adapter.setData(Functions.getInstance().dewrapList(new JSONArray(tinyDB.getString("list"))));
                tinyDB.putString("list", "");
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        view.findViewById(R.id.button_discover).setOnClickListener((v) -> {
            Switch buttonView = (Switch) v;
            buttonView.setChecked(buttonView.isChecked());
            List<String> listKnownAddress = new ArrayList<>();
            List<String> listKnownDevices = tinyDB.getListString(getString(R.string.key_authenticated_devices));
            for (int i = 0; i < listKnownDevices.size(); i++) {
                try {
                    JSONObject obj = new JSONObject(listKnownDevices.get(i));
                    listKnownAddress.add(obj.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (buttonView.isChecked()) {
                adapter = new DevicesListAdapter(getContext(), null, false);
                scanDevicesList.setAdapter(adapter);
                bluetooth.setOnDeviceFound(device -> {
                    if (TextUtils.isEmpty(device.getName())) {
                        return;
                    }
                    boolean isRecognized = listKnownAddress.contains(device.getAddress());
                    adapter.addDevice(new ListDataWrapper(device.getName(), device.getAddress(), isRecognized, 0));
                    adapter.notifyDataSetChanged();
                });
                bluetooth.startScanning();
            } else {
                try {
                    bluetooth.stopScanning();
                } catch (Exception ignored) {

                }
            }
        });

        view.findViewById(R.id.button_start_qr_scan).setOnClickListener(v -> {

            Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);

        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            tinyDB.putString("list", Functions.getInstance().wrapList(adapter.getData()).toString());
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == CommonStatusCodes.SUCCESS && requestCode == RC_BARCODE_CAPTURE) {
            if (data == null) return;
            String name = data.getStringExtra(Constants.BARCODE.NAME);
            String address = data.getStringExtra(Constants.BARCODE.ADDRESS);

            Log.e("res", name);
            Log.e("res", address);

            if (bluetooth.isConnected()) {
                bluetooth.suppressDisconnectCallback();
                bluetooth.disconnect();
            }
            bluetooth.connectToDevice(name, address.toUpperCase());

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
