package com.bluetoothdemo.bledemo.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bluetoothdemo.bledemo.R;

import java.util.List;

import cn.com.heaton.blelibrary.ble.model.BleDevice;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {
    private List<BleDevice> bleDeviceList;

    private ItemClickListener mItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView deviceNameTv;
        TextView deviceAddressTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTv = itemView.findViewById(R.id.tv_name);
            deviceAddressTv = itemView.findViewById(R.id.tv_address);
        }

    }
    public ScanAdapter(List<BleDevice> deviceList){
        bleDeviceList = deviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BleDevice device = bleDeviceList.get(position);
        holder.deviceNameTv.setText(device.getBleName());
        holder.deviceAddressTv.setText(device.getBleAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null){
                    mItemClickListener.onItemClickListener(device);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bleDeviceList.size();
    }

    public void setItemClickListener(ItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface ItemClickListener{
        void onItemClickListener(BleDevice device);
    }

}
