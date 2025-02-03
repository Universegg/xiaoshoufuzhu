package com.example.xiaoshoufuzhu.SalesManagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SalesManagement.model.RetailSales;

import java.util.List;

public class RetailSalesAdapter extends BaseAdapter {

    private Context context;
    private List<RetailSales> salesList;
    private LayoutInflater inflater;

    public RetailSalesAdapter(Context context, List<RetailSales> salesList) {
        this.context = context;
        this.salesList = salesList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return salesList.size();
    }

    @Override
    public Object getItem(int position) {
        return salesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_retail_sales, parent, false);
            holder = new ViewHolder();
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvBatchNumber = convertView.findViewById(R.id.tvBatchNumber);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvUnitPrice = convertView.findViewById(R.id.tvUnitPrice);
            holder.tvActualAmount = convertView.findViewById(R.id.tvActualAmount);
            holder.tvReceivableAmount = convertView.findViewById(R.id.tvReceivableAmount);
            holder.tvSaleDate = convertView.findViewById(R.id.tvSaleDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RetailSales sales = salesList.get(position);
        holder.tvProductName.setText(sales.getProductName());
        holder.tvBatchNumber.setText(sales.getBatchNo());
        holder.tvQuantity.setText(String.valueOf(sales.getQuantity()));
        holder.tvUnitPrice.setText(String.valueOf(sales.getUnitPrice()));
        holder.tvActualAmount.setText(String.valueOf(sales.getTotalPrice()));
        holder.tvReceivableAmount.setText(String.valueOf(sales.getQuantity() * sales.getUnitPrice()));  // 使用数据库中的单价计算应收金额
        holder.tvSaleDate.setText(sales.getSaleDate());

        return convertView;
    }

    private static class ViewHolder {
        TextView tvProductName;
        TextView tvBatchNumber;
        TextView tvQuantity;
        TextView tvUnitPrice;
        TextView tvActualAmount;
        TextView tvReceivableAmount;
        TextView tvSaleDate;
    }
}