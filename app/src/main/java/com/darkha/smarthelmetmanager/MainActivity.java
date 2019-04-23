package com.darkha.smarthelmetmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.transition.TransitionManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.DeviceCallback;
import me.aflak.bluetooth.DiscoveryCallback;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    DeviceState deviceState = DeviceState.UNAUTHENTICATED;
    String TAG = "DAMN_BL";
    String connectedDeviceMac = null;
    String connectedDeviceName = null;
    LinearLayout root;
    Bluetooth bluetooth = new Bluetooth(this);
    boolean suppressDisconnectCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        suppressDisconnectCallback = false;
        root = findViewById(R.id.layout_root);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        layoutFocus(R.id.layout_home);
        bluetoothCheck();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.btn_command).setOnClickListener(v -> {
            String input = ((EditText) findViewById(R.id.text_command)).getText().toString();
            try {
                if (((CheckBox) findViewById(R.id.checkbox_mux)).isChecked()) {
                    bluetooth.send(new String(Functions.getInstance().muxWrap(input, 0xff)));
                } else {
                    input += '\n';
                    bluetooth.send(input);
//                    bluetoothSerial.getSerialOutputStream().write(input.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(MainActivity.this, Functions.getInstance().bytesToHex(Functions.getInstance().muxWrap(input, 0xff)), Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.switch_discover).setOnClickListener((v) -> {
            Switch buttonView = (Switch) v;
            if (buttonView.isChecked()) {
                DevicesListAdapter adapter = new DevicesListAdapter(this, null);
                ((ListView) findViewById(R.id.list_devices)).setAdapter(adapter);
                DiscoveryCallback discoveryCallback = new DiscoveryCallback() {
                    @Override
                    public void onDiscoveryStarted() {
                        buttonView.setChecked(true);
                        findViewById(R.id.progress_discovery).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onDiscoveryFinished() {
                        buttonView.setChecked(false);
                        findViewById(R.id.progress_discovery).setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onDeviceFound(BluetoothDevice device) {
                        if (TextUtils.isEmpty(device.getName())) {
                            return;
                        }
                        adapter.addDevice(new ListDataWrapper(device, false));
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onDevicePaired(BluetoothDevice device) {

                    }

                    @Override
                    public void onDeviceUnpaired(BluetoothDevice device) {

                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "onError: " + message);
                    }
                };

                bluetooth.setDiscoveryCallback(discoveryCallback);
                bluetooth.startScanning();
                ListView devicesList = findViewById(R.id.list_devices);
                devicesList.setAdapter(adapter);
                devicesList.setOnItemClickListener((parent, view, position, id) -> {
                    try {
                        bluetooth.stopScanning();
                        findViewById(R.id.progress_discovery).setVisibility(View.INVISIBLE);
                        buttonView.setChecked(false);
                    } catch (Exception ignored) {

                    }
                    connectToDevice(adapter.getItem(position).getDevice());
                });
            } else {
                try {
                    bluetooth.stopScanning();
                } catch (Exception ignored) {

                }
                buttonView.setChecked(false);
                findViewById(R.id.progress_discovery).setVisibility(View.INVISIBLE);
            }
        });
    }

    void connectToDevice(BluetoothDevice device) {
        if (TextUtils.isEmpty(device.getName())) {
            return;
        }
        suppressDisconnectCallback = true;
        deviceState = DeviceState.UNAUTHENTICATED;

        AlertDialog bondDialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(getString(R.string.pairing_with) + ' ' + device.getName() + "...")
                .setPositiveButton(R.string.close, (_dialog, which) -> {
                    _dialog.dismiss();
                })
                .setView(R.layout.progressbar_layout)
                .setCancelable(false)
                .create();

        AlertDialog loadingDialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(getString(R.string.connecting_to) + ' ' + device.getName() + "...")
                .setView(R.layout.progressbar_layout)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    try {
                        bluetooth.disconnect();
                    } catch (Exception ignored) {

                    }
                    Toast.makeText(MainActivity.this, getString(R.string.connection_canceled), Toast.LENGTH_LONG).show();
                })
                .setCancelable(false).create();
        loadingDialog.show();

        AlertDialog authDialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(R.string.authenticating)
                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    deviceState = DeviceState.UNAUTHENTICATED;
                    bluetooth.disconnect();
                })
                .setView(R.layout.progressbar_layout)
                .setCancelable(false)
                .create();

        bluetooth.setDeviceCallback(new DeviceCallback() {
            @Override
            public void onDeviceConnected(BluetoothDevice device) {
                connectedDeviceMac = device.getAddress();
                connectedDeviceName = device.getName();
                bluetooth.send("AUT\n");
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    authDialog.show();
                    new Handler().postDelayed(() -> {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
                            bluetooth.disconnect();
                            suppressDisconnectCallback = true;
                            authDialog.dismiss();
                            deviceState = DeviceState.UNAUTHENTICATED;
                        });
                    }, 5000);
                    Toast.makeText(MainActivity.this, getString(R.string.connection_sucess), Toast.LENGTH_LONG).show();
//                    layoutFocus(R.id.layout_home);
                });
            }

            @Override
            public void onDeviceDisconnected(BluetoothDevice device, String message) {
                runOnUiThread(() -> {
                    if (!suppressDisconnectCallback) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(R.string.lost_connection_prompt)
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    dialog.dismiss();
                                    bluetooth.connectToDevice(device);
                                    loadingDialog.show();
                                })
                                .setNegativeButton(R.string.no, (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .create()
                                .show();
                        suppressDisconnectCallback = false;
                    }
                });
            }

            @Override
            public void onMessage(String message) {
                if (deviceState == DeviceState.UNAUTHENTICATED) {
                    // TODO: Load shared preference to find if device was authenticated before
                    if (message.startsWith("AUTH OK")) {
                        deviceState = DeviceState.AUTHENTICATING;
                        bluetooth.send("AOK\n");
                    }
                } else if (deviceState == DeviceState.AUTHENTICATING) {
                    if (message.startsWith("AOK")) {
                        deviceState = DeviceState.AUTHENTICATED;
                        // TODO: Save device address to shared preference for later use
                        runOnUiThread(() -> {
                            authDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Authenticated", Toast.LENGTH_LONG).show();
                            layoutFocus(R.id.layout_home);
                        });
                    }
                } else if (deviceState == DeviceState.AUTHENTICATED) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, Functions.getInstance().bytesToHex(message.getBytes()), Toast.LENGTH_LONG).show();
                    });
                    // TODO: Handle message
                }
                Log.e(TAG, "message " + message);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "err " + message);
            }

            @Override
            public void onConnectError(BluetoothDevice device, String message) {
                connectedDeviceMac = device.getAddress();
                connectedDeviceName = device.getName();
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    bondDialog.dismiss();
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.connection_failed_try_again)
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                });
            }
        });

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            bluetooth.pair(device);
            bondDialog.show();

            IntentFilter filterBond = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            BroadcastReceiver bondReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            bondDialog.dismiss();
                            bluetooth.connectToDevice(device);
                            loadingDialog.show();
                            context.unregisterReceiver(this);
                        }
                    }
                }
            };
            registerReceiver(bondReceiver, filterBond);
        } else {
            bluetooth.connectToDevice(device);
            loadingDialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        bluetooth.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private void bluetoothCheck() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.bluetooth_not_supported_error)
                    .setCancelable(false)
                    .setPositiveButton(R.string.exit, (dialog, which) -> finish()).show();
            // Device does not support Bluetooth
        } else {
            if ((!bluetoothAdapter.isEnabled()) || bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.bluetooth_not_enabled_error)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBT, 1);
                            BroadcastReceiver receiver = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    final String action = intent.getAction();
                                    if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                                        final int state = bluetoothAdapter.getState();
                                        if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_ON) {
                                            dialog.dismiss();
                                            unregisterReceiver(this);
                                        }
                                    }
                                }
                            };
                            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                        })
                        .setNegativeButton(R.string.exit, (dialog, which) -> finish())
                        .show();
            }
        }
    }

    private void layoutFocus(@IdRes int id) {
        TransitionManager.beginDelayedTransition(root);
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = root.getChildAt(i);
            if (v.getId() == id) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        ActionBar actionBar = getSupportActionBar();
        if (id == R.id.nav_home) {
            layoutFocus(R.id.layout_home);
            if (actionBar != null) {
                actionBar.setTitle(R.string.app_name);
            }
        } else if (id == R.id.nav_devices) {
            layoutFocus(R.id.layout_devices);
            if (actionBar != null) {
                actionBar.setTitle(R.string.nav_devices);
            }
        } else if (id == R.id.nav_settings) {
            layoutFocus(R.id.layout_settings);
            if (actionBar != null) {
                actionBar.setTitle(R.string.nav_settings);
            }
        } else if (id == R.id.nav_about) {
            layoutFocus(R.id.layout_about);
            if (actionBar != null) {
                actionBar.setTitle(R.string.nav_about);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    enum DeviceState {
        UNAUTHENTICATED,
        AUTHENTICATING,
        AUTHENTICATED
    }
}
