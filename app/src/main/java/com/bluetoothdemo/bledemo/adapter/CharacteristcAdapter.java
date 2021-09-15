package com.bluetoothdemo.bledemo.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bluetoothdemo.bledemo.R;

import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleReadCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.utils.ByteUtils;
import cn.com.heaton.blelibrary.ble.utils.ThreadUtils;

public class CharacteristcAdapter extends RecyclerView.Adapter<CharacteristcAdapter.ViewHolder>{
    private Context context;
    private List<BluetoothGattCharacteristic> gattCharacteristicList;

    public CharacteristcAdapter(Context context, List<BluetoothGattCharacteristic> gattCharacteristicList){
        this.context = context;
        this.gattCharacteristicList = gattCharacteristicList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvCharacteristicUuid;
        TextView tvProperties;
        ImageView btnWrite;
        ImageView btnRead;
        TextView tvValue;
        EditText etWriteValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCharacteristicUuid = itemView.findViewById(R.id.tv_characteristic_uuid);
            tvProperties = itemView.findViewById(R.id.tv_properties);
            btnWrite = itemView.findViewById(R.id.iv_write_value);
            btnRead = itemView.findViewById(R.id.iv_read_value);
            tvValue = itemView.findViewById(R.id.tv_read_value);
            etWriteValue = itemView.findViewById(R.id.et_write_value);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_characteristic_info, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothGattCharacteristic characteristic = gattCharacteristicList.get(position);
        holder.tvCharacteristicUuid.setText(characteristic.getUuid().toString());

        int charaProp = characteristic.getProperties();
        StringBuilder builder = new StringBuilder();
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            builder.append("READ,");
            holder.btnRead.setVisibility(View.VISIBLE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            builder.append("WRITE,");
            holder.btnWrite.setVisibility(View.VISIBLE);
            holder.etWriteValue.setVisibility(View.VISIBLE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            builder.append("WRITE_NO_RESPONSE,");
            holder.btnWrite.setVisibility(View.VISIBLE);
            holder.etWriteValue.setVisibility(View.VISIBLE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            builder.append("NOTIFY,");
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            builder.append("INDICATE,");
        }
        if (builder.length() > 0){
            builder.deleteCharAt(builder.length()-1);
            holder.tvProperties.setText(String.format("Properties: %s", builder.toString()));
        }

        holder.btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BleDevice> deviceList = Ble.getInstance().getConnectedDevices();
                UUID serviceUuid = characteristic.getService().getUuid();
                UUID characteristicUuid = characteristic.getUuid();
                try {
                    byte[] bytes = ByteUtils.hexStr2Bytes(holder.etWriteValue.getText().toString());
                    if (!deviceList.isEmpty()) {
                        BleDevice device = deviceList.get(0);
                        Ble.getInstance().writeByUuid(device, bytes, serviceUuid, characteristicUuid,
                                new BleWriteCallback<BleDevice>() {
                                    @Override
                                    public void onWriteSuccess(BleDevice device, BluetoothGattCharacteristic characteristic) {
                                        ThreadUtils.ui(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "写入成功", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onWriteFailed(BleDevice device, int failedCode) {
                                        super.onWriteFailed(device, failedCode);
                                        ThreadUtils.ui(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "写入失败", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    holder.etWriteValue.requestFocus();
                    holder.etWriteValue.setError("请输入十六进制字符");
                }
            }
        });

        holder.btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BleDevice> deviceList = Ble.getInstance().getConnectedDevices();
                UUID serviceUuid = characteristic.getService().getUuid();
                UUID characteristicUuid = characteristic.getUuid();
                if (!deviceList.isEmpty()){
                    BleDevice device = deviceList.get(0);
                    Ble.getInstance().readByUuid(device, serviceUuid, characteristicUuid,
                            new BleReadCallback<BleDevice>() {
                                @Override
                                public void onReadSuccess(BleDevice dedvice, BluetoothGattCharacteristic characteristic) {
                                    super.onReadSuccess(dedvice, characteristic);
                                    ThreadUtils.ui(new Runnable() {
                                        @Override
                                        public void run() {
                                            holder.tvValue.setVisibility(View.VISIBLE);
                                            holder.tvValue.setText("value:0x" + ByteUtils.bytes2HexStr(characteristic.getValue()));
                                        }
                                    });
                                }

                                @Override
                                public void onReadFailed(BleDevice device, int failedCode) {
                                    super.onReadFailed(device, failedCode);
                                    ThreadUtils.ui(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, "读取失败", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return gattCharacteristicList.size();
    }
}
