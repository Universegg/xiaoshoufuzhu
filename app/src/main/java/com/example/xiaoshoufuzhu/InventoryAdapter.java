package com.example.xiaoshoufuzhu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class InventoryAdapter extends ArrayAdapter<InventoryItem> {

    public InventoryAdapter(Context context, List<InventoryItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_item_inventory);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_item_inventory_dropdown);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        final InventoryItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        }

        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvBatchNo = convertView.findViewById(R.id.tvBatchNo);

        if (item != null) {
            tvProductName.setText(item.getName());
            tvBatchNo.setText(item.getBatchNo());
        }

        return convertView;
    }
}