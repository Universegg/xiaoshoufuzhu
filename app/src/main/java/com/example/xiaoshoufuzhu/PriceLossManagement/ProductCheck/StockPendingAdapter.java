package com.example.xiaoshoufuzhu.PriceLossManagement.ProductCheck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;

import java.util.List;

public class StockPendingAdapter extends BaseAdapter {
    private Context context;
    private List<StockPending> stockPendingList;

    public StockPendingAdapter(Context context, List<StockPending> stockPendingList) {
        this.context = context;
        this.stockPendingList = stockPendingList;
    }

    @Override
    public int getCount() {
        return stockPendingList.size();
    }

    @Override
    public Object getItem(int position) {
        return stockPendingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_stock_pending, parent, false);
        }

        StockPending stockPending = stockPendingList.get(position);

        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvNum = convertView.findViewById(R.id.tv_num);
        TextView tvPrice = convertView.findViewById(R.id.tv_price);
        TextView tvQuantity = convertView.findViewById(R.id.tv_quantity);
        TextView tvState = convertView.findViewById(R.id.tv_state);

        tvName.setText(stockPending.getName());
        tvNum.setText(stockPending.getNum());
        tvPrice.setText(String.valueOf(stockPending.getPrice()));
        tvQuantity.setText(String.valueOf(stockPending.getQuantity()));
        tvState.setText(stockPending.getState());

        return convertView;
    }

    public void updateData(List<StockPending> newStockPendingList) {
        this.stockPendingList = newStockPendingList;
        notifyDataSetChanged();
    }
}