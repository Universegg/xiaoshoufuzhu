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

public class BalanceSheetFragment extends BaseFinancialFragment {
    private TextView tvAssets, tvLiabilities, tvEquity;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_balance_sheet, null);
        ((ViewGroup) rootView.findViewById(R.id.content_container)).addView(contentView);

        tvAssets = contentView.findViewById(R.id.tv_assets);
        tvLiabilities = contentView.findViewById(R.id.tv_liabilities);
        tvEquity = contentView.findViewById(R.id.tv_equity);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 资产 = 应收账款 + 存货
                String assetsSql = "SELECT " +
                        "(SELECT COALESCE(SUM(total_price),0) FROM records_customers WHERE state='赊账') + " +
                        "(SELECT COALESCE(SUM(p.stock * unit_cost),0) FROM (" +
                        "SELECT p.id, p.stock, " +
                        "COALESCE(SUM(rs.total_price)/NULLIF(SUM(rs.quantity),0), 0) AS unit_cost " +
                        "FROM products p " +
                        "LEFT JOIN records_suppliers rs ON p.num = rs.num " +
                        "GROUP BY p.id" +
                        ") t JOIN products p ON t.id = p.id) AS total_assets";

                // 负债 = 应付账款
                String liabilitiesSql = "SELECT COALESCE(SUM(total_price + freight),0) " +
                        "FROM records_suppliers WHERE state='0'";

                ResultSet rs = stmt.executeQuery(assetsSql);
                double assets = rs.next() ? rs.getDouble(1) : 0;

                rs = stmt.executeQuery(liabilitiesSql);
                double liabilities = rs.next() ? rs.getDouble(1) : 0;
                double equity = assets - liabilities;

                requireActivity().runOnUiThread(() -> {
                    tvAssets.setText(String.format("总资产：¥%.2f", assets));
                    tvLiabilities.setText(String.format("总负债：¥%.2f", liabilities));
                    tvEquity.setText(String.format("所有者权益：¥%.2f", equity));
                });

            } catch (Exception e) {
                Log.e("BALANCE_SHEET", "Error loading data", e);
                showError("资产负债表加载失败：" + e.getMessage());
            } finally {
                showLoading(false);
            }
        }).start();
    }
}