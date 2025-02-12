package com.example.xiaoshoufuzhu.Reports.customer;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.lang.ref.WeakReference;

public class CustomerReportActivity extends AppCompatActivity {

    // 控件声明
    private TextView tvTotalAmount;
    private TextView tvCustomerInfo;
    private ListView lvCustomerReport;
    private Spinner spinnerTimePeriod;
    private Spinner spinnerCustomer;
    private ImageView ivDatePicker;

    // 数据相关
    private CustomerReportAdapter customerReportAdapter;
    private List<CustomerRecord> customerRecordList;
    private List<String> customerNameList;
    private List<Integer> customerIdList;
    private String selectedTimePeriod;
    private int selectedCustomerId;
    private Calendar selectedDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_report);

        // 初始化视图
        initViews();

        // 初始化数据
        customerRecordList = new ArrayList<>();
        customerNameList = new ArrayList<>();
        customerIdList = new ArrayList<>();

        // 设置适配器
        customerReportAdapter = new CustomerReportAdapter(this, customerRecordList);
        lvCustomerReport.setAdapter(customerReportAdapter);

        // 设置监听器
        setupListeners();

        // 加载初始数据
        loadCustomerList();
    }

    private void initViews() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);
        lvCustomerReport = findViewById(R.id.lvCustomerReport);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);
        spinnerCustomer = findViewById(R.id.spinner_customer);
        ivDatePicker = findViewById(R.id.ivDatePicker);

        // 初始化日期
        selectedDate = Calendar.getInstance();
    }

    private void setupListeners() {
        // 时间段选择监听
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = parent.getItemAtPosition(position).toString();
                loadCustomerReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimePeriod = "按日";
                loadCustomerReport();
            }
        });

        // 采购商选择监听
        spinnerCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < customerIdList.size()) {
                    selectedCustomerId = customerIdList.get(position);
                    loadCustomerReport();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCustomerId = -1;
                loadCustomerReport();
            }
        });

        // 日期选择监听
        ivDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    loadCustomerReport();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadCustomerList() {
        new Thread(new CustomerLoaderTask(this)).start();
    }

    private void loadCustomerReport() {
        if (selectedCustomerId == -1) return;
        new Thread(new ReportLoaderTask(this)).start();
    }

    /* 内部任务类 */
    private static class CustomerLoaderTask implements Runnable {
        private final WeakReference<CustomerReportActivity> activityRef;

        CustomerLoaderTask(CustomerReportActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            CustomerReportActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;

            try (Connection connection = DatabaseHelper.getConnection()) {
                if (connection == null) {
                    activity.showToast("数据库连接失败");
                    return;
                }

                PreparedStatement stmt = connection.prepareStatement(
                        "SELECT id, name FROM customers ORDER BY name ASC");
                ResultSet rs = stmt.executeQuery();

                activity.customerIdList.clear();
                activity.customerNameList.clear();
                while (rs.next()) {
                    activity.customerIdList.add(rs.getInt("id"));
                    activity.customerNameList.add(rs.getString("name"));
                }

                activity.runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            activity,
                            android.R.layout.simple_spinner_item,
                            activity.customerNameList
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    activity.spinnerCustomer.setAdapter(adapter);

                    if (!activity.customerNameList.isEmpty()) {
                        activity.spinnerCustomer.setSelection(0);
                    }
                });

            } catch (Exception e) {
                activity.showToast("加载客户列表失败");
                e.printStackTrace();
            }
        }
    }

    private static class ReportLoaderTask implements Runnable {
        private final WeakReference<CustomerReportActivity> activityRef;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        ReportLoaderTask(CustomerReportActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            CustomerReportActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;

            try (Connection connection = DatabaseHelper.getConnection()) {
                if (connection == null) {
                    activity.showToast("数据库连接失败");
                    return;
                }

                // 加载客户信息
                loadCustomerInfo(connection, activity);

                // 加载销售记录
                loadSalesRecords(connection, activity);

            } catch (Exception e) {
                activity.showToast("加载报表失败");
                e.printStackTrace();
            }
        }

        private void loadCustomerInfo(Connection connection, CustomerReportActivity activity) throws Exception {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT name, phone, address, amount FROM customers WHERE id = ?")) {

                stmt.setInt(1, activity.selectedCustomerId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String info = String.format(
                            "姓名: %s\n电话: %s\n地址: %s\n赊账: ¥%.2f",
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getDouble("amount")
                    );
                    activity.runOnUiThread(() ->
                            activity.tvCustomerInfo.setText("客户信息:\n" + info));
                }
            }
        }

        private void loadSalesRecords(Connection connection, CustomerReportActivity activity) throws Exception {
            String query = buildQuery(activity);
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                bindQueryParameters(stmt, activity);

                ResultSet rs = stmt.executeQuery();
                List<CustomerRecord> records = new ArrayList<>();
                double total = 0;

                while (rs.next()) {
                    CustomerRecord record = new CustomerRecord(
                            rs.getString("product_name"),
                            rs.getString("batch_num"),
                            rs.getDouble("price"),
                            rs.getDouble("quantity"),
                            formatDate(rs.getString("sale_date")),
                            rs.getString("state"),
                            rs.getDouble("total_price") // 新增总价字段
                    );
                    records.add(record);
                    total += record.getTotalPrice();
                }

                double finalTotal = total;
                activity.runOnUiThread(() -> updateUI(activity, records, finalTotal));
            }
        }

        private String formatDate(String dateTime) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
                return dateFormat.format(date);
            } catch (ParseException e) {
                return "日期格式错误";
            }
        }

        private String buildQuery(CustomerReportActivity activity) {
            return "SELECT " +
                    "products.name AS product_name, " +
                    "products.num AS batch_num, " +
                    "records_customers.price, " +
                    "records_customers.quantity, " +
                    "records_customers.sale_date, " +
                    "records_customers.state, " +
                    "records_customers.total_price " + // 包含总价字段
                    "FROM records_customers " +
                    "JOIN products ON records_customers.product_id = products.id " +
                    "JOIN t_customers_records ON records_customers.id = t_customers_records.rid " +
                    "WHERE t_customers_records.cid = ? AND " + getDateCondition(activity);
        }

        private String getDateCondition(CustomerReportActivity activity) {
            switch (activity.selectedTimePeriod) {
                case "按年": return "YEAR(records_customers.sale_date) = ?";
                case "按月": return "YEAR(records_customers.sale_date) = ? AND MONTH(records_customers.sale_date) = ?";
                default: return "DATE(records_customers.sale_date) = ?";
            }
        }

        private void bindQueryParameters(PreparedStatement stmt, CustomerReportActivity activity) throws Exception {
            int paramIndex = 1;
            stmt.setInt(paramIndex++, activity.selectedCustomerId);

            Calendar cal = activity.selectedDate;
            switch (activity.selectedTimePeriod) {
                case "按年":
                    stmt.setInt(paramIndex, cal.get(Calendar.YEAR));
                    break;
                case "按月":
                    stmt.setInt(paramIndex++, cal.get(Calendar.YEAR));
                    stmt.setInt(paramIndex, cal.get(Calendar.MONTH) + 1);
                    break;
                default:
                    String dateStr = String.format("%04d-%02d-%02d",
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH));
                    stmt.setString(paramIndex, dateStr);
            }
        }

        private void updateUI(CustomerReportActivity activity, List<CustomerRecord> records, double total) {
            activity.customerRecordList.clear();
            activity.customerRecordList.addAll(records);
            activity.tvTotalAmount.setText(String.format("总销售金额: ¥%.2f", total));
            activity.customerReportAdapter.notifyDataSetChanged();
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

}