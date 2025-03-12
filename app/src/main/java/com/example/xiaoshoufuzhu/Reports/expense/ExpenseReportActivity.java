package com.example.xiaoshoufuzhu.Reports.expense;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Map;


public class ExpenseReportActivity extends AppCompatActivity {

    private TextView tvTotalExpense;
    private TextView tvTotalFreight;
    private ListView lvExpenseReport;
    private ImageView ivDatePicker;
    private Spinner spinnerTimePeriod;
    private ExpenseReportAdapter expenseAdapter;
    private List<ExpenseRecord> expenseRecords;
    private String selectedTimePeriod;
    private Calendar selectedDate;
    private PieChart pieChart;
    private BarChart barChart;


    private static final String TAG = "ExpenseReportActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_report);

        initViews();
        setupAdapters();
        setupListeners();
        loadExpenseReport();
    }

    private void initViews() {
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotalFreight = findViewById(R.id.tvTotalFreight);
        lvExpenseReport = findViewById(R.id.lvExpenseReport);
        ivDatePicker = findViewById(R.id.ivDatePicker);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
    }

    private void setupAdapters() {
        expenseRecords = new ArrayList<>();
        expenseAdapter = new ExpenseReportAdapter(this, expenseRecords);
        lvExpenseReport.setAdapter(expenseAdapter);
    }

    private void setupListeners() {
        selectedDate = Calendar.getInstance();

        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = (String) parent.getItemAtPosition(position);
                loadExpenseReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimePeriod = "按日";
                loadExpenseReport();
            }
        });

        ivDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ExpenseReportActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    loadExpenseReport();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadExpenseReport() {
        new Thread(new LoadTask(this, buildQuery())).start();
    }

    private QueryInfo buildQuery() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH) + 1;
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        String baseQuery = "SELECT name, num, " +
                "SUM(total_price) AS total_expense, " +
                "SUM(freight) AS total_freight " +
                "FROM records_suppliers " +
                "WHERE state = '1' AND %s " +
                "GROUP BY name, num";

        List<Object> params = new ArrayList<>();
        String dateCondition;

        if (selectedTimePeriod == null) selectedTimePeriod = "按日";

        switch (selectedTimePeriod) {
            case "按年":
                dateCondition = "YEAR(purchase_date) = ?";
                params.add(year);
                break;
            case "按月":
                dateCondition = "YEAR(purchase_date) = ? AND MONTH(purchase_date) = ?";
                params.add(year);
                params.add(month);
                break;
            default:
                String dateStr = String.format("%04d-%02d-%02d", year, month, day);
                dateCondition = "DATE(purchase_date) = ?";
                params.add(dateStr);
                break;
        }

        return new QueryInfo(String.format(baseQuery, dateCondition), params);
    }

    private static class LoadTask implements Runnable {
        private final WeakReference<ExpenseReportActivity> activityRef;
        private final QueryInfo queryInfo;

        LoadTask(ExpenseReportActivity activity, QueryInfo queryInfo) {
            this.activityRef = new WeakReference<>(activity);
            this.queryInfo = queryInfo;
        }

        @Override
        public void run() {
            ExpenseReportActivity activity = activityRef.get();
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

                List<ExpenseRecord> records = new ArrayList<>();
                double totalExpense = 0;
                double totalFreight = 0;

                while (resultSet.next()) {
                    String productName = resultSet.getString("name");
                    String batchNumber = resultSet.getString("num");
                    double expense = resultSet.getDouble("total_expense");
                    double freight = resultSet.getDouble("total_freight");

                    records.add(new ExpenseRecord(productName, batchNumber, expense, freight));
                    totalExpense += expense;
                    totalFreight += freight;

                    Log.d(TAG, "Loaded record: " + productName + " | " + batchNumber
                            + " | Expense: " + expense + " | Freight: " + freight);
                }

                activity.updateUI(records, totalExpense, totalFreight);

            } catch (SQLException e) {
                activity.showError("数据库查询异常");
                Log.e(TAG, "SQL Error: ", e);
            } catch (Exception e) {
                activity.showError("系统错误");
                Log.e(TAG, "General Error: ", e);
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (Exception e) {
                    Log.e(TAG, "Resource close error: ", e);
                }
            }
        }
    }

    private void updateUI(List<ExpenseRecord> records, double totalExpense, double totalFreight) {
        runOnUiThreadIfAlive(() -> {
            // 原有列表更新逻辑
            expenseRecords.clear();
            expenseRecords.addAll(records);
            tvTotalExpense.setText(String.format("总支出: %.2f（元）", totalExpense));
            tvTotalFreight.setText(String.format("总运费: %.2f（元）", totalFreight));
            expenseAdapter.notifyDataSetChanged();

            // 新增图表数据处理
            Map<String, Double> expenseMap = new HashMap<>();
            for (ExpenseRecord record : records) {
                expenseMap.merge(record.getProductName(),
                        record.getTotalExpense() + record.getFreight(),
                        Double::sum);
            }

            // 合并小份额数据
            List<Map.Entry<String, Double>> sorted = new ArrayList<>(expenseMap.entrySet());
            sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            Map<String, Double> finalData = new LinkedHashMap<>();
            if (sorted.size() > 10) {
                double others = 0;
                for (int i = 0; i < 9; i++) {
                    finalData.put(sorted.get(i).getKey(), sorted.get(i).getValue());
                }
                for (int i = 9; i < sorted.size(); i++) {
                    others += sorted.get(i).getValue();
                }
                finalData.put("其他", others);
            } else {
                sorted.forEach(e -> finalData.put(e.getKey(), e.getValue()));
            }

            setupPieChart(finalData, totalExpense + totalFreight);
            setupBarChart(finalData);
            adjustListViewHeight();
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

    // 新增图表配置方法
    private void setupPieChart(Map<String, Double> expenseData, double total) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : expenseData.entrySet()) {
            float percentage = (float) (entry.getValue() / total * 100);
            entries.add(new PieEntry(percentage, entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "支出占比");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.animateY(1000);
    }

    private void setupBarChart(Map<String, Double> expenseData) {
        barChart.getDescription().setEnabled(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Double> entry : expenseData.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue().floatValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "支出金额（元）");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.setData(new BarData(dataSet));
        barChart.animateY(1000);
    }
    private void adjustListViewHeight() {
        // 等待布局完成
        lvExpenseReport.post(() -> {
            ListAdapter adapter = lvExpenseReport.getAdapter();
            if (adapter == null) return;

            int totalHeight = 0;
            // 使用正确的测量规格
            final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    lvExpenseReport.getWidth(), View.MeasureSpec.AT_MOST
            );
            final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    0, View.MeasureSpec.UNSPECIFIED
            );

            // 计算所有子项高度总和
            for (int i = 0; i < adapter.getCount(); i++) {
                View child = adapter.getView(i, null, lvExpenseReport);
                child.measure(widthMeasureSpec, heightMeasureSpec);
                totalHeight += child.getMeasuredHeight();
            }

            // 添加分隔线高度（项数-1）
            totalHeight += lvExpenseReport.getDividerHeight() * (adapter.getCount() - 1);

            // 设置ListView高度
            ViewGroup.LayoutParams params = lvExpenseReport.getLayoutParams();
            params.height = totalHeight;
            lvExpenseReport.setLayoutParams(params);
            lvExpenseReport.requestLayout(); // 请求重新布局
        });
    }
}