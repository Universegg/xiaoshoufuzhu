package com.example.xiaoshoufuzhu.SalesManagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.InventoryAdapter;
import com.example.xiaoshoufuzhu.SalesManagement.adapter.RetailSalesAdapter;
import com.example.xiaoshoufuzhu.InventoryItem;
import com.example.xiaoshoufuzhu.SalesManagement.model.RetailSales;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RetailSalesFragment extends Fragment {

    private TextView tvCurrentDate;
    private Button btnAddRetailSales;
    private ListView lvRetailSalesRecords;

    private RetailSalesAdapter retailSalesAdapter;
    private List<RetailSales> retailSalesList;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> inventoryList;
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_retailer_sales, container, false);

        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        btnAddRetailSales = view.findViewById(R.id.btnAddRetailSales);
        lvRetailSalesRecords = view.findViewById(R.id.lvRetailSalesRecords);

        retailSalesList = new ArrayList<>();
        inventoryList = new ArrayList<>();
        retailSalesAdapter = new RetailSalesAdapter(getContext(), retailSalesList);
        inventoryAdapter = new InventoryAdapter(getContext(), inventoryList);
        lvRetailSalesRecords.setAdapter(retailSalesAdapter);

        // 设置当前日期显示
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        updateDateDisplay(year, month, day);

        // 设置日期选择器点击事件
        tvCurrentDate.setOnClickListener(v -> showDatePickerDialog(year, month, day));

        // 默认加载当天的销售记录
        String today = String.format("%d-%02d-%02d", year, month + 1, day);
        loadSalesRecords(today);

        btnAddRetailSales.setOnClickListener(v -> showAddSalesRecordDialog());

        return view;
    }

    private void showDatePickerDialog(int year, int month, int day) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            updateDateDisplay(selectedYear, selectedMonth, selectedDay);
            String selectedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            loadSalesRecords(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void updateDateDisplay(int year, int month, int day) {
        String date = String.format("%d-%02d-%02d", year, month + 1, day);
        tvCurrentDate.setText(date);
    }

    private void loadSalesRecords(String date) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT s.id, s.product_id, p.name AS product_name, p.num AS batch_no, s.quantity, s.total_price, s.sale_date, p.price AS unit_price " +
                            "FROM sales s " +
                            "JOIN products p ON s.product_id = p.id " +
                            "WHERE DATE(s.sale_date) = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, date);
                    ResultSet resultSet = statement.executeQuery();

                    retailSalesList.clear();
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        int productId = resultSet.getInt("product_id");
                        String productName = resultSet.getString("product_name");
                        String batchNo = resultSet.getString("batch_no");
                        int quantity = resultSet.getInt("quantity");
                        double totalPrice = resultSet.getDouble("total_price");
                        String saleDate = resultSet.getString("sale_date");
                        double unitPrice = resultSet.getDouble("unit_price");

                        retailSalesList.add(new RetailSales(id, productId, quantity, saleDate, totalPrice, productName, batchNo, unitPrice));
                    }

                    getActivity().runOnUiThread(() -> retailSalesAdapter.notifyDataSetChanged());
                } catch (SQLException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "加载销售记录失败", Toast.LENGTH_SHORT).show());
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Database connection failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadInventoryItems() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT id, name, num, stock, price FROM products";
                    PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();

                    inventoryList.clear();
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        String batchNo = resultSet.getString("num");
                        int stock = resultSet.getInt("stock");
                        double price = resultSet.getDouble("price");

                        inventoryList.add(new InventoryItem(id, name, batchNo, stock, price));
                    }

                    getActivity().runOnUiThread(() -> inventoryAdapter.notifyDataSetChanged());
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void showAddSalesRecordDialog() {
        // 创建对话框视图
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_sales_record, null);
        Spinner spnProducts = dialogView.findViewById(R.id.spnProducts);
        EditText edtQuantity = dialogView.findViewById(R.id.edtQuantity);
        EditText edtUnitPrice = dialogView.findViewById(R.id.edtUnitPrice);
        EditText edtActualAmount = dialogView.findViewById(R.id.edtActualAmount);

        spnProducts.setAdapter(inventoryAdapter);
        loadInventoryItems();

        // 当选择产品时，显示单价，并计算应收金额
        spnProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                InventoryItem selectedProduct = (InventoryItem) parent.getItemAtPosition(position);
                edtUnitPrice.setText(String.valueOf(selectedProduct.getPrice())); // 显示单价，不会改变
                calculateTotalPrice(edtQuantity, edtUnitPrice, edtActualAmount); // 计算应收金额
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // 添加监听器，在用户修改数量或单价时重新计算应收金额
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalPrice(edtQuantity, edtUnitPrice, edtActualAmount);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtQuantity.addTextChangedListener(textWatcher);
        edtUnitPrice.addTextChangedListener(textWatcher);

        // 显示对话框
        new AlertDialog.Builder(getContext())
                .setTitle("添加销售记录")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    try {
                        InventoryItem selectedProduct = (InventoryItem) spnProducts.getSelectedItem();
                        int quantity = Integer.parseInt(edtQuantity.getText().toString());
                        double unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
                        double totalPrice = Double.parseDouble(edtActualAmount.getText().toString());

                        // 将销售记录添加到数据库
                        addSalesRecordToDatabase(selectedProduct, quantity, unitPrice, totalPrice);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "请输入有效的数量和单价", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void calculateTotalPrice(EditText edtQuantity, EditText edtUnitPrice, EditText edtActualAmount) {
        try {
            int quantity = Integer.parseInt(edtQuantity.getText().toString());
            double unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
            double totalPrice = quantity * unitPrice;
            edtActualAmount.setText(String.valueOf(totalPrice));
        } catch (NumberFormatException e) {
            // 忽略错误，保持应收金额为空
        }
    }

    private void addSalesRecordToDatabase(InventoryItem product, int quantity, double unitPrice, double totalPrice) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false); // 开始事务

                    // 插入销售记录到 sales 表
                    String insertSalesQuery = "INSERT INTO sales (product_id, quantity, sale_date, total_price) " +
                            "VALUES (?, ?, NOW(), ?)";
                    PreparedStatement insertSalesStmt = connection.prepareStatement(insertSalesQuery);
                    insertSalesStmt.setInt(1, product.getId());
                    insertSalesStmt.setInt(2, quantity);
                    insertSalesStmt.setDouble(3, totalPrice);
                    insertSalesStmt.executeUpdate();

                    // 更新产品库存
                    String updateProductQuery = "UPDATE products SET stock = stock - ? WHERE id = ?";
                    PreparedStatement updateProductStmt = connection.prepareStatement(updateProductQuery);
                    updateProductStmt.setInt(1, quantity);
                    updateProductStmt.setInt(2, product.getId());
                    updateProductStmt.executeUpdate();

                    connection.commit(); // 提交事务
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "销售记录添加成功", Toast.LENGTH_SHORT).show();
                        // 重新加载当天的销售记录
                        String today = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                        loadSalesRecords(today);
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        connection.rollback(); // 出现异常时回滚事务
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                } finally {
                    try {
                        connection.setAutoCommit(true); // 恢复自动提交
                        connection.close(); // 关闭连接
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Database connection failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}