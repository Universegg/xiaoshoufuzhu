package com.example.xiaoshoufuzhu.Reports.income;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IncomeReportActivity extends AppCompatActivity {

    private TextView tvTotalIncome;
    private ListView lvIncomeReport;
    private DatePicker datePicker;
    private Spinner spinnerTimePeriod;
    private IncomeReportAdapter incomeReportAdapter;
    private List<IncomeRecord> incomeRecordList;
    private String selectedTimePeriod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_report);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        lvIncomeReport = findViewById(R.id.lvIncomeReport);
        datePicker = findViewById(R.id.datePicker);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);

        incomeRecordList = new ArrayList<>();
        incomeReportAdapter = new IncomeReportAdapter(this, incomeRecordList);
        lvIncomeReport.setAdapter(incomeReportAdapter);

        // 设置 Spinner 的监听器
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 获取选择的时间段类型
                selectedTimePeriod = (String) parent.getItemAtPosition(position);
                loadIncomeReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 默认选择按日
                selectedTimePeriod = "按日";
                loadIncomeReport();
            }
        });

        // 设置日期选择器的监听器
        datePicker.init(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> loadIncomeReport()
        );

        // 初次加载报表数据
        loadIncomeReport();
    }

    private void loadIncomeReport() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "";
                    String dateParam = "";

                    // 根据选择的时间段类型构建查询语句
                    if ("按年".equals(selectedTimePeriod)) {
                        query =
                                "SELECT products.name AS product_name, products.num AS product_num, " +
                                        "SUM(sales.total_price) AS total_income " +
                                        "FROM sales " +
                                        "JOIN products ON sales.product_id = products.id " +
                                        "WHERE YEAR(sales.sale_date) = ? " +
                                        "GROUP BY products.name, products.num";
                        dateParam = String.format("%04d", datePicker.getYear());
                    } else if ("按月".equals(selectedTimePeriod)) {
                        query =
                                "SELECT products.name AS product_name, products.num AS product_num, " +
                                        "SUM(sales.total_price) AS total_income " +
                                        "FROM sales " +
                                        "JOIN products ON sales.product_id = products.id " +
                                        "WHERE YEAR(sales.sale_date) = ? AND MONTH(sales.sale_date) = ? " +
                                        "GROUP BY products.name, products.num";
                        dateParam = String.format("%04d", datePicker.getYear());
                    } else if ("按日".equals(selectedTimePeriod)) {
                        query =
                                "SELECT products.name AS product_name, products.num AS product_num, " +
                                        "SUM(sales.total_price) AS total_income " +
                                        "FROM sales " +
                                        "JOIN products ON sales.product_id = products.id " +
                                        "WHERE DATE(sales.sale_date) = ? " +
                                        "GROUP BY products.name, products.num";
                        dateParam = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                    }

                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, dateParam);
                    if ("按月".equals(selectedTimePeriod)) {
                        statement.setString(2, String.format("%02d", datePicker.getMonth() + 1));
                    }
                    ResultSet resultSet = statement.executeQuery();

                    double totalIncome = 0;
                    incomeRecordList.clear();
                    while (resultSet.next()) {
                        String productName = resultSet.getString("product_name");
                        String productNum = resultSet.getString("product_num");
                        double totalIncomeForProduct = resultSet.getDouble("total_income");
                        totalIncome += totalIncomeForProduct;

                        incomeRecordList.add(new IncomeRecord(productName, productNum, totalIncomeForProduct));
                    }

                    double finalTotalIncome = totalIncome;
                    runOnUiThread(() -> {
                        tvTotalIncome.setText("总收入: " + finalTotalIncome);
                        incomeReportAdapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(IncomeReportActivity.this, "", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(IncomeReportActivity.this, "数据库连接失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}