package com.bluetoothdemo.bledemo.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bluetoothdemo.bledemo.R;
import com.bluetoothdemo.bledemo.adapter.ScanAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleStatusCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.model.ScanRecord;
import cn.com.heaton.blelibrary.ble.utils.Utils;
import cn.com.heaton.blelibrary.ble.utils.UuidUtils;
import cn.com.superLei.aoparms.annotation.Permission;
import cn.com.superLei.aoparms.annotation.PermissionDenied;
import cn.com.superLei.aoparms.annotation.PermissionNoAskDenied;
import cn.com.superLei.aoparms.common.permission.AopPermissionUtils;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_LOCATION = 2;
    public static final int REQUEST_PERMISSION_WRITE = 3;
    public static final int REQUEST_GPS = 4;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Snackbar snackbar;

    private List<BleDevice> bleDeviceList;
    private ScanAdapter scanAdapter;
    private Ble<BleDevice> ble = Ble.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAdapter();
        initLinsenter();
        initBleStatus();
        requestPermission();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);

        snackbar = Snackbar.make(recyclerView,"蓝牙未开启",Snackbar.LENGTH_LONG)
                .setAction("开启", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
    }

    private void initAdapter() {
        bleDeviceList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        scanAdapter = new ScanAdapter(bleDeviceList);
        recyclerView.setAdapter(scanAdapter);
    }

    private void initLinsenter(){

    }

    private void initBleStatus(){
        ble.setBleStatusCallback(new BleStatusCallback() {
            @Override
            public void onBluetoothStatusChanged(boolean isOn) {
                BleLog.i(TAG, "onBluetoothStatusOn: 蓝牙是否打开>>>>:" + isOn);
                snackbar.show();
                if (isOn){
                    checkGpsStatus();
                }else {
                    if (ble.isScanning()) {
                        ble.stopScan();
                    }
                }
            }
        });
    }

    //请求权限
    @Permission(value = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
            requestCode = REQUEST_PERMISSION_LOCATION,
            rationale = "需要蓝牙相关权限")
    public void requestPermission() {
        checkBlueStatus();
    }

    @PermissionDenied
    public void permissionDenied(int requestCode, List<String> denyList) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            Log.e(TAG, "permissionDenied>>>:定位权限被拒 " + denyList.toString());
        } else if (requestCode == REQUEST_PERMISSION_WRITE) {
            Log.e(TAG, "permissionDenied>>>:读写权限被拒 " + denyList.toString());
        }
    }

    @PermissionNoAskDenied
    public void permissionNoAskDenied(int requestCode, List<String> denyNoAskList) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            Log.e(TAG, "permissionNoAskDenied 定位权限被拒>>>: " + denyNoAskList.toString());
        } else if (requestCode == REQUEST_PERMISSION_WRITE) {
            Log.e(TAG, "permissionDenied>>>:读写权限被拒>>> " + denyNoAskList.toString());
        }
        AopPermissionUtils.showGoSetting(this, "为了更好的体验，建议前往设置页面打开权限");
    }

    private void checkBlueStatus() {
        if (!ble.isSupportBle(this)) {
            com.bluetoothdemo.bledemo.Utils.showToast(R.string.ble_not_supported);
            finish();
        }
        if (!ble.isBleEnable()) {
            snackbar.show();
        }else {
            checkGpsStatus();
        }
    }

    private void checkGpsStatus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Utils.isGpsOpen(MainActivity.this)){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("为了更精确的扫描到Bluetooth LE设备,请打开GPS定位")
                    .setPositiveButton("确定", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent,REQUEST_GPS);
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        }else {
            ble.startScan(scanCallback);
        }
    }

    private BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            synchronized (ble.getLocker()) {
                for (int i = 0; i < bleDeviceList.size(); i++) {
                    BleDevice bleDevice = bleDeviceList.get(i);
                    if (TextUtils.equals(bleDevice.getBleAddress(), device.getBleAddress())){
                        return;
                    }
                }
                device.setScanRecord(ScanRecord.parseFromBytes(scanRecord));
                bleDeviceList.add(device);
                scanAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            startBannerLoadingAnim();
        }

        @Override
        public void onStop() {
            super.onStop();
            stopBannerLoadingAnim();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: "+errorCode);
        }
    };

    private void startBannerLoadingAnim() {
    }

    private void stopBannerLoadingAnim() {
    }

}