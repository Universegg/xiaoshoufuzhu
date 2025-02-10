package com.example.xiaoshoufuzhu.Reports;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.Reports.ProductLoss.ProductLossReportActivity;
import com.example.xiaoshoufuzhu.Reports.expense.ExpenseReportActivity;
import com.example.xiaoshoufuzhu.Reports.financial.FinancialReportActivity;
import com.example.xiaoshoufuzhu.Reports.income.IncomeReportActivity;
import com.example.xiaoshoufuzhu.Reports.customer.CustomerReportActivity;

public class ReportViewActivity extends AppCompatActivity {

    private Button btnIncomeReport, btnExpenseReport, btnFinancialReport, btnProductLossReport, btnCustomerReport;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);

        // 初始化按钮
        btnIncomeReport = findViewById(R.id.btnIncomeReport);
        btnExpenseReport = findViewById(R.id.btnExpenseReport);
        btnFinancialReport = findViewById(R.id.btnFinancialReport);
        btnProductLossReport = findViewById(R.id.btnProductLossReport);
        btnCustomerReport = findViewById(R.id.btnCustomerReport);

        // 设置按钮点击事件
        btnIncomeReport.setOnClickListener(v -> {
            Intent intent = new Intent(ReportViewActivity.this, IncomeReportActivity.class);
            startActivity(intent);
        });

        btnExpenseReport.setOnClickListener(v -> {
            Intent intent = new Intent(ReportViewActivity.this, ExpenseReportActivity.class);
            startActivity(intent);
        });

        btnFinancialReport.setOnClickListener(v -> {
            Intent intent = new Intent(ReportViewActivity.this, FinancialReportActivity.class);
            startActivity(intent);
        });

        btnProductLossReport.setOnClickListener(v -> {
            Intent intent = new Intent(ReportViewActivity.this, ProductLossReportActivity.class);
            startActivity(intent);
        });

        btnCustomerReport.setOnClickListener(v -> {
            Intent intent = new Intent(ReportViewActivity.this, CustomerReportActivity.class);
            startActivity(intent);
        });
    }
}