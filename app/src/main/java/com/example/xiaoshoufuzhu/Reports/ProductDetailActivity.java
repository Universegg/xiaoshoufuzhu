package com.example.xiaoshoufuzhu.Reports;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
                        "p.name AS product_name, p.num AS product_num, " +
                        "rc.id AS rc_id, s.id AS s_id, " +
                        "SUM(CASE WHEN rc.state = '结清' THEN rc.quantity ELSE 0 END) AS customer_quantity, " +
                        "SUM(CASE WHEN rc.state = '结清' THEN rc.total_price ELSE 0 END) AS customer_total_price, " +
                        "SUM(CASE WHEN rc.state = '赊账' THEN rc.total_price ELSE 0 END) AS receivable_amount, " +
                        "SUM(s.quantity) AS sales_quantity, " +
                        "SUM(s.total_price) AS sales_total_price, " +
                        "SUM(CASE WHEN rc.state = '赊账' THEN rc.total_price ELSE 0 END) AS total_receivable, " +
                        "SUM(CASE WHEN rc.state = '结清' THEN rc.total_price ELSE 0 END) + SUM(s.total_price) AS total_income " +
                        "FROM products p " +
                        "LEFT JOIN records_customers rc ON p.id = rc.product_id " +
                        "LEFT JOIN sales s ON p.id = s.product_id " +
                        "WHERE p.name = ? AND p.num = ? " +
                        "GROUP BY p.name, p.num, rc.id, s.id";

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

                Set<Integer> processedRcIds = new HashSet<>();
                Set<Integer> processedSIds = new HashSet<>();

                while (resultSet.next()) {
                    int rcId = resultSet.getInt("rc_id");
                    int sId = resultSet.getInt("s_id");

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
                double finalReceivableAmount = receivableAmount;
                double finalSalesQuantity = salesQuantity;
                double finalSalesTotalPrice = salesTotalPrice;
                double finalTotalReceivable = totalReceivable;
                double finalTotalIncome = totalIncome;

                runOnUiThread(() -> {
                    tvCustomerQuantity.setText(String.format("采购商购买数量: %.2f", finalCustomerQuantity));
                    tvCustomerTotalPrice.setText(String.format("采购商购买总金额: %.2f", finalCustomerTotalPrice));
                    tvReceivableAmount.setText(String.format("赊账金额: %.2f", finalReceivableAmount));
                    tvSalesQuantity.setText(String.format("散户购买数量: %.2f", finalSalesQuantity));
                    tvSalesTotalPrice.setText(String.format("散户购买总金额: %.2f", finalSalesTotalPrice));
                    tvTotalReceivable.setText(String.format("应收金额: %.2f", finalTotalReceivable));
                    tvTotalIncome.setText(String.format("实收金额: %.2f", finalTotalIncome));
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
        if (llReceivableDetails.getVisibility() == View.VISIBLE) {
            llReceivableDetails.setVisibility(View.GONE);
        } else {
            loadReceivableDetails(productName, productNum);
            llReceivableDetails.setVisibility(View.VISIBLE);
        }
    }

    private void toggleCustomerDetails(String productName, String productNum) {
        if (llCustomerDetails.getVisibility() == View.VISIBLE) {
            llCustomerDetails.setVisibility(View.GONE);
        } else {
            loadCustomerDetails(productName, productNum);
            llCustomerDetails.setVisibility(View.VISIBLE);
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

                String query = "SELECT c.name AS customer_name, rc.total_price AS receivable_amount " +
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
                    double receivableAmount = resultSet.getDouble("receivable_amount");

                    receivableDetails.add(new ReceivableDetail(customerName, receivableAmount));
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
            tvReceivableDetail.setText(String.format("%s: %.2f", detail.customerName, detail.receivableAmount));
            llReceivableDetails.addView(tvReceivableDetail);
        }
    }

    private void populateCustomerDetails(List<CustomerDetail> customerDetails) {
        llCustomerDetails.removeAllViews();
        for (CustomerDetail detail : customerDetails) {
            TextView tvCustomerDetail = new TextView(this);
            tvCustomerDetail.setText(String.format("%s: 购买数量: %d, 购买金额: %.2f", detail.customerName, detail.quantity, detail.amount));
            llCustomerDetails.addView(tvCustomerDetail);
        }
    }

    private void showError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private static class ReceivableDetail {
        String customerName;
        double receivableAmount;

        ReceivableDetail(String customerName, double receivableAmount) {
            this.customerName = customerName;
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