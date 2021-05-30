package com.gtpautomation.securityguard.pages.add_truck.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gtpautomation.securityguard.R;
import com.gtpautomation.securityguard.pojos.truck.TruckField;

import java.util.List;

/**
 * Created by Sunil Kumar on 09-04-2021 05:46 PM.
 */
public class TruckSpinnerAdapter extends BaseAdapter {
    private List<TruckField.SelectValue> list;
    private Context context;

    public TruckSpinnerAdapter(List<TruckField.SelectValue> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.spinner_layout,null);
        TextView tv = view.findViewById(R.id.spinner_text);
        tv.setText(list.get(position).getValue().toString());
        return view;
    }
}
