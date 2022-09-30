package com.desaysv.autoconnectwifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.internal.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<WifiData> mWifiDataList = new ArrayList<>();

    public MyAdapter( List<WifiData> wifiDataList){
        mWifiDataList = wifiDataList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tName;
        private TextView tStatus;
        private View view;
        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tName = view.findViewById(R.id.wifi_cell_name);
            tStatus = view.findViewById(R.id.wifi_cell_status);
        }
    }
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        WifiData wifiData = mWifiDataList.get(i);
        viewHolder.tName.setText(wifiData.getName());
        viewHolder.tStatus.setText(wifiData.getStatus());
    }




    @Override
    public int getItemCount() {
        return mWifiDataList.size();
    }
}
