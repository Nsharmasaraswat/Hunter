package com.gtp.hunter.structure.spinner;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SearchableAdapter extends ArrayAdapter<String> implements Filterable {

    private final ArrayList<String> items;
    private ArrayList<String> filteredItems;

    SearchableAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        this.items = objects;
        this.filteredItems = objects;
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public String getItem(int position) {
        return filteredItems.get(position);
    }

    private List<String> getAllItems() {
        return items;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<String> filteredItems1 = new ArrayList<>();
                List<String> items1 = getAllItems();

                if (constraint == null || constraint.length() == 0) {
                    results.values = items1;
                    results.count = items1.size();
                } else {
                    for (String item : items1) {
                        if (item.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredItems1.add(item);
                        }
                    }
                    results.values = filteredItems1;
                    results.count = filteredItems1.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredItems = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}