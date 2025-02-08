package com.example.xiaoshoufuzhu.Reports.income;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IncomeReportActivity extends AppCompatActivity {

    private TextView tvTotalIncome;
    private TextView tvAccountReceivable;
    private ListView lvIncomeReport;
    private ImageView ivDatePicker;
    private Spinner spinnerTimePeriod;
    private IncomeReportAdapter incomeReportAdapter;
    private List<IncomeRecord> incomeRecordList;
    private String selectedTimePeriod;
    private Calendar selectedDate;

    private static final String TAG = "IncomeReportActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_report);

        initViews();
        setupAdapters();
        setupListeners();
        loadIncomeReport();
    }

    private void initViews() {
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvAccountReceivable = findViewById(R.id.tvAccountReceivable);
        lvIncomeReport = findViewById(R.id.lvIncomeReport);
        ivDatePicker = findViewById(R.id.ivDatePicker);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);
    }

    private void setupAdapters() {
        incomeRecordList = new ArrayList<>();
        incomeReportAdapter = new IncomeReportAdapter(this, incomeRecordList);
        lvIncomeReport.setAdapter(incomeReportAdapter);
    }

    private void setupListeners() {
        selectedDate = Calendar.getInstance();

        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = (String) parent.getItemAtPosition(position);
                loadIncomeReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimePeriod = "按日";
                loadIncomeReport();
            }
        });

        ivDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                IncomeReportActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    loadIncomeReport();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadIncomeReport() {
        new Thread(new LoadTask(this, buildQuery())).start();
    }

    private QueryInfo buildQuery() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH) + 1;
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        String baseQuery = "SELECT products.name AS product_name, products.num AS product_num, " +
                "records_customers.id AS rc_id, sales.id AS s_id, " +
                "SUM(CASE WHEN records_customers.state = '结清' THEN records_customers.total_price ELSE 0 END) AS customer_total_income, " +
                "SUM(CASE WHEN records_customers.state = '赊账' THEN records_customers.total_price ELSE 0 END) AS customer_receivable_amount, " +
                "SUM(sales.total_price) AS sales_total_income " +
                "FROM products " +
                "LEFT JOIN records_customers ON records_customers.product_id = products.id AND %s " +
                "LEFT JOIN sales ON sales.product_id = products.id AND %s " +
                "GROUP BY products.name, products.num, rc_id, s_id";

        List<Object> params = new ArrayList<>();
        String recordsCondition;
        String salesCondition;

        if (selectedTimePeriod == null) {
            selectedTimePeriod = "按日";
        }

        switch (selectedTimePeriod) {
            case "按年":
                recordsCondition = "YEAR(records_customers.sale_date) = ?";
                salesCondition = "YEAR(sales.sale_date) = ?";
                params.add(year);
                params.add(year);
                break;
            case "按月":
                recordsCondition = "YEAR(records_customers.sale_date) = ? AND MONTH(records_customers.sale_date) = ?";
                salesCondition = "YEAR(sales.sale_date) = ? AND MONTH(sales.sale_date) = ?";
                params.add(year);
                params.add(month);
                params.add(year);
                params.add(month);
                break;
            default: // 按日
                String dateStr = String.format("%04d-%02d-%02d", year, month, day);
                recordsCondition = "DATE(records_customers.sale_date) = ?";
                salesCondition = "DATE(sales.sale_date) = ?";
                params.add(dateStr);
                params.add(dateStr);
                break;
        }

        return new QueryInfo(String.format(baseQuery, recordsCondition, salesCondition), params);
    }

    private static class LoadTask implements Runnable {
        private final WeakReference<IncomeReportActivity> activityRef;
        private final QueryInfo queryInfo;

        LoadTask(IncomeReportActivity activity, QueryInfo queryInfo) {
            this.activityRef = new WeakReference<>(activity);
            this.queryInfo = queryInfo;
        }

        @Override
        public void run() {
            IncomeReportActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;

            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                connection = DatabaseHelper.getConnection();
                if (connection == null) {
                    activity.showError("数据库连接失败");
                    return;
                }

                statement = connection.prepareStatement(queryInfo.sql);
                for (int i = 0; i < queryInfo.params.size(); i++) {
                    Object param = queryInfo.params.get(i);
                    if (param instanceof Integer) {
                        statement.setInt(i + 1, (Integer) param);
                    } else {
                        statement.setString(i + 1, param.toString());
                    }
                }

                resultSet = statement.executeQuery();

                Map<String, IncomeRecord> recordMap = new HashMap<>();
                Set<Integer> processedRcIds = new HashSet<>();
                Set<Integer> processedSIds = new HashSet<>();
                double totalIncome = 0;
                double accountReceivable = 0;

                while (resultSet.next()) {
                    String productName = resultSet.getString("product_name");
                    String productNum = resultSet.getString("product_num");
                    int rcId = resultSet.getInt("rc_id");
                    int sId = resultSet.getInt("s_id");
                    double customerIncome = resultSet.getDouble("customer_total_income");
                    double receivable = resultSet.getDouble("customer_receivable_amount");
                    double salesIncome = resultSet.getDouble("sales_total_income");

                    String compositeKey = productName + "|" + productNum;
                    IncomeRecord record = recordMap.get(compositeKey);

                    if (record == null) {
                        record = new IncomeRecord(productName, productNum, 0, 0);
                        recordMap.put(compositeKey, record);
                    }

                    if (!processedRcIds.contains(rcId)) {
                        record.setTotalIncome(record.getTotalIncome() + customerIncome);
                        record.setAccountReceivable(record.getAccountReceivable() + receivable);
                        totalIncome += customerIncome;
                        accountReceivable += receivable;
                        processedRcIds.add(rcId);
                    }

                    if (!processedSIds.contains(sId)) {
                        record.setTotalIncome(record.getTotalIncome() + salesIncome);
                        totalIncome += salesIncome;
                        processedSIds.add(sId);
                    }

                    // Log the income addition with table source and id
                    Log.i(TAG, "Added income for product: " + productName + " (" + productNum + "), Customer Income: " + customerIncome + ", Sales Income: " + salesIncome + ", Receivable: " + receivable + ", Total Income: " + totalIncome + ", Total Receivable: " + accountReceivable + ", Source: records_customers(id=" + rcId + "), sales(id=" + sId + ")");
                }

                activity.updateUI(recordMap.values(), totalIncome, accountReceivable);

            } catch (SQLException e) {
                activity.showError("数据库查询异常");
                e.printStackTrace();
            } catch (Exception e) {
                activity.showError("系统错误");
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateUI(Iterable<IncomeRecord> records, double totalIncome, double accountReceivable) {
        runOnUiThreadIfAlive(() -> {
            incomeRecordList.clear();
            for (IncomeRecord record : records) {
                incomeRecordList.add(record);
            }
            tvTotalIncome.setText(String.format("实际收入: %.2f（元）", totalIncome));
            tvAccountReceivable.setText(String.format("赊账（未回款）: %.2f（元）", accountReceivable));
            incomeReportAdapter.notifyDataSetChanged();
        });
    }

    private void runOnUiThreadIfAlive(Runnable action) {
        if (!isFinishing()) {
            runOnUiThread(() -> {
                if (!isFinishing()) {
                    action.run();
                }
            });
        }
    }

    private void showError(String message) {
        runOnUiThreadIfAlive(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private static class QueryInfo {
        final String sql;
        final List<Object> params;

        QueryInfo(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }
}