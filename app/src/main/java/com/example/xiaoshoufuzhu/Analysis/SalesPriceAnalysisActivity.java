package com.example.xiaoshoufuzhu.Analysis;

import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalesPriceAnalysisActivity extends AppCompatActivity {
    private LineChart lineChart;
    private Spinner spinnerProducts, spinnerTimeRange;
    private TextView tvPrediction;
    private List<Product> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_price_analysis);

        initViews();
        loadProducts();
        setupChart();
        setupListeners();
    }

    private void initViews() {
        lineChart = findViewById(R.id.lineChart);
        spinnerProducts = findViewById(R.id.spinnerProducts);
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        tvPrediction = findViewById(R.id.tvPrediction);
        findViewById(R.id.btnPredict).setOnClickListener(v -> predictPrice());
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        lineChart.getAxisRight().setEnabled(false);
    }

    private void loadProducts() {
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery("SELECT id, name, price FROM products"); // 新增price字段
                while (rs.next()) {
                    productList.add(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getFloat("price") // 读取价格
                    ));
                }

                runOnUiThread(() -> {
                    if (productList.isEmpty()) {
                        Toast.makeText(this, "无可用产品数据", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ArrayAdapter<Product> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, productList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProducts.setAdapter(adapter);
                    spinnerProducts.setSelection(0); // 默认选中第一项
                });

            } catch (Exception e) {
                Log.e("ProductLoad", "产品加载失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "产品数据加载失败", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void setupListeners() {
        spinnerProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Product selectedProduct = (Product) parent.getItemAtPosition(position);
                loadCurrentPrice(selectedProduct.getId()); // 新增：加载当前价格
                loadChartData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadChartData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadChartData() {
        if (spinnerProducts.getSelectedItem() == null) return;

        int productId = ((Product)spinnerProducts.getSelectedItem()).getId();
        String timeRange = (String) spinnerTimeRange.getSelectedItem();

        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection()) {
                // 获取三个数据集
                List<Entry> priceEntries = getPriceData(conn, productId, timeRange);
                List<Entry> wholesaleEntries = getSalesData(conn, productId, timeRange); // 批发销量
                List<Entry> retailEntries = getRetailSalesData(conn, productId, timeRange); // 散户销量

                runOnUiThread(() -> {
                    updateChart(priceEntries, wholesaleEntries, retailEntries);
                    generatePriceSuggestion(priceEntries, wholesaleEntries); // 新增建议生成
                });
            } catch (Exception e) {
                Log.e("ChartData", "数据加载失败", e);
            }
        }).start();
    }

    private List<Entry> getPriceData(Connection conn, int productId, String timeRange) throws SQLException {
        List<Entry> entries = new ArrayList<>();
        // 修改后的 SQL：使用聚合函数 AVG(price)
        String sql = "SELECT DATE(sale_date) as date, AVG(price) as avg_price " +
                "FROM records_customers " +
                "WHERE product_id = ? AND " + getDateCondition(timeRange) +
                " GROUP BY DATE(sale_date) " +
                "ORDER BY DATE(sale_date)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            int index = 0;
            while (rs.next()) {
                float price = rs.getFloat("avg_price"); // 使用聚合后的字段名
                entries.add(new Entry(index++, price));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

    private List<Entry> getSalesData(Connection conn, int productId, String timeRange) throws SQLException {
        List<Entry> entries = new ArrayList<>();
        String sql = "SELECT DATE(sale_date) as date, SUM(quantity) as total_sales " +
                "FROM records_customers " +
                "WHERE product_id = ? AND " + getDateCondition(timeRange) +
                " GROUP BY DATE(sale_date) " +
                "ORDER BY DATE(sale_date)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            int index = 0;
            while (rs.next()) {
                float sales = rs.getFloat("total_sales");
                entries.add(new Entry(index++, sales));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

    private String getDateCondition(String timeRange) {
        Calendar cal = Calendar.getInstance();
        switch (timeRange) {
            case "最近7天":
                cal.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case "最近30天":
                cal.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case "最近3个月":
                cal.add(Calendar.MONTH, -3);
                break;
            default: // 全部
                return "1=1";
        }
        return "sale_date >= '" + new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()) + "'";
    }

    private void updateChart(List<Entry> priceEntries,
                             List<Entry> wholesaleEntries,
                             List<Entry> retailEntries) {
        // 价格趋势（蓝色）
        LineDataSet priceDataSet = new LineDataSet(priceEntries, "价格趋势");
        priceDataSet.setColor(Color.BLUE);
        priceDataSet.setCircleColor(Color.BLUE);

        // 批发销量趋势（红色）
        LineDataSet wholesaleDataSet = new LineDataSet(wholesaleEntries, "批发销量");
        wholesaleDataSet.setColor(Color.RED);
        wholesaleDataSet.setCircleColor(Color.RED);

        // 散户销量趋势（绿色）
        LineDataSet retailDataSet = new LineDataSet(retailEntries, "散户销量");
        retailDataSet.setColor(Color.GREEN);
        retailDataSet.setCircleColor(Color.GREEN);

        // 合并三个数据集
        LineData lineData = new LineData(priceDataSet, wholesaleDataSet, retailDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    // 简单价格预测算法（加权移动平均）
    private void predictPrice() {
        int productId = ((Product)spinnerProducts.getSelectedItem()).getId();
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection()) {
                // 获取最近7天价格
                List<Float> prices = getRecentPrices(conn, productId);
                float predictedPrice = calculatePrediction(prices);

                runOnUiThread(() -> {
                    tvPrediction.setText(String.format("预测明日价格：¥%.2f", predictedPrice));
                });

            } catch (Exception e) {
                Log.e("PricePredict", "Prediction failed", e);
            }
        }).start();
    }

    private List<Float> getRecentPrices(Connection conn, int productId) throws SQLException {
        List<Float> prices = new ArrayList<>();
        String sql = "SELECT price FROM records_customers " +
                "WHERE product_id = ? AND sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "ORDER BY sale_date DESC LIMIT 7";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prices.add(rs.getFloat("price"));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }

        // 如果无数据则取产品基础价格
        if (prices.isEmpty()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT price FROM products WHERE id = " + productId);
                if (rs.next()) prices.add(rs.getFloat("price"));
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return prices;
    }

    private float calculatePrediction(List<Float> prices) {
        if (prices.isEmpty()) return 0f;

        // 简单加权平均（最近3天权重更高）
        float sum = 0;
        float weightSum = 0;
        for (int i = 0; i < prices.size(); i++) {
            float weight = (i < 3) ? 1.5f : 1.0f; // 最近3天权重1.5
            sum += prices.get(i) * weight;
            weightSum += weight;
        }
        return sum / weightSum;
    }

    private List<Entry> getRetailSalesData(Connection conn, int productId, String timeRange) throws SQLException {
        List<Entry> entries = new ArrayList<>();
        String sql = "SELECT DATE(sale_date) as date, SUM(CAST(quantity AS DECIMAL(10,2))) as total_retail " +
                "FROM sales " +
                "WHERE product_id = ? AND " + getDateCondition(timeRange) +
                " GROUP BY DATE(sale_date) " +
                "ORDER BY DATE(sale_date)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            int index = 0;
            while (rs.next()) {
                float sales = rs.getFloat("total_retail");
                entries.add(new Entry(index++, sales));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

    private void loadCurrentPrice(int productId) {
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery("SELECT price FROM products WHERE id = " + productId);
                if (rs.next()) {
                    float currentPrice = rs.getFloat("price");
                    runOnUiThread(() -> {
                        TextView tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
                        tvCurrentPrice.setText(String.format("今日基准价格：¥%.2f", currentPrice));
                    });
                }
            } catch (Exception e) {
                Log.e("CurrentPrice", "获取基准价格失败", e);
            }
        }).start();
    }

    private void generatePriceSuggestion(List<Entry> priceEntries, List<Entry> salesEntries) {
        new Thread(() -> {
            String suggestion = analyzeTrend(priceEntries, salesEntries);
            runOnUiThread(() -> {
                TextView tvSuggestion = findViewById(R.id.tvPriceSuggestion);
                tvSuggestion.setText("价格建议：" + suggestion);
            });
        }).start();
    }

    private String analyzeTrend(List<Entry> priceEntries, List<Entry> salesEntries) {
        if (priceEntries.size() < 3 || salesEntries.size() < 3) {
            return "数据不足，建议维持当前价格";
        }

        // 获取最近3个数据点
        float[] recentPrices = getLastNValues(priceEntries, 3);
        float[] recentSales = getLastNValues(salesEntries, 3);

        // 计算价格趋势
        float priceSlope = calculateSlope(recentPrices);
        float salesSlope = calculateSlope(recentSales);

        // 生成建议
        if (priceSlope > 0.05 && salesSlope > 0) {
            return "价格销量同步上涨，建议小幅上调价格（+5%）";
        } else if (priceSlope < -0.03 && salesSlope < 0) {
            return "价格销量同步下跌，建议降低价格（-5%~-10%）";
        } else if (priceSlope > 0.1 && salesSlope < -0.1) {
            return "价格虚高导致销量下滑，建议降价促销（-8%~-15%）";
        } else if (priceSlope < 0.05 && salesSlope > 0.15) {
            return "需求旺盛价格稳定，建议试探性涨价（+3%~+5%）";
        } else {
            return "市场波动平稳，建议维持当前价格";
        }
    }

    private float[] getLastNValues(List<Entry> entries, int n) {
        int size = Math.min(n, entries.size());
        float[] values = new float[size];
        for (int i = 0; i < size; i++) {
            values[i] = entries.get(entries.size() - 1 - i).getY();
        }
        return values;
    }

    //最小二乘法计算线性回归的斜率
    private float calculateSlope(float[] values) {
        if (values.length < 2) return 0;
        float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < values.length; i++) {
            sumX += i;
            sumY += values[i];
            sumXY += i * values[i];
            sumX2 += i * i;
        }
        return (values.length * sumXY - sumX * sumY) / (values.length * sumX2 - sumX * sumX);
    }


    // 产品数据模型
    private static class Product {
        private final int id;
        private final String name;
        private final float price; // 新增价格字段

        public Product(int id, String name, float price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() { return name; }
        public int getId() { return id; }
        public float getPrice() { return price; }
    }
}