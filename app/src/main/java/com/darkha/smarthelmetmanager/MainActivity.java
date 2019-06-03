package com.darkha.smarthelmetmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

enum DeviceState {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED
}

public class MainActivity extends AppCompatActivity implements DevicesFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private static final int PERMISSIONS_ALL = 1;
    private static final String TAG = "BluetoothService";
    String[] PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    BottomNavigationView bottomNavigationView;
    ViewPager viewPager;
    DevicesFragment devicesFragment;
    HomeFragment homeFragment;
    SettingsFragment settingsFragment;
    MenuItem prevMenuItem;
    BluetoothHandler bluetooth;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_job);

        permissionCheck();

        if (bluetooth == null) {
            bluetooth = new BluetoothHandler(this);
        }
        Log.e(TAG, "Connected: " + bluetooth.isConnected());
        tinyDB = new TinyDB(this);

        viewPager = findViewById(R.id.viewpager);
        bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_devices:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_settings:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                Functions.getInstance().hideKeyboard(MainActivity.this);

            }

            @Override
            public void onPageSelected(int i) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(1).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(i).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        setupViewPager(viewPager);
        viewPager.setCurrentItem(1);
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
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.suppressDisconnectCallback();
        try {
            bluetooth.disconnect();
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bluetooth.onActivityResult(requestCode, resultCode);
    }

    public boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    void permissionCheck() {
        // Here, thisActivity is the current activity
        if (!hasPermissions(PERMISSIONS)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("This app need to access location service and send SMS in emergency situation. Please grant these permissions.")
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                PERMISSIONS,
                                PERMISSIONS_ALL);
                    })
                    .create()
                    .show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ALL) {
            if (!(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                permissionCheck();
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        devicesFragment = new DevicesFragment();
        devicesFragment.setServices(bluetooth, tinyDB);
        homeFragment = new HomeFragment();
        homeFragment.setServices(bluetooth, tinyDB);
        settingsFragment = new SettingsFragment();
        settingsFragment.setServices(bluetooth, tinyDB);
        adapter.addFragment(devicesFragment);
        adapter.addFragment(homeFragment);
        adapter.addFragment(settingsFragment);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 1) {
            viewPager.setCurrentItem(1);
        } else {
            MainActivity.this.moveTaskToBack(true);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}