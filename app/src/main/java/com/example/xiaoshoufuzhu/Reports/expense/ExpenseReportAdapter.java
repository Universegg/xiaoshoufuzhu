package com.example.xiaoshoufuzhu.Reports.expense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;

import java.util.List;

public class ExpenseReportAdapter extends ArrayAdapter<ExpenseRecord> {

    public ExpenseReportAdapter(Context context, List<ExpenseRecord> expenseRecords) {
        super(context, 0, expenseRecords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取当前项的 ExpenseRecord 对象
        ExpenseRecord expenseRecord = getItem(position);

        // 检查是否存在可重用的视图，否则创建一个新的视图
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_expense_record, parent, false);
        }

        // 获取布局中的 TextView
        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvProductNum = convertView.findViewById(R.id.tvProductNum);
        TextView tvTotalExpense = convertView.findViewById(R.id.tvTotalExpense);

        // 设置 TextView 的内容
        if (expenseRecord != null) {
            tvProductName.setText(expenseRecord.getProductName());
            tvProductNum.setText(expenseRecord.getProductNum());
            tvTotalExpense.setText(String.valueOf(expenseRecord.getTotalExpense()));
        }

        // 返回显示的视图
        return convertView;
    }
}