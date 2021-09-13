package com.bluetoothdemo.bledemo.adapter;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluetoothdemo.bledemo.R;

import java.util.List;

import cn.com.heaton.blelibrary.ble.model.BleDevice;

public class ConnectInfoAdapter extends RecyclerView.Adapter<ConnectInfoAdapter.ViewHolder>{
    private Context context;
    private List<BluetoothGattService> serviceList;
    private int opened = -1;

    private ItemClickListener mItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvServiceUuid;
        RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceUuid = itemView.findViewById(R.id.tv_service_uuid);
            recyclerView = itemView.findViewById(R.id.recyclerView_characteristc);
        }
    }

    public ConnectInfoAdapter(Context context, List<BluetoothGattService> serviceList){
        this.context = context;
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothGattService service = serviceList.get(position);
        holder.tvServiceUuid.setText(service.getUuid().toString());

        if (position == opened){
            holder.recyclerView.setVisibility(View.VISIBLE);
        }else {
            holder.recyclerView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先判断点击的是否是已经展开的列表
                if (opened == holder.getAdapterPosition()){
                    opened = -1;
                    notifyItemChanged(holder.getAdapterPosition());
                }else {
                    int oldOpened = opened;
                    opened = holder.getAdapterPosition();
                    notifyItemChanged(oldOpened);
                    notifyItemChanged(opened);
                }
            }
        });

        CharacteristcAdapter characteristcAdapter = new CharacteristcAdapter(context, service.getCharacteristics());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerView.setAdapter(characteristcAdapter);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void setItemClickListener(ItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface ItemClickListener{
        void onItemClickListener();
    }
}
