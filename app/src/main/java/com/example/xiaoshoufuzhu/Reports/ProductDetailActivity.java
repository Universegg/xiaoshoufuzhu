package com.example.xiaoshoufuzhu.Reports;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvProductName;
    private TextView tvProductNum;
    private TextView tvCustomerQuantity;
    private TextView tvSalesQuantity;
    private TextView tvCustomerTotalPrice;
    private TextView tvSalesTotalPrice;
    private TextView tvReceivableAmount;
    private TextView tvTotalReceivable;
    private TextView tvTotalIncome;
    private Button btnReceivableInfo;
    private Button btnCustomerDetails;
    private LinearLayout llReceivableDetails;
    private LinearLayout llCustomerDetails;
    private ScrollView svCustomerDetails;
    private ScrollView svReceivableDetails; // 新增的ScrollView

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        tvProductName = findViewById(R.id.tvProductName);
        tvProductNum = findViewById(R.id.tvProductNum);
        tvCustomerQuantity = findViewById(R.id.tvCustomerQuantity);
        tvSalesQuantity = findViewById(R.id.tvSalesQuantity);
        tvCustomerTotalPrice = findViewById(R.id.tvCustomerTotalPrice);
        tvSalesTotalPrice = findViewById(R.id.tvSalesTotalPrice);
        tvReceivableAmount = findViewById(R.id.tvReceivableAmount);
        tvTotalReceivable = findViewById(R.id.tvTotalReceivable);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        btnReceivableInfo = findViewById(R.id.btnReceivableInfo);
        btnCustomerDetails = findViewById(R.id.btnCustomerDetails);
        llReceivableDetails = findViewById(R.id.llReceivableDetails);
        llCustomerDetails = findViewById(R.id.llCustomerDetails);
        svCustomerDetails = findViewById(R.id.svCustomerDetails);
        svReceivableDetails = findViewById(R.id.svReceivableDetails);

        // 获取传递过来的产品名称和编号
        String productName = getIntent().getStringExtra("productName");
        String productNum = getIntent().getStringExtra("productNum");

        // 设置 TextView 的内容
        tvProductName.setText(productName);
        tvProductNum.setText(productNum);

        // 加载产品的其他详细信息
        loadProductDetails(productName, productNum);

        btnReceivableInfo.setOnClickListener(v -> toggleReceivableDetails(productName, productNum));
        btnCustomerDetails.setOnClickListener(v -> toggleCustomerDetails(productName, productNum));
    }

    private void loadProductDetails(String productName, String productNum) {
        new Thread(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                connection = DatabaseHelper.getConnection();
                if (connection == null) {
                    showError("数据库连接失败");
                    return;
                }

                String query = "SELECT " +
                        "p.name AS product_name, p.num AS product_num, p.price AS product_price, " +
                        "rc.id AS rc_id, s.id AS s_id, " +
                        "SUM(CASE WHEN rc.state = '结清' THEN rc.quantity ELSE 0 END) AS customer_quantity, " +
                        "SUM(CASE WHEN rc.state = '结清' THEN rc.total_price ELSE 0 END) AS customer_total_price, " +
                        "SUM(CASE WHEN rc.state = '赊账' THEN rc.total_price ELSE 0 END) AS receivable_amount, " +
                        "SUM(s.quantity) AS sales_quantity, " +
                        "SUM(s.total_price) AS sales_total_price, " +
                        "SUM(rc.quantity + s.quantity) * p.price AS total_receivable, " +
                        "SUM(CASE WHEN rc.state = '结清' THEN rc.total_price ELSE 0 END) + SUM(s.total_price) AS total_income " +
                        "FROM products p " +
                        "LEFT JOIN records_customers rc ON p.id = rc.product_id " +
                        "LEFT JOIN sales s ON p.id = s.product_id " +
                        "WHERE p.name = ? AND p.num = ? " +
                        "GROUP BY p.name, p.num, p.price, rc.id, s.id";

                statement = connection.prepareStatement(query);
                statement.setString(1, productName);
                statement.setString(2, productNum);

                resultSet = statement.executeQuery();

                double customerQuantity = 0;
                double customerTotalPrice = 0;
                double receivableAmount = 0;
                double salesQuantity = 0;
                double salesTotalPrice = 0;
                double totalReceivable = 0;
                double totalIncome = 0;
                double productPrice = 0;

                Set<Integer> processedRcIds = new HashSet<>();
                Set<Integer> processedSIds = new HashSet<>();

                while (resultSet.next()) {
                    int rcId = resultSet.getInt("rc_id");
                    int sId = resultSet.getInt("s_id");
                    productPrice = resultSet.getDouble("product_price");

                    if (!processedRcIds.contains(rcId)) {
                        customerQuantity += resultSet.getDouble("customer_quantity");
                        customerTotalPrice += resultSet.getDouble("customer_total_price");
                        receivableAmount += resultSet.getDouble("receivable_amount");
                        totalReceivable += resultSet.getDouble("total_receivable");
                        totalIncome += resultSet.getDouble("customer_total_price");
                        processedRcIds.add(rcId);
                    }

                    if (!processedSIds.contains(sId)) {
                        salesQuantity += resultSet.getDouble("sales_quantity");
                        salesTotalPrice += resultSet.getDouble("sales_total_price");
                        totalIncome += resultSet.getDouble("sales_total_price");
                        processedSIds.add(sId);
                    }

                    // Log the details
                    Log.i("ProductDetailActivity", "Processed record: rc_id=" + rcId + ", s_id=" + sId + ", customer_quantity=" + customerQuantity + ", customer_total_price=" + customerTotalPrice + ", receivable_amount=" + receivableAmount + ", sales_quantity=" + salesQuantity + ", sales_total_price=" + salesTotalPrice + ", total_receivable=" + totalReceivable + ", total_income=" + totalIncome);
                }

                double finalCustomerQuantity = customerQuantity;
                double finalCustomerTotalPrice = customerTotalPrice;
                double finalSalesQuantity = salesQuantity;
                double finalSalesTotalPrice = salesTotalPrice;
                double finalTotalReceivable = receivableAmount + totalIncome; // Change here
                double finalTotalIncome = totalIncome;

                double finalReceivableAmount = receivableAmount;
                runOnUiThread(() -> {
                    tvCustomerQuantity.setText(String.format("采购商销售总数: %.2f（斤）", finalCustomerQuantity));
                    tvCustomerTotalPrice.setText(String.format("采购商销售总金额: %.2f（元）", finalCustomerTotalPrice));
                    tvReceivableAmount.setText(String.format("赊账金额: %.2f（元）", finalReceivableAmount));
                    tvSalesQuantity.setText(String.format("散户销售总数: %.2f（斤）", finalSalesQuantity));
                    tvSalesTotalPrice.setText(String.format("散户销售总金额: %.2f（元）", finalSalesTotalPrice));
                    tvTotalReceivable.setText(String.format("应收金额: %.2f（元）", finalTotalReceivable));
                    tvTotalIncome.setText(String.format("实收金额: %.2f（元）", finalTotalIncome));
                });

            } catch (SQLException e) {
                showError("数据库查询异常");
                e.printStackTrace();
            } catch (Exception e) {
                showError("系统错误");
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
        }).start();
    }

    private void toggleReceivableDetails(String productName, String productNum) {
        if (svReceivableDetails.getVisibility() == View.VISIBLE) {
            svReceivableDetails.setVisibility(View.GONE);
        } else {
            loadReceivableDetails(productName, productNum);
            svReceivableDetails.setVisibility(View.VISIBLE);
        }
    }

    private void toggleCustomerDetails(String productName, String productNum) {
        if (svCustomerDetails.getVisibility() == View.VISIBLE) {
            svCustomerDetails.setVisibility(View.GONE);
        } else {
            loadCustomerDetails(productName, productNum);
            svCustomerDetails.setVisibility(View.VISIBLE);
        }
    }

    private void loadReceivableDetails(String productName, String productNum) {
        new Thread(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                connection = DatabaseHelper.getConnection();
                if (connection == null) {
                    showError("数据库连接失败");
                    return;
                }

                String query = "SELECT c.name AS customer_name, rc.quantity AS quantity, rc.total_price AS receivable_amount " +
                        "FROM records_customers rc " +
                        "LEFT JOIN customers c ON rc.cid = c.id " +
                        "LEFT JOIN products p ON rc.product_id = p.id " +
                        "WHERE p.name = ? AND p.num = ? AND rc.state = '赊账'";

                statement = connection.prepareStatement(query);
                statement.setString(1, productName);
                statement.setString(2, productNum);

                resultSet = statement.executeQuery();

                List<ReceivableDetail> receivableDetails = new ArrayList<>();

                while (resultSet.next()) {
                    String customerName = resultSet.getString("customer_name");
                    int quantity = resultSet.getInt("quantity");
                    double receivableAmount = resultSet.getDouble("receivable_amount");

                    receivableDetails.add(new ReceivableDetail(customerName, quantity, receivableAmount));
                }

                runOnUiThread(() -> populateReceivableDetails(receivableDetails));

            } catch (SQLException e) {
                showError("数据库查询异常");
                e.printStackTrace();
            } catch (Exception e) {
                showError("系统错误");
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
        }).start();
    }

    private void loadCustomerDetails(String productName, String productNum) {
        new Thread(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                connection = DatabaseHelper.getConnection();
                if (connection == null) {
                    showError("数据库连接失败");
                    return;
                }

                String query = "SELECT c.name AS customer_name, rc.quantity AS quantity, rc.total_price AS amount " +
                        "FROM records_customers rc " +
                        "LEFT JOIN customers c ON rc.cid = c.id " +
                        "LEFT JOIN products p ON rc.product_id = p.id " +
                        "WHERE p.name = ? AND p.num = ? AND rc.state = '结清'";

                statement = connection.prepareStatement(query);
                statement.setString(1, productName);
                statement.setString(2, productNum);

                resultSet = statement.executeQuery();

                List<CustomerDetail> customerDetails = new ArrayList<>();

                while (resultSet.next()) {
                    String customerName = resultSet.getString("customer_name");
                    int quantity = resultSet.getInt("quantity");
                    double amount = resultSet.getDouble("amount");

                    customerDetails.add(new CustomerDetail(customerName, quantity, amount));
                }

                runOnUiThread(() -> populateCustomerDetails(customerDetails));

            } catch (SQLException e) {
                showError("数据库查询异常");
                e.printStackTrace();
            } catch (Exception e) {
                showError("系统错误");
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
        }).start();
    }

    private void populateReceivableDetails(List<ReceivableDetail> receivableDetails) {
        llReceivableDetails.removeAllViews();
        for (ReceivableDetail detail : receivableDetails) {
            TextView tvReceivableDetail = new TextView(this);
            tvReceivableDetail.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvReceivableDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvReceivableDetail.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
            tvReceivableDetail.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
            tvReceivableDetail.setBackgroundResource(R.drawable.bg_detail_item);
            tvReceivableDetail.setText(String.format("▫ %s\n   购买数量：%d斤\n   赊账金额：¥%.2f", detail.customerName, detail.quantity, detail.receivableAmount));
            llReceivableDetails.addView(tvReceivableDetail);
        }
    }

    private void populateCustomerDetails(List<CustomerDetail> customerDetails) {
        llCustomerDetails.removeAllViews();
        for (CustomerDetail detail : customerDetails) {
            TextView tvCustomerDetail = new TextView(this);
            tvCustomerDetail.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvCustomerDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvCustomerDetail.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
            tvCustomerDetail.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
            tvCustomerDetail.setBackgroundResource(R.drawable.bg_detail_item);
            tvCustomerDetail.setText(String.format("▫ %s\n   购买数量：%d斤\n   总金额：¥%.2f", detail.customerName, detail.quantity, detail.amount));
            llCustomerDetails.addView(tvCustomerDetail);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void showError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private static class ReceivableDetail {
        String customerName;
        int quantity;
        double receivableAmount;

        ReceivableDetail(String customerName, int quantity, double receivableAmount) {
            this.customerName = customerName;
            this.quantity = quantity;
            this.receivableAmount = receivableAmount;
        }
    }

    private static class CustomerDetail {
        String customerName;
        int quantity;
        double amount;

        CustomerDetail(String customerName, int quantity, double amount) {
            this.customerName = customerName;
            this.quantity = quantity;
            this.amount = amount;
        }
    }
}