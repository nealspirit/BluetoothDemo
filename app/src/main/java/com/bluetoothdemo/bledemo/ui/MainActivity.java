package com.bluetoothdemo.bledemo.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bluetoothdemo.bledemo.R;
import com.bluetoothdemo.bledemo.adapter.ScanAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.utils.UuidUtils;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private List<BleDevice> bleDeviceList;
    private ScanAdapter scanAdapter;

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
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void initAdapter() {
        bleDeviceList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        scanAdapter = new ScanAdapter(bleDeviceList);

    }

    private void initLinsenter(){

    }

    private void initBleStatus(){

    }

    private void requestPermission(){

    }

}