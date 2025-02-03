package com.example.xiaoshoufuzhu.Reports.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;

import java.util.List;

public class CustomerReportAdapter extends ArrayAdapter<CustomerRecord> {

    public CustomerReportAdapter(Context context, List<CustomerRecord> customerRecords) {
        super(context, 0, customerRecords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomerRecord customerRecord = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_customer_record, parent, false);
        }

        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvTotalPrice = convertView.findViewById(R.id.tvTotalPrice);

        if (customerRecord != null) {
            tvProductName.setText(customerRecord.getProductName());
            tvQuantity.setText(String.valueOf(customerRecord.getQuantity()));
            tvTotalPrice.setText(String.valueOf(customerRecord.getTotalPrice()));
        }

        return convertView;
    }
}