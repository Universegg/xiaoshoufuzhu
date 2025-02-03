package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Loss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;

import java.util.List;

public class LossRecordAdapter extends BaseAdapter {
    private Context context;
    private List<LossRecord> lossRecordList;

    public LossRecordAdapter(Context context, List<LossRecord> lossRecordList) {
        this.context = context;
        this.lossRecordList = lossRecordList;
    }

    @Override
    public int getCount() {
        return lossRecordList.size();
    }

    @Override
    public Object getItem(int position) {
        return lossRecordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_loss_record, parent, false);
        }

        LossRecord lossRecord = lossRecordList.get(position);

        TextView tvProductName = convertView.findViewById(R.id.tv_product_name);
        TextView tvBatchNumber = convertView.findViewById(R.id.tv_batch_number);
        TextView tvQuantity = convertView.findViewById(R.id.tv_quantity);
        TextView tvLossReason = convertView.findViewById(R.id.tv_loss_reason);
        TextView tvLossDate = convertView.findViewById(R.id.tv_loss_date);
        TextView tvLossAmount = convertView.findViewById(R.id.tv_loss_amount);

        tvProductName.setText(lossRecord.getProductName());
        tvBatchNumber.setText(lossRecord.getBatchNumber());
        tvQuantity.setText(String.valueOf(lossRecord.getQuantity()));
        tvLossReason.setText(lossRecord.getLossReason());
        tvLossDate.setText(lossRecord.getLossDate());
        tvLossAmount.setText(String.valueOf(lossRecord.getLossAmount()));

        return convertView;
    }
}