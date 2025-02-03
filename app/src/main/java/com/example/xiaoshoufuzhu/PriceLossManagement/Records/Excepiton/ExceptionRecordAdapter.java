package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Excepiton;


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
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ExceptionRecordAdapter extends BaseAdapter {
    private Context context;
    private List<ExceptionRecord> exceptionRecords;

    public ExceptionRecordAdapter(Context context, List<ExceptionRecord> exceptionRecords) {
        this.context = context;
        this.exceptionRecords = exceptionRecords;
    }

    @Override
    public int getCount() {
        return exceptionRecords.size();
    }

    @Override
    public Object getItem(int position) {
        return exceptionRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_exception_record, parent, false);
        }

        ExceptionRecord exceptionRecord = exceptionRecords.get(position);

        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvNum = convertView.findViewById(R.id.tv_num);
        TextView tvQuantity = convertView.findViewById(R.id.tv_quantity);
        TextView tvPrice = convertView.findViewById(R.id.tv_price);
        TextView tvDate = convertView.findViewById(R.id.tv_date);
        TextView tvReason = convertView.findViewById(R.id.tv_reason);
        TextView tvSupplierName = convertView.findViewById(R.id.tv_supplier_name); // 获取供应商名称视图

        tvName.setText(exceptionRecord.getName());
        tvNum.setText(exceptionRecord.getNum());
        tvQuantity.setText(String.valueOf(exceptionRecord.getQuantity()));
        tvPrice.setText(String.valueOf(exceptionRecord.getPrice()));
        tvDate.setText(exceptionRecord.getDate());
        tvReason.setText(exceptionRecord.getReason());
        tvSupplierName.setText(exceptionRecord.getSupplierName()); // 设置供应商名称

        tvSupplierName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchSupplierDetailsTask().execute(exceptionRecord.getSid());
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
        protected void onPostExecute(getSupplier getSupplier) {
            if (getSupplier != null) {
                showSupplierDetailsDialog(getSupplier);
            } else {
                Toast.makeText(context, "无法获取供应商详细信息", Toast.LENGTH_SHORT).show();
            }
        }

        private getSupplier fetchSupplierDetails(int supplierId) {
            Connection connection = DatabaseHelper.getConnection();
            getSupplier getSupplier = null;
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

                        getSupplier = new getSupplier(supplierId, name, phone, address);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return getSupplier;
        }
    }

    private void showSupplierDetailsDialog(getSupplier getSupplier) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("供应商详细信息");
        builder.setMessage("名称: " + getSupplier.getName() + "\n电话: " + getSupplier.getPhone() + "\n地址: " + getSupplier.getAddress());
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}