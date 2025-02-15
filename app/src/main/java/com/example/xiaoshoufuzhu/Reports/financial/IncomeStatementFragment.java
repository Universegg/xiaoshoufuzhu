package com.example.xiaoshoufuzhu.Reports.financial;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class IncomeStatementFragment extends BaseFinancialFragment {
    private TextView tvRevenue, tvCost, tvProfit;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_income_statement, null);
        ((ViewGroup) rootView.findViewById(R.id.content_container)).addView(contentView);

        tvRevenue = contentView.findViewById(R.id.tv_revenue);
        tvCost = contentView.findViewById(R.id.tv_cost);
        tvProfit = contentView.findViewById(R.id.tv_profit);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 营业收入 = 线下 + 线上
                String revenueSql = "SELECT " +
                        "(SELECT COALESCE(SUM(rc.total_price),0) " +
                        "FROM records_customers rc " +
                        "WHERE " + getDateCondition("rc") + ") + " +
                        "(SELECT COALESCE(SUM(s.total_price),0) " +
                        "FROM sales s " +
                        "WHERE " + getDateCondition("s") + ") AS total_revenue";

                // 营业成本
                String costSql = "SELECT COALESCE(SUM(rs.quantity * rs.price),0) " +
                        "FROM records_suppliers rs " +
                        "WHERE " + getDateCondition("rs");

                // 执行查询
                Log.d("ProfitQuery", "Revenue SQL: " + revenueSql);
                ResultSet rs = stmt.executeQuery(revenueSql);
                double revenue = rs.next() ? rs.getDouble(1) : 0;

                Log.d("ProfitQuery", "Cost SQL: " + costSql);
                rs = stmt.executeQuery(costSql);
                double cost = rs.next() ? rs.getDouble(1) : 0;

                // 更新UI
                requireActivity().runOnUiThread(() -> {
                    tvRevenue.setText(String.format("营业收入：¥%.2f", revenue));
                    tvCost.setText(String.format("营业成本：¥%.2f", cost));
                    tvProfit.setText(String.format("营业利润：¥%.2f", revenue - cost));
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
}