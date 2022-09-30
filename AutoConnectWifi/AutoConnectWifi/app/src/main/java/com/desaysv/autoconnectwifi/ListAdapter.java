package com.desaysv.autoconnectwifi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.List;

public class ListAdapter extends ArrayAdapter<WifiData> {
    private int resource;
    public ListAdapter(Context context, int resource,  List<WifiData> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }
    //每个子项被滚动到屏幕内的时候会被调用

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       WifiData wifiData = getItem(position) ;//得到当前项的 Fruit 实例
        //为每一个子项加载设定的布局
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cell,parent,false);
        //分别获取 image view 和 textview 的实例
        TextView wiFiName = view.findViewById(R.id.wifi_cell_name);
        TextView wiFiStatus = view.findViewById(R.id.wifi_cell_status);
        // 设置要显示的图片和文字
        wiFiName.setText(wifiData.getName());
        wiFiStatus.setText(wifiData.getStatus());
        return view;
    }

}
