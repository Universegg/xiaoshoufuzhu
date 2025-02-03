package com.example.xiaoshoufuzhu.Reports.financial;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

public class FinancialReportActivity extends AppCompatActivity {

    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvTotalLoss;
    private TextView tvTotalProfitLoss;
    private DatePicker datePicker;
    private Spinner spinnerTimePeriod;
    private String selectedTimePeriod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_report);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotalLoss = findViewById(R.id.tvTotalLoss);
        tvTotalProfitLoss = findViewById(R.id.tvTotalProfitLoss);
        datePicker = findViewById(R.id.datePicker);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);

        // 设置 Spinner 的监听器
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 获取选择的时间段类型
                selectedTimePeriod = (String) parent.getItemAtPosition(position);
                loadFinancialReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 默认选择按日
                selectedTimePeriod = "按日";
                loadFinancialReport();
            }
        });

        // 设置日期选择器的监听器
        datePicker.init(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> loadFinancialReport()
        );

        // 初次加载报表数据
        loadFinancialReport();
    }

    private void loadFinancialReport() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    double totalIncome = getTotalIncome(connection);
                    double totalExpense = getTotalExpense(connection);
                    double totalLoss = getTotalLoss(connection);
                    double totalProfitLoss = totalIncome - totalExpense - totalLoss;

                    runOnUiThread(() -> {
                        tvTotalIncome.setText("总收入: " + totalIncome);
                        tvTotalExpense.setText("总支出: " + totalExpense);
                        tvTotalLoss.setText("总损耗金额: " + totalLoss);
                        tvTotalProfitLoss.setText("总盈亏: " + totalProfitLoss);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(FinancialReportActivity.this, "", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(FinancialReportActivity.this, "数据库连接失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private double getTotalIncome(Connection connection) throws Exception {
        String query = "";
        String dateParam = "";

        // 根据选择的时间段类型构建查询语句
        if ("按年".equals(selectedTimePeriod)) {
            query =
                    "SELECT SUM(sales.total_price) AS total_income " +
                            "FROM sales " +
                            "WHERE YEAR(sales.sale_date) = ?";
            dateParam = String.format("%04d", datePicker.getYear());
        } else if ("按月".equals(selectedTimePeriod)) {
            query =
                    "SELECT SUM(sales.total_price) AS total_income " +
                            "FROM sales " +
                            "WHERE YEAR(sales.sale_date) = ? AND MONTH(sales.sale_date) = ?";
            dateParam = String.format("%04d", datePicker.getYear());
        } else if ("按日".equals(selectedTimePeriod)) {
            query =
                    "SELECT SUM(sales.total_price) AS total_income " +
                            "FROM sales " +
                            "WHERE DATE(sales.sale_date) = ?";
            dateParam = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
        }

        // 准备并执行查询语句
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, dateParam);
        if ("按月".equals(selectedTimePeriod)) {
            statement.setString(2, String.format("%02d", datePicker.getMonth() + 1));
        }
        ResultSet resultSet = statement.executeQuery();

        // 获取总收入
        if (resultSet.next()) {
            return resultSet.getDouble("total_income");
        }
        return 0;
    }

    private double getTotalExpense(Connection connection) throws Exception {
        // 使用相同的表 records_suppliers
        String query = "";
        String dateParam = "";

        if ("按年".equals(selectedTimePeriod)) {
            query = "SELECT SUM(total_price) AS total_expense FROM records_suppliers WHERE YEAR(purchase_date) = ?";
            dateParam = String.format("%04d", datePicker.getYear());
        } else if ("按月".equals(selectedTimePeriod)) {
            query = "SELECT SUM(total_price) AS total_expense FROM records_suppliers WHERE YEAR(purchase_date) = ? AND MONTH(purchase_date) = ?";
            dateParam = String.format("%04d", datePicker.getYear());
        } else if ("按日".equals(selectedTimePeriod)) {
            query = "SELECT SUM(total_price) AS total_expense FROM records_suppliers WHERE DATE(purchase_date) = ?";
            dateParam = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
        }

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, dateParam);
        if ("按月".equals(selectedTimePeriod)) {
            statement.setString(2, String.format("%02d", datePicker.getMonth() + 1));
        }
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getDouble("total_expense");
        }
        return 0;
    }

    private double getTotalLoss(Connection connection) throws Exception {
        String query = "";
        String dateParam = "";

        if ("按年".equals(selectedTimePeriod)) {
            query = "SELECT SUM(quantity * price) AS total_loss FROM losses JOIN products ON losses.product_id = products.id WHERE YEAR(loss_data) = ?";
            dateParam = String.format("%04d", datePicker.getYear());
        } else if ("按月".equals(selectedTimePeriod)) {
            query = "SELECT SUM(quantity * price) AS total_loss FROM losses JOIN products ON losses.product_id = products.id WHERE YEAR(loss_data) = ? AND MONTH(loss_data) = ?";
            dateParam = String.format("%04d-%02d", datePicker.getYear(), datePicker.getMonth() + 1);
        } else if ("按日".equals(selectedTimePeriod)) {
            query = "SELECT SUM(quantity * price) AS total_loss FROM losses JOIN products ON losses.product_id = products.id WHERE DATE(loss_data) = ?";
            dateParam = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
        }

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, dateParam);
        if ("按月".equals(selectedTimePeriod)) {
            statement.setString(2, String.format("%02d", datePicker.getMonth() + 1));
        }
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getDouble("total_loss");
        }
        return 0;
    }
}