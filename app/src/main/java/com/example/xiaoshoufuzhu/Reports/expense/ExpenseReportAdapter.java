package com.example.xiaoshoufuzhu.Reports.expense;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.Reports.ProductDetailActivity;

import java.util.List;

public class ExpenseReportAdapter extends ArrayAdapter<ExpenseRecord> {
    private static class ViewHolder {
        TextView tvProductName;
        TextView tvBatchNumber;
        TextView tvTotalExpense;
        TextView tvFreight;
        ImageView ivInfo; // 新增
    }

    public ExpenseReportAdapter(Context context, List<ExpenseRecord> expenseRecords) {
        super(context, 0, expenseRecords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExpenseRecord record = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_expense_record, parent, false);

            holder = new ViewHolder();
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvBatchNumber = convertView.findViewById(R.id.tvProductNum);
            holder.tvTotalExpense = convertView.findViewById(R.id.tvTotalExpense);
            holder.tvFreight = convertView.findViewById(R.id.tvFreight);
            holder.ivInfo = convertView.findViewById(R.id.ivInfo); // 绑定新视图

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (record != null) {
            holder.tvProductName.setText(record.getProductName());
            holder.tvBatchNumber.setText(record.getBatchNumber());
            holder.tvTotalExpense.setText(String.format("¥%.2f", record.getTotalExpense()));
            holder.tvFreight.setText(String.format("¥%.2f", record.getFreight()));

            // 设置点击事件
            holder.ivInfo.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("productName", record.getProductName());
                intent.putExtra("productNum", record.getBatchNumber());
                getContext().startActivity(intent);
            });
        }

        return convertView;
    }
}