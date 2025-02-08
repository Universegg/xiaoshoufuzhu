package com.example.xiaoshoufuzhu.SupplierSalesManagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.model.PurchaseRecord;

import java.util.List;

public class PurchaseRecordAdapter extends ArrayAdapter<PurchaseRecord> {

    public PurchaseRecordAdapter(Context context, List<PurchaseRecord> purchaseRecords) {
        super(context, 0, purchaseRecords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PurchaseRecord purchaseRecord = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_purchase_record, parent, false);
        }

        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvBatchNo = convertView.findViewById(R.id.tvBatchNo);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvUnitPrice = convertView.findViewById(R.id.tvUnitPrice);
        TextView tvPurchaseDate = convertView.findViewById(R.id.tvPurchaseDate);
        TextView tvTotalPrice = convertView.findViewById(R.id.tvTotalPrice);
        TextView tvFreight = convertView.findViewById(R.id.tvFreight); // 新增运费显示

        tvProductName.setText(purchaseRecord.getProductName());
        tvBatchNo.setText(purchaseRecord.getBatchNo());
        tvQuantity.setText(String.valueOf(purchaseRecord.getQuantity()));
        tvUnitPrice.setText(String.valueOf(purchaseRecord.getPrice()));
        tvPurchaseDate.setText(purchaseRecord.getPurchaseDate());
        tvTotalPrice.setText(String.valueOf(purchaseRecord.getTotalPrice()));
        tvFreight.setText(String.valueOf(purchaseRecord.getFreight())); // 显示运费

        return convertView;
    }
}