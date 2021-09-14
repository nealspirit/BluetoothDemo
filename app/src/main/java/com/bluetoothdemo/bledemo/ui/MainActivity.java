package com.bluetoothdemo.bledemo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.bluetoothdemo.bledemo.R;
import com.bluetoothdemo.bledemo.adapter.ScanAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleStatusCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.model.ScanRecord;
import cn.com.heaton.blelibrary.ble.utils.Utils;

import static cn.com.heaton.blelibrary.ble.Ble.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_LOCATION = 2;
    public static final int REQUEST_PERMISSION_WRITE = 3;
    public static final int REQUEST_GPS = 4;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Snackbar snackbar;
    private FloatingActionButton floatingActionButton;
    private ObjectAnimator animator;

    private List<BleDevice> bleDeviceList;
    private ScanAdapter scanAdapter;
    private Ble<BleDevice> ble = Ble.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAdapter();
        initListener();
        initBleStatus();
        requestPermission();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        recyclerView = findViewById(R.id.recyclerView);
        floatingActionButton = findViewById(R.id.fab);

        snackbar = Snackbar.make(recyclerView,"蓝牙未开启",Snackbar.LENGTH_LONG)
                .setAction("开启", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });
    }

    private void initAdapter() {
        bleDeviceList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        scanAdapter = new ScanAdapter(bleDeviceList);
        recyclerView.setAdapter(scanAdapter);
    }

    private void initListener(){
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });

        scanAdapter.setItemClickListener(new ScanAdapter.ItemClickListener() {
            @Override
            public void onItemClickListener(BleDevice device) {
                ble.stopScan();
                startActivity(new Intent(MainActivity.this,DeviceConnectActivity.class)
                        .putExtra("BluetoothDevice", device));
            }
        });
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

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }else {
            checkBlueStatus();
        }
    }

    private void checkBlueStatus() {
        if (!ble.isSupportBle(this)) {
            Toast.makeText(this,R.string.ble_not_supported,Toast.LENGTH_LONG).show();
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

    private void rescan(){
        if (ble != null){
            if (ble.isScanning()){
                ble.stopScan();
            }else {
                bleDeviceList.clear();
                scanAdapter.notifyDataSetChanged();
                checkBlueStatus();
            }
        }
    }

    private BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onLeScan: " + "name: " + device.getBleName() + "; address: " + device.getBleAddress());
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
        floatingActionButton.setImageResource(R.drawable.ic_loading);
        animator = ObjectAnimator.ofFloat(floatingActionButton, "rotation", 0, 360);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(800);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void stopBannerLoadingAnim() {
        floatingActionButton.setImageResource(R.drawable.ic_bluetooth_audio_black_24dp);
        animator.cancel();
        floatingActionButton.setRotation(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "蓝牙未启动", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else {
                Toast.makeText(this,"定位权限被禁止",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}