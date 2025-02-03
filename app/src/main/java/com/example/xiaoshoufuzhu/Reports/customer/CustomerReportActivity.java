package com.example.xiaoshoufuzhu.Reports.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class CustomerReportActivity extends AppCompatActivity {

    private TextView tvTotalAmount;
    private TextView tvCustomerInfo;
    private ListView lvCustomerReport;
    private DatePicker datePicker;
    private Spinner spinnerTimePeriod;
    private Spinner spinnerCustomer;
    private CustomerReportAdapter customerReportAdapter;
    private List<CustomerRecord> customerRecordList;
    private List<String> customerNameList;
    private List<Integer> customerIdList;
    private String selectedTimePeriod;
    private int selectedCustomerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_report);

        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);
        lvCustomerReport = findViewById(R.id.lvCustomerReport);
        datePicker = findViewById(R.id.datePicker);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);
        spinnerCustomer = findViewById(R.id.spinner_customer);

        customerRecordList = new ArrayList<>();
        customerReportAdapter = new CustomerReportAdapter(this, customerRecordList);
        lvCustomerReport.setAdapter(customerReportAdapter);

        customerNameList = new ArrayList<>();
        customerIdList = new ArrayList<>();

        // 设置 Spinner 的监听器
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = (String) parent.getItemAtPosition(position);
                loadCustomerReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimePeriod = "按日";
                loadCustomerReport();
            }
        });

        spinnerCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCustomerId = customerIdList.get(position);
                loadCustomerReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCustomerId = -1;
                loadCustomerReport();
            }
        });

        // 设置日期选择器的监听器
        datePicker.init(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> loadCustomerReport()
        );

        // 初次加载报表数据
        loadCustomerList();
    }

    private void loadCustomerList() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT id, name FROM customers";
                    PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();

                    customerNameList.clear();
                    customerIdList.clear();
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        customerIdList.add(id);
                        customerNameList.add(name);
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CustomerReportActivity.this, android.R.layout.simple_spinner_item, customerNameList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCustomer.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(CustomerReportActivity.this, "加载客户列表失败", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(CustomerReportActivity.this, "数据库连接失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadCustomerReport() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    loadCustomerInfo(connection);
                    loadCustomerRecords(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(CustomerReportActivity.this, "加载客户报表失败", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(CustomerReportActivity.this, "数据库连接失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadCustomerInfo(Connection connection) throws Exception {
        String query = "SELECT name, phone, address, amount FROM customers WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, selectedCustomerId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            String customerInfo = String.format("姓名: %s\n电话: %s\n地址: %s\n赊账: %.2f",
                    resultSet.getString("name"),
                    resultSet.getString("phone"),
                    resultSet.getString("address"),
                    resultSet.getDouble("amount"));
            runOnUiThread(() -> tvCustomerInfo.setText("客户信息: \n" + customerInfo));
        }
    }

    private void loadCustomerRecords(Connection connection) throws Exception {
        String query = "";
        String dateParam = "";

        if ("按年".equals(selectedTimePeriod)) {
            query =
                    "SELECT products.name AS product_name, records_customers.quantity, records_customers.total_price " +
                            "FROM records_customers " +
                            "JOIN products ON records_customers.product_id = products.id " +
                            "JOIN t_customers_records ON records_customers.id = t_customers_records.rid " +
                            "WHERE t_customers_records.cid = ? AND YEAR(records_customers.sale_date) = ?";
            dateParam = String.format("%04d", datePicker.getYear());
        } else if ("按月".equals(selectedTimePeriod)) {
            query =
                    "SELECT products.name AS product_name, records_customers.quantity, records_customers.total_price " +
                            "FROM records_customers " +
                            "JOIN products ON records_customers.product_id = products.id " +
                            "JOIN t_customers_records ON records_customers.id = t_customers_records.rid " +
                            "WHERE t_customers_records.cid = ? AND YEAR(records_customers.sale_date) = ? AND MONTH(records_customers.sale_date) = ?";
            dateParam = String.format("%04d", datePicker.getYear());
        } else if ("按日".equals(selectedTimePeriod)) {
            query =
                    "SELECT products.name AS product_name, records_customers.quantity, records_customers.total_price " +
                            "FROM records_customers " +
                            "JOIN products ON records_customers.product_id = products.id " +
                            "JOIN t_customers_records ON records_customers.id = t_customers_records.rid " +
                            "WHERE t_customers_records.cid = ? AND DATE(records_customers.sale_date) = ?";
            dateParam = String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
        }

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, selectedCustomerId);
        statement.setString(2, dateParam);
        if ("按月".equals(selectedTimePeriod)) {
            statement.setString(3, String.format("%02d", datePicker.getMonth() + 1));
        }
        ResultSet resultSet = statement.executeQuery();

        double totalAmount = 0;
        List<CustomerRecord> tempCustomerRecordList = new ArrayList<>();
        while (resultSet.next()) {
            String productName = resultSet.getString("product_name");
            int quantity = resultSet.getInt("quantity");
            double totalPrice = resultSet.getDouble("total_price");
            totalAmount += totalPrice;

            tempCustomerRecordList.add(new CustomerRecord(productName, quantity, totalPrice));
        }

        double finalTotalAmount = totalAmount;
        runOnUiThread(() -> {
            customerRecordList.clear();
            customerRecordList.addAll(tempCustomerRecordList);
            tvTotalAmount.setText("总销售金额: " + finalTotalAmount);
            customerReportAdapter.notifyDataSetChanged();
        });
    }
}