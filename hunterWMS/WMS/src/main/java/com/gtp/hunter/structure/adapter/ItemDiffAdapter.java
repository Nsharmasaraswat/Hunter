package com.gtp.hunter.structure.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.gtp.hunter.R;

import java.util.ArrayList;
import java.util.Objects;

public class ItemDiffAdapter extends BaseAdapter {

    private final ArrayList<String> itemList;

    public Context context;
    public LayoutInflater inflater;

    public ItemDiffAdapter(Context context, ArrayList<String> itemList) {
        super();

        this.context = context;
        this.itemList = itemList;

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return itemList.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return itemList.get(position);
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static class ViewHolder {
        TextView text;
        TextView diff;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String[] txt = itemList.get(position).split(": ");
        int diff = Integer.parseInt(txt[1]);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_diff, null);
            holder.text = convertView.findViewById(R.id.txtItemName);
            holder.diff = convertView.findViewById(R.id.txtItemDiff);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.text.setText(txt[0]);
        holder.diff.setText(String.valueOf(diff));
        convertView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(context), diff < 0 ? R.color.light_error : R.color.light_info));
        return convertView;
    }

}
