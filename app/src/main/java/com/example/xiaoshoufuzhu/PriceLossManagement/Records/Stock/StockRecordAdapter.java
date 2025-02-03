package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Stock;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.PriceLossManagement.Records.Excepiton.getSupplier;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class StockRecordAdapter extends BaseAdapter {
    private Context context;
    private List<StockRecord> stockRecords;

    public StockRecordAdapter(Context context, List<StockRecord> stockRecords) {
        this.context = context;
        this.stockRecords = stockRecords;
    }

    @Override
    public int getCount() {
        return stockRecords.size();
    }

    @Override
    public Object getItem(int position) {
        return stockRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_stock_record, parent, false);
        }

        StockRecord stockRecord = stockRecords.get(position);

        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvNum = convertView.findViewById(R.id.tv_num);
        TextView tvPrice = convertView.findViewById(R.id.tv_price);
        TextView tvQuantity = convertView.findViewById(R.id.tv_quantity);
        TextView tvUpdatedAt = convertView.findViewById(R.id.tv_updated_at);
        TextView tvSupplierName = convertView.findViewById(R.id.tv_supplier_name);

        tvName.setText(stockRecord.getName());
        tvNum.setText(stockRecord.getNum());
        tvPrice.setText(String.valueOf(stockRecord.getPrice()));
        tvQuantity.setText(String.valueOf(stockRecord.getQuantity()));
        tvUpdatedAt.setText(stockRecord.getUpdatedAt());
        tvSupplierName.setText(stockRecord.getSupplierName()); // 设置供应商名称

        tvSupplierName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchSupplierDetailsTask().execute(stockRecord.getSid());
            }
        });

        return convertView;
    }

    private class FetchSupplierDetailsTask extends AsyncTask<Integer, Void, getSupplier> {
        @Override
        protected getSupplier doInBackground(Integer... params) {
            return fetchSupplierDetails(params[0]);
        }

        @Override
        protected void onPostExecute(getSupplier supplier) {
            if (supplier != null) {
                showSupplierDetailsDialog(supplier);
            } else {
                Toast.makeText(context, "无法获取供应商详细信息", Toast.LENGTH_SHORT).show();
            }
        }

        private getSupplier fetchSupplierDetails(int supplierId) {
            Connection connection = DatabaseHelper.getConnection();
            getSupplier supplier = null;
            if (connection != null) {
                try {
                    String query = "SELECT * FROM suppliers WHERE id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, supplierId);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        String name = resultSet.getString("name");
                        String phone = resultSet.getString("phone");
                        String address = resultSet.getString("address");

                        supplier = new getSupplier(supplierId, name, phone, address);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return supplier;
        }
    }

    private void showSupplierDetailsDialog(getSupplier supplier) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("供应商详细信息");
        builder.setMessage("名称: " + supplier.getName() + "\n电话: " + supplier.getPhone() + "\n地址: " + supplier.getAddress());
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}