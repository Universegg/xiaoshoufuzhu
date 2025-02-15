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

public class CashFlowFragment extends BaseFinancialFragment {
    private TextView tvCashIn, tvCashOut, tvNetCash;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_cash_flow, null);
        ((ViewGroup) rootView.findViewById(R.id.content_container)).addView(contentView);

        tvCashIn = contentView.findViewById(R.id.tv_cash_in);
        tvCashOut = contentView.findViewById(R.id.tv_cash_out);
        tvNetCash = contentView.findViewById(R.id.tv_net_cash);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 现金流入 = 线下已结清订单 + 线上销售（假设线上即时支付）
                String cashInSql = "SELECT " +
                        "(SELECT COALESCE(SUM(rc.total_price),0) " +
                        "FROM records_customers rc " +
                        "WHERE rc.state='结清' AND " + getDateCondition("rc") + ") + " +
                        "(SELECT COALESCE(SUM(s.total_price),0) " +
                        "FROM sales s " +
                        "WHERE " + getDateCondition("s") + ") AS total_cash_in";

                // 现金流出（采购支出）
                String cashOutSql = "SELECT COALESCE(SUM(rs.total_price + rs.freight),0) " +
                        "FROM records_suppliers rs " +
                        "WHERE rs.state='1' AND " + getDateCondition("rs");

                ResultSet rs = stmt.executeQuery(cashInSql);
                double cashIn = rs.next() ? rs.getDouble(1) : 0;

                rs = stmt.executeQuery(cashOutSql);
                double cashOut = rs.next() ? rs.getDouble(1) : 0;
                double netCash = cashIn - cashOut;

                requireActivity().runOnUiThread(() -> {
                    tvCashIn.setText(String.format("现金流入：¥%.2f", cashIn));
                    tvCashOut.setText(String.format("现金流出：¥%.2f", cashOut));
                    tvNetCash.setText(String.format("净现金流：¥%.2f", netCash));
                });

            } catch (Exception e) {
                Log.e("CASH_FLOW", "Error loading data", e);
                showError("现金流量表加载失败：" + e.getMessage());
            } finally {
                showLoading(false);
            }
        }).start();
    }
}