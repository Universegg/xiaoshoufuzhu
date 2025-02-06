package com.example.xiaoshoufuzhu.Reports.income;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;

import java.util.List;

public class IncomeReportAdapter extends ArrayAdapter<IncomeRecord> {

    public IncomeReportAdapter(Context context, List<IncomeRecord> incomeRecords) {
        super(context, 0, incomeRecords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取当前项的 IncomeRecord 对象
        IncomeRecord incomeRecord = getItem(position);

        // 检查是否存在可重用的视图，否则创建一个新的视图
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_income_record, parent, false);
        }

        // 获取布局中的 TextView
        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvProductNum = convertView.findViewById(R.id.tvProductNum);
        TextView tvTotalIncome = convertView.findViewById(R.id.tvTotalIncome);
        TextView tvAccountReceivable = convertView.findViewById(R.id.tvAccountReceivable);

        // 设置 TextView 的内容
        if (incomeRecord != null) {
            tvProductName.setText(incomeRecord.getProductName());
            tvProductNum.setText(incomeRecord.getProductNum());
            tvTotalIncome.setText(String.valueOf(incomeRecord.getTotalIncome()));
            tvAccountReceivable.setText(String.valueOf(incomeRecord.getAccountReceivable()));
        }

        // 返回显示的视图
        return convertView;
    }
}