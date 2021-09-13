package com.bluetoothdemo.bledemo.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.widget.Toast;

import com.bluetoothdemo.bledemo.R;
import com.bluetoothdemo.bledemo.adapter.ConnectInfoAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotifyCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.utils.ByteUtils;

public class DeviceConnectActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private Ble<BleDevice> ble;
    private BleDevice device;
    private Toolbar toolbar;
    private RecyclerView rvServiceList;
    private ConnectInfoAdapter infoAdapter;
    private List<BluetoothGattService> gattServiceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_connect);
        initView();
        initData();
        initAdapter();
        initListener();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        rvServiceList = findViewById(R.id.recyclerView_service);
    }

    private void initData() {
        ble = Ble.getInstance();
        device = getIntent().getParcelableExtra("BluetoothDevice");
        if (device == null) return;
        if (device.getBleName() != null) {
            toolbar.setTitle(device.getBleName());
        }else {
            toolbar.setTitle(device.getBleAddress());
        }
        ble.connect(device,connectCallback);
    }

    private void initAdapter(){
        gattServiceList = new ArrayList<>();
        infoAdapter = new ConnectInfoAdapter(this, gattServiceList);
        rvServiceList.setLayoutManager(new LinearLayoutManager(this));
        rvServiceList.getItemAnimator().setChangeDuration(300);
        rvServiceList.getItemAnimator().setMoveDuration(300);
        rvServiceList.setAdapter(infoAdapter);
    }

    private void initListener(){

    }

    private BleConnectCallback<BleDevice> connectCallback = new BleConnectCallback<BleDevice>() {
        @Override
        public void onConnectionChanged(BleDevice device) {
            if (device.isConnected()){
                toolbar.setSubtitle(R.string.line_success);
            }else if (device.isConnecting()){
                toolbar.setSubtitle(R.string.line_connecting);
            }else if (device.isDisconnected()){
                toolbar.setSubtitle(R.string.line_disconnect);
            }
        }

        @Override
        public void onConnectFailed(BleDevice device, int errorCode) {
            super.onConnectFailed(device, errorCode);
            Toast.makeText(DeviceConnectActivity.this,"连接异常，异常码：" + errorCode, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServicesDiscovered(BleDevice device, BluetoothGatt gatt) {
            super.onServicesDiscovered(device, gatt);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gattServiceList.addAll(gatt.getServices());
                    infoAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onReady(BleDevice device) {
            super.onReady(device);
            ble.enableNotify(device, true, new BleNotifyCallback<BleDevice>() {
                @Override
                public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
                    UUID uuid = characteristic.getUuid();
                    BleLog.e(TAG, "onChanged==uuid:" + uuid.toString());
                    BleLog.e(TAG, "onChanged==data:" + ByteUtils.toHexString(characteristic.getValue()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DeviceConnectActivity.this,
                                    String.format("收到设备通知数据: %s", ByteUtils.toHexString(characteristic.getValue())),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }

                @Override
                public void onNotifySuccess(BleDevice device) {
                    super.onNotifySuccess(device);
                    BleLog.e(TAG, "onNotifySuccess: "+device.getBleName());
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (device != null){
            if (device.isConnecting()){
                ble.cancelConnecting(device);
            }else if (device.isConnected()){
                ble.disconnect(device);
            }
        }
        ble.cancelCallback(connectCallback);
    }
}