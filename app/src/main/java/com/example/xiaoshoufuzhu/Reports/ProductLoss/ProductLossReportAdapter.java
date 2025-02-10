package com.example.xiaoshoufuzhu.Reports.ProductLoss;

import static com.example.xiaoshoufuzhu.R.*;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.ProductDetail.ProductDetailActivity;
import com.example.xiaoshoufuzhu.R;

import java.util.List;

public class ProductLossReportAdapter extends ArrayAdapter<ProductLossRecord> {
    private static class ViewHolder {
        TextView tvProductName;
        TextView tvBatchNumber;
        TextView tvLossQuantity;
        TextView tvLossAmount;
        ImageView ivDetail;
    }

    public ProductLossReportAdapter(Context context, List<ProductLossRecord> records) {
        super(context, 0, records);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProductLossRecord record = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_product_loss_report, parent, false);

            holder = new ViewHolder();
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvBatchNumber = convertView.findViewById(R.id.tvBatchNumber);
            holder.tvLossQuantity = convertView.findViewById(R.id.tvLossQuantity);
            holder.tvLossAmount = convertView.findViewById(R.id.tvLossAmount);
            holder.ivDetail = convertView.findViewById(R.id.ivDetail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (record != null) {
            holder.tvProductName.setText(record.getProductName());
            holder.tvBatchNumber.setText(record.getBatchNumber());
            holder.tvLossQuantity.setText(String.valueOf(record.getLossQuantity()));
            holder.tvLossAmount.setText(String.format("¥%.2f", record.getLossAmount()));

            holder.ivDetail.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("productName", record.getProductName());
                intent.putExtra("productNum", record.getBatchNumber());
                getContext().startActivity(intent);
            });
        }

        return convertView;
    }
}