package com.example.xiaoshoufuzhu.SalesManagement.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SalesManagement.model.SalesRecord;
import com.example.xiaoshoufuzhu.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SalesRecordAdapter extends ArrayAdapter<SalesRecord> {

    private Context context;
    private List<SalesRecord> salesRecords;

    public SalesRecordAdapter(Context context, List<SalesRecord> salesRecords) {
        super(context, 0, salesRecords);
        this.context = context;
        this.salesRecords = salesRecords;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SalesRecord record = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sales_record, parent, false);
        }

        // 获取视图中的元素
        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvBatchNumber = convertView.findViewById(R.id.tvBatchNumber);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvUnitPrice = convertView.findViewById(R.id.tvUnitPrice);
        TextView tvSaleDate = convertView.findViewById(R.id.tvSaleDate);
        TextView tvState = convertView.findViewById(R.id.tvState);
        Button btnToggleState = convertView.findViewById(R.id.btnToggleState);
        TextView tvActualAmount = convertView.findViewById(R.id.tvActualAmount); // 实收金额
        TextView tvReceivableAmount = convertView.findViewById(R.id.tvReceivableAmount); // 应收金额

        // 设置数据
        if (record != null) {
            tvProductName.setText(record.getProductName());
            tvBatchNumber.setText(record.getBatchNo());
            tvQuantity.setText(String.valueOf(record.getQuantity()));
            tvUnitPrice.setText(String.format("%.2f", record.getPrice())); // 单价
            tvSaleDate.setText(record.getSaleDate());
            tvState.setText(record.getState());
            tvActualAmount.setText(String.format("%.2f", record.getActualAmount())); // 实收金额
            tvReceivableAmount.setText(String.format("%.2f", record.getReceivableAmount())); // 应收金额

            // 根据状态设置颜色
            if (record.getState().equals("结清")) {
                tvState.setTextColor(getContext().getResources().getColor(R.color.green));
                btnToggleState.setVisibility(View.GONE); // 隐藏按钮
            } else {
                tvState.setTextColor(getContext().getResources().getColor(R.color.red));
                btnToggleState.setVisibility(View.VISIBLE); // 显示按钮
            }

            btnToggleState.setOnClickListener(v -> {
                // 切换状态
                String newState = "结清";
                record.setState(newState);
                tvState.setText(newState);

                // 更新数据库中的状态
                new Thread(() -> {
                    Connection connection = DatabaseHelper.getConnection();
                    if (connection != null) {
                        try {
                            connection.setAutoCommit(false); // 开始事务

                            // 更新销售记录状态
                            String updateStateQuery = "UPDATE records_customers SET state = ? WHERE id = ?";
                            PreparedStatement updateStateStmt = connection.prepareStatement(updateStateQuery);
                            updateStateStmt.setString(1, newState);
                            updateStateStmt.setInt(2, record.getId());
                            updateStateStmt.executeUpdate();

                            // 减少采购商的赊账金额
                            String updateCustomerQuery = "UPDATE customers SET amount = amount - ? WHERE id = ?";
                            PreparedStatement updateCustomerStmt = connection.prepareStatement(updateCustomerQuery);
                            updateCustomerStmt.setDouble(1, record.getActualAmount());
                            updateCustomerStmt.setInt(2, record.getCid());
                            updateCustomerStmt.executeUpdate();

                            connection.commit(); // 提交事务

                            ((Activity) getContext()).runOnUiThread(() -> {
                                Toast.makeText(getContext(), "状态更新成功", Toast.LENGTH_SHORT).show();
                                // 更新颜色和隐藏按钮
                                tvState.setTextColor(getContext().getResources().getColor(R.color.green));
                                btnToggleState.setVisibility(View.GONE);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                connection.rollback(); // 出现异常时回滚事务
                            } catch (SQLException rollbackEx) {
                                rollbackEx.printStackTrace();
                            }
                        } finally {
                            try {
                                connection.setAutoCommit(true); // 恢复自动提交
                                connection.close(); // 关闭连接
                            } catch (SQLException autoCommitEx) {
                                autoCommitEx.printStackTrace();
                            }
                        }
                    }
                }).start();
            });
        }

        return convertView;
    }
}