package com.bluetoothdemo.bledemo;

import android.app.Application;

import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.model.BleFactory;
import cn.com.heaton.blelibrary.ble.utils.UuidUtils;

public class MyApplication extends Application {
    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        initBle();
    }

    //初始化蓝牙
    private void initBle() {
        Ble.options()
                .setLogBleEnable(true)//设置是否输出打印蓝牙日志（非正式打包请设置为true，以便于调试）
                .setThrowBleException(true)//设置是否抛出蓝牙异常（默认true）
                .setLogTAG("AndroidBLE")//设置全局蓝牙操作日志TAG
                .setAutoConnect(false)//设置是否自动连接（默认false）
                .setIgnoreRepeat(false)//设置是否过滤扫描到的设备(已扫描到的不会再次扫描)
                .setConnectFailedRetryCount(3)//连接异常时（如蓝牙协议栈错误）,重新连接次数
                .setConnectTimeout(10 * 1000)//设置连接超时时长（默认10*1000 ms）
                .setScanPeriod(12 * 1000)//设置扫描时长
                .setMaxConnectNum(7)//最大连接数量
                .setUuidService(UUID.fromString(UuidUtils.uuid16To128("fd00")))//设置主服务的uuid
                .setUuidWriteCha(UUID.fromString(UuidUtils.uuid16To128("fd01")))//设置可写特征的uuid
//                .setUuidReadCha(UUID.fromString(UuidUtils.uuid16To128("fd02")))//设置可读特征的uuid （选填）
//                .setUuidNotifyCha(UUID.fromString(UuidUtils.uuid16To128("fd03")))//设置可通知特征的uuid （选填，库中默认已匹配可通知特征的uuid）
//                .setFactory(new BleFactory<BleRssiDevice>() {//实现自定义BleDevice时必须设置
//                    @Override
//                    public BleRssiDevice create(String address, String name) {
//                        return new BleRssiDevice(address, name);//自定义BleDevice的子类
//                    }
//                })
                .create(myApplication, new Ble.InitCallback() {
                    @Override
                    public void success() {
                        BleLog.e("MainApplication", "初始化成功");
                    }

                    @Override
                    public void failed(int failedCode) {
                        BleLog.e("MainApplication", "初始化失败：" + failedCode);
                    }
                });
    }

    public static MyApplication getInstance() {
        return myApplication;
    }
}
