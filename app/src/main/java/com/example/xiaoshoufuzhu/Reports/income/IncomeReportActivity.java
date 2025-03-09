package com.example.xiaoshoufuzhu.Reports.income;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


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
    private PieChart pieChart;
    private BarChart barChart;

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
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
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
            adjustListViewHeight(); // 新增高度调整

            // 新增饼图数据处理逻辑
            Map<String, Double> productIncomes = new LinkedHashMap<>();
            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>();

            // 1. 汇总各产品总收入
            for (IncomeRecord record : incomeRecordList) {
                productIncomes.merge(record.getProductName(),
                        record.getTotalIncome(),
                        Double::sum);
            }

            // 2. 过滤零值并按收入排序
            sortedEntries = productIncomes.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .collect(Collectors.toList());

            // 3. 合并逻辑（当产品数>10时）
            Map<String, Double> finalData = new LinkedHashMap<>();
            if (sortedEntries.size() > 10) {
                // 取前9个主要产品
                for (int i = 0; i < 9; i++) {
                    Map.Entry<String, Double> entry = sortedEntries.get(i);
                    finalData.put(entry.getKey(), entry.getValue());
                }

                // 合并剩余为"其他"
                double othersTotal = sortedEntries.subList(9, sortedEntries.size())
                        .stream()
                        .mapToDouble(Map.Entry::getValue)
                        .sum();

                if (othersTotal > 0) {
                    finalData.put("其他", othersTotal);
                }
            } else {
                sortedEntries.forEach(entry -> finalData.put(entry.getKey(), entry.getValue()));
            }

            // 4. 传递处理后的数据给饼图
            setupPieChart(finalData, totalIncome);
            setupBarChart(finalData);
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
    private void setupPieChart(Map<String, Double> incomeData, double total) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : incomeData.entrySet()) {
            float percentage = (float) (entry.getValue() / total * 100);
            entries.add(new PieEntry(percentage, entry.getKey()));
        }

        // 修正后的颜色初始化
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        colors.add(Color.parseColor("#9E9E9E"));

        PieDataSet dataSet = new PieDataSet(entries, "产品收入占比");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(pieData);

        pieChart.animateY(1000);
        pieChart.invalidate();
        pieChart.getLegend().setWordWrapEnabled(true); // 图例换行
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setDrawInside(false);
        pieChart.getLegend().setXEntrySpace(7f);
        pieChart.getLegend().setYEntrySpace(0f);
        pieChart.getLegend().setYOffset(10f);

        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(10f);
    }

    private void setupBarChart(Map<String, Double> incomeData) {
        // 基础配置
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setPinchZoom(true);
        barChart.setDoubleTapToZoomEnabled(false);
        int barWidthDp = 100;
        int totalWidthDp = (int) (incomeData.size() * barWidthDp * 1.2); // 含间距
        barChart.getLayoutParams().width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                totalWidthDp,
                getResources().getDisplayMetrics()
        );

        // X轴配置
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(incomeData.size(), false); // 禁用强制数量
        xAxis.setCenterAxisLabels(true); // 标签居中显示
        xAxis.setAxisMinimum(-0.5f); // 左边界偏移
        xAxis.setAxisMaximum(incomeData.size() - 0.5f); // 右边界偏移
        xAxis.setValueFormatter(new IndexAxisValueFormatter(incomeData.keySet().toArray(new String[0])));

        // Y轴配置
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(100f);
        barChart.getAxisRight().setEnabled(false);

        // 数据准备
        ArrayList<BarEntry> entries = new ArrayList<>();
        List<Double> values = new ArrayList<>(incomeData.values());
        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i + 0.5f, values.get(i).floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "产品收入金额（元）");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new LargeValueFormatter());

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);
        barChart.setData(barData);

        // 动画和刷新
        barChart.animateY(1000);
        barChart.invalidate();
        // 允许水平拖动
        barChart.setDragEnabled(true);
        barChart.setScaleXEnabled(true);
        barChart.setVisibleXRangeMaximum(5); // 默认显示5个柱子
    }

    // 大数字格式化显示
    private static class LargeValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format(Locale.CHINA, "%.0f", value);
        }
    }

    // 解决X轴标签显示问题
    private static class IndexAxisValueFormatter extends ValueFormatter {
        private final String[] mLabels;

        public IndexAxisValueFormatter(String[] labels) {
            mLabels = labels;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index < 0 || index >= mLabels.length) {
                return "";
            }
            return mLabels[index];
        }
    }

    private void adjustListViewHeight() {
        // 等待布局完成
        lvIncomeReport.post(() -> {
            ListAdapter adapter = lvIncomeReport.getAdapter();
            if (adapter == null) return;

            int totalHeight = 0;
            final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    lvIncomeReport.getWidth(), View.MeasureSpec.AT_MOST
            );
            final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    0, View.MeasureSpec.UNSPECIFIED
            );

            // 计算所有子项高度总和
            for (int i = 0; i < adapter.getCount(); i++) {
                View child = adapter.getView(i, null, lvIncomeReport);
                child.measure(widthMeasureSpec, heightMeasureSpec);
                totalHeight += child.getMeasuredHeight();
            }

            // 添加分隔线高度
            totalHeight += lvIncomeReport.getDividerHeight() * (adapter.getCount() - 1);

            // 设置ListView高度
            ViewGroup.LayoutParams params = lvIncomeReport.getLayoutParams();
            params.height = totalHeight;
            lvIncomeReport.setLayoutParams(params);
            lvIncomeReport.requestLayout();
        });
    }
}