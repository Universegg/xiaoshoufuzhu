package com.example.xiaoshoufuzhu.Reports.financial;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class IncomeStatementFragment extends BaseFinancialFragment {
    private TextView tvRevenue, tvCost, tvProfit;
    private Button btnRevenueDetail;
    private double customerRevenue;
    private double salesRevenue;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_income_statement, null);
        ((ViewGroup) rootView.findViewById(R.id.content_container)).addView(contentView);

        tvRevenue = contentView.findViewById(R.id.tv_revenue);
        tvCost = contentView.findViewById(R.id.tv_cost);
        tvProfit = contentView.findViewById(R.id.tv_profit);
        btnRevenueDetail = contentView.findViewById(R.id.btn_revenue_detail);

        btnRevenueDetail.setOnClickListener(v -> showRevenueDetails());
    }

    @Override
    protected void loadData() {
        showLoading(true);
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 修改后的收入查询SQL
                String revenueSql = "SELECT " +
                        "(SELECT COALESCE(SUM(rc.total_price), 0) " +
                        "FROM records_customers rc " +
                        "WHERE " + getDateCondition("rc") + ") AS customer_rev, " +
                        "(SELECT COALESCE(SUM(s.total_price), 0) " +
                        "FROM sales s " +
                        "WHERE " + getDateCondition("s") + ") AS sales_rev";

                // 营业成本查询保持不变
                String costSql = "SELECT COALESCE(SUM(rs.quantity * rs.price), 0) " +
                        "FROM records_suppliers rs " +
                        "WHERE " + getDateCondition("rs");

                // 执行收入查询
                Log.d("ProfitQuery", "Revenue SQL: " + revenueSql);
                ResultSet rs = stmt.executeQuery(revenueSql);
                if (rs.next()) {
                    customerRevenue = rs.getDouble("customer_rev");
                    salesRevenue = rs.getDouble("sales_rev");
                }

                // 执行成本查询
                Log.d("ProfitQuery", "Cost SQL: " + costSql);
                rs = stmt.executeQuery(costSql);
                double cost = rs.next() ? rs.getDouble(1) : 0;

                // 更新UI
                requireActivity().runOnUiThread(() -> {
                    double totalRevenue = customerRevenue + salesRevenue;
                    tvRevenue.setText(String.format("营业收入：¥%.2f", totalRevenue));
                    tvCost.setText(String.format("营业成本：¥%.2f", cost));
                    tvProfit.setText(String.format("营业利润：¥%.2f", totalRevenue - cost));
                    progressBar.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                Log.e("ProfitError", "加载失败", e);
                requireActivity().runOnUiThread(() -> {
                    tvError.setText("数据加载失败，请检查网络连接");
                    tvError.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void showRevenueDetails() {
        new AlertDialog.Builder(requireContext())
                .setTitle("收入明细")
                .setMessage(String.format(
                        "采购商销售收入：¥%.2f\n\n散户销售收入：¥%.2f",
                        customerRevenue,
                        salesRevenue
                ))
                .setPositiveButton("确定", null)
                .show();
    }
}