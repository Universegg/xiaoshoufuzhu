package com.example.xiaoshoufuzhu.Reports.ProductLoss;

import java.util.List;
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

public class ProductLossReportActivity extends AppCompatActivity {
    private TextView tvTotalLoss;
    private ListView lvLossReport;
    private ImageView ivDatePicker;
    private Spinner spinnerTimePeriod;
    private ProductLossReportAdapter adapter;
    private List<ProductLossRecord> ProductlossRecords;
    String selectedTimePeriod;
    Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_loss_report);

        initViews();
        setupAdapters();
        setupListeners();
        loadLossReport();
    }

    private void initViews() {
        tvTotalLoss = findViewById(R.id.tvTotalLoss);
        lvLossReport = findViewById(R.id.lvLossReport);
        ivDatePicker = findViewById(R.id.ivDatePicker);
        spinnerTimePeriod = findViewById(R.id.spinner_time_period);
    }

    private void setupAdapters() {
        ProductlossRecords = new ArrayList<>();
        adapter = new ProductLossReportAdapter(this, ProductlossRecords);
        lvLossReport.setAdapter(adapter);
    }

    private void setupListeners() {
        selectedDate = Calendar.getInstance();

        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = (String) parent.getItemAtPosition(position);
                loadLossReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimePeriod = "按日";
                loadLossReport();
            }
        });

        ivDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate.set(year, month, day);
            loadLossReport();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadLossReport() {
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection()) {
                String sql = "SELECT p.name, p.num, " +
                        "SUM(l.quantity) AS total_quantity, " +
                        "SUM(l.loss_amount) AS total_amount " +
                        "FROM losses l " +
                        "JOIN products p ON l.product_id = p.id " +
                        "WHERE " + getDateCondition() + " " +
                        "GROUP BY p.name, p.num";

                PreparedStatement stmt = conn.prepareStatement(sql);
                setDateParameters(stmt);

                ResultSet rs = stmt.executeQuery();
                List<ProductLossRecord> records = new ArrayList<>();
                double totalLoss = 0;

                while (rs.next()) {
                    String name = rs.getString("name");
                    String num = rs.getString("num");
                    double quantity = rs.getDouble("total_quantity");
                    double amount = rs.getDouble("total_amount");

                    records.add(new ProductLossRecord(name, num, quantity, amount));
                    totalLoss += amount;
                }

                double finalTotalLoss = totalLoss;
                runOnUiThread(() -> {
                    ProductlossRecords.clear();
                    ProductlossRecords.addAll(records);
                    tvTotalLoss.setText(String.format("总损耗金额: ¥%.1f", finalTotalLoss));
                    adapter.notifyDataSetChanged();
                });

            } catch (SQLException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String getDateCondition() {
        if (selectedTimePeriod == null) {
            selectedTimePeriod = "按日";
        }
        switch (selectedTimePeriod) {
            case "按年":
                return "YEAR(l.loss_date) = ?";
            case "按月":
                return "YEAR(l.loss_date) = ? AND MONTH(l.loss_date) = ?";
            default:
                return "DATE(l.loss_date) = ?";
        }
    }

    private void setDateParameters(PreparedStatement stmt) throws SQLException {
        Calendar cal = selectedDate;
        switch (selectedTimePeriod) {
            case "按年":
                stmt.setInt(1, cal.get(Calendar.YEAR));
                break;
            case "按月":
                stmt.setInt(1, cal.get(Calendar.YEAR));
                stmt.setInt(2, cal.get(Calendar.MONTH) + 1);
                break;
            default:
                String dateStr = String.format("%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));
                stmt.setString(1, dateStr);
                break;
        }
    }
}