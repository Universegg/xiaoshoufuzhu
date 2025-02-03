package com.example.xiaoshoufuzhu.SupplierSalesManagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.model.Supplier;

import java.util.List;

public class SupplierAdapter extends ArrayAdapter<Supplier> {

    public SupplierAdapter(Context context, List<Supplier> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_item, parent, false);
        }
        Supplier supplier = getItem(position);
        if (supplier != null) {
            TextView textView = convertView.findViewById(R.id.textView);
            textView.setText(supplier.getName());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_item, parent, false);
        }
        Supplier supplier = getItem(position);
        if (supplier != null) {
            TextView textView = convertView.findViewById(R.id.textView);
            textView.setText(supplier.getName());
        }
        return convertView;
    }
}