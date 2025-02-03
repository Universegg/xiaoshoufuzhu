package com.example.xiaoshoufuzhu.Reports.expense;

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

public class ExpenseReportActivity extends AppCompatActivity {

    private TextView tvTotalExpense;
    private ListView lvExpenseReport;
    private DatePicker datePicker;
    private Spinner spinnerTimePeriod;
    private ExpenseReportAdapter expenseReportAdapter;
    private List<ExpenseRecord> expenseRecordList;
    private String selectedTimePeriod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_report);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        lvExpenseReport = findViewById(R.id.lvExpenseReport);
        datePicker = findViewById(R.id.datePicker);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);

        expenseRecordList = new ArrayList<>();
        expenseReportAdapter = new ExpenseReportAdapter(this, expenseRecordList);
        lvExpenseReport.setAdapter(expenseReportAdapter);

        // 设置 Spinner 的监听器
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 获取选择的时间段类型
                selectedTimePeriod = (String) parent.getItemAtPosition(position);
                loadExpenseReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 默认选择按日
                selectedTimePeriod = "按日";
                loadExpenseReport();
            }
        });

        // 设置日期选择器的监听器
        datePicker.init(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> loadExpenseReport()
        );

        // 初次加载报表数据
        loadExpenseReport();
    }

    private void loadExpenseReport() {
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
                                        "SUM(records_suppliers.total_price) AS total_expense " +
                                        "FROM records_suppliers " +
                                        "JOIN products ON records_suppliers.product_id = products.id " +
                                        "WHERE YEAR(records_suppliers.purchase_date) = ? " +
                                        "GROUP BY products.name, products.num";
                        dateParam = String.format("%04d", datePicker.getYear());
                    } else if ("按月".equals(selectedTimePeriod)) {
                        query =
                                "SELECT products.name AS product_name, products.num AS product_num, " +
                                        "SUM(records_suppliers.total_price) AS total_expense " +
                                        "FROM records_suppliers " +
                                        "JOIN products ON records_suppliers.product_id = products.id " +
                                        "WHERE YEAR(records_suppliers.purchase_date) = ? AND MONTH(records_suppliers.purchase_date) = ? " +
                                        "GROUP BY products.name, products.num";
                        dateParam = String.format("%04d-%02d", datePicker.getYear(), datePicker.getMonth() + 1);
                    } else if ("按日".equals(selectedTimePeriod)) {
                        query =
                                "SELECT products.name AS product_name, products.num AS product_num, " +
                                        "SUM(records_suppliers.total_price) AS total_expense " +
                                        "FROM records_suppliers " +
                                        "JOIN products ON records_suppliers.product_id = products.id " +
                                        "WHERE DATE(records_suppliers.purchase_date) = ? " +
                                        "GROUP BY products.name, products.num";
                        dateParam = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                    }

                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, dateParam);
                    if ("按月".equals(selectedTimePeriod)) {
                        statement.setString(2, String.format("%02d", datePicker.getMonth() + 1));
                    }
                    ResultSet resultSet = statement.executeQuery();

                    double totalExpense = 0;
                    expenseRecordList.clear();
                    while (resultSet.next()) {
                        String productName = resultSet.getString("product_name");
                        String productNum = resultSet.getString("product_num");
                        double totalExpenseForProduct = resultSet.getDouble("total_expense");
                        totalExpense += totalExpenseForProduct;

                        expenseRecordList.add(new ExpenseRecord(productName, productNum, totalExpenseForProduct));
                    }

                    double finalTotalExpense = totalExpense;
                    runOnUiThread(() -> {
                        tvTotalExpense.setText("总支出: " + finalTotalExpense);
                        expenseReportAdapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(ExpenseReportActivity.this, "", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(ExpenseReportActivity.this, "数据库连接失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}