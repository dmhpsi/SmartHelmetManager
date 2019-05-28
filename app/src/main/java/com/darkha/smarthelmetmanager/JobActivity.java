package com.darkha.smarthelmetmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

enum DeviceState {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED
}

public class JobActivity extends AppCompatActivity implements DevicesFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private static final int PERMISSIONS_REQUEST_COARSE_LOCATION = 99;
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 98;
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
        permissionCheck();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        bluetooth = new BluetoothHandler(this);
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
                Functions.getInstance().hideKeyboard(JobActivity.this);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bluetooth.onActivityResult(requestCode, resultCode);
    }

    void permissionCheck() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(JobActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(JobActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(JobActivity.this)
                        .setMessage("This app need to access location service in order to work. Please grant the permission.")
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(JobActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    PERMISSIONS_REQUEST_COARSE_LOCATION);
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(JobActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_COARSE_LOCATION);

            }
        }

        if (ContextCompat.checkSelfPermission(JobActivity.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(JobActivity.this,
                    Manifest.permission.SEND_SMS)) {
                new AlertDialog.Builder(JobActivity.this)
                        .setMessage("This app need to send SMS in order to work. Please grant the permission.")
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(JobActivity.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    PERMISSIONS_REQUEST_SEND_SMS);
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(JobActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_COARSE_LOCATION: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    permissionCheck();
                }
                return;
            }
            case PERMISSIONS_REQUEST_SEND_SMS: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    permissionCheck();
                }
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
    public void onFragmentInteraction(Uri uri) {

    }
}