package com.example.xiaoshoufuzhu.Reports.customer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerReportAdapter extends ArrayAdapter<CustomerRecord> {
    public CustomerReportAdapter(Context context, List<CustomerRecord> customerRecords) {
        super(context, 0, customerRecords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomerRecord record = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_customer_record, parent, false);
        }

        // 初始化所有视图组件
        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvBatchNum = convertView.findViewById(R.id.tvBatchNum);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvTotalPrice = convertView.findViewById(R.id.tvTotalPrice); // 新增
        TextView tvSaleDate = convertView.findViewById(R.id.tvSaleDate);
        TextView tvState = convertView.findViewById(R.id.tvState);

        if (record != null) {
            // 设置文本内容
            tvProductName.setText(record.getProductName());
            tvBatchNum.setText(record.getBatchNum());
            tvPrice.setText(String.format("¥%.2f", record.getPrice()));
            tvQuantity.setText(String.format("%.2f", record.getQuantity()));
            tvTotalPrice.setText(String.format("¥%.2f", record.getTotalPrice())); // 设置总价
            tvSaleDate.setText(record.getSaleDate());

            // 设置状态颜色
            if ("赊账".equals(record.getState())) {
                tvState.setTextColor(Color.parseColor("#E6755F"));
            } else {
                tvState.setTextColor(Color.parseColor("#8DB799"));
            }
            tvState.setText(record.getState());
        }

        return convertView;
    }
}