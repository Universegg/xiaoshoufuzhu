package com.example.xiaoshoufuzhu.SupplierSalesManagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.InventoryAdapter;
import com.example.xiaoshoufuzhu.InventoryItem;
import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.adapter.PurchaseRecordAdapter;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.adapter.SupplierAdapter;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.model.PurchaseRecord;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierSalesActivity extends AppCompatActivity {

    private Spinner spnSuppliers;
    private TextView tvSupplierDetails;
    private Button btnAddSupplier;
    private Button btnUpdateSupplier;
    private Button btnAddPurchaseRecord;
    private ListView lvSupplierPurchaseRecords;

    private PurchaseRecordAdapter purchaseRecordAdapter;
    private List<PurchaseRecord> purchaseRecordList;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> inventoryList;
    private List<Supplier> supplierList;
    private SupplierAdapter supplierAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_sales);

        // 初始化视图
        spnSuppliers = findViewById(R.id.spnSuppliers);
        tvSupplierDetails = findViewById(R.id.tvSupplierDetails);
        btnAddSupplier = findViewById(R.id.btnAddSupplier);
        btnUpdateSupplier = findViewById(R.id.btnUpdateSupplier);
        btnAddPurchaseRecord = findViewById(R.id.btnAddPurchaseRecord);
        lvSupplierPurchaseRecords = findViewById(R.id.lvSupplierPurchaseRecords);

        // 初始化数据适配器
        purchaseRecordList = new ArrayList<>();
        inventoryList = new ArrayList<>();
        supplierList = new ArrayList<>();
        purchaseRecordAdapter = new PurchaseRecordAdapter(this, purchaseRecordList);
        inventoryAdapter = new InventoryAdapter(this, inventoryList);
        supplierAdapter = new SupplierAdapter(this, supplierList);
        lvSupplierPurchaseRecords.setAdapter(purchaseRecordAdapter);

        // 供应商下拉菜单配置
        spnSuppliers.setAdapter(supplierAdapter);
        spnSuppliers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Supplier selectedSupplier = supplierList.get(position);
                tvSupplierDetails.setText("电话: " + selectedSupplier.getPhone() + "\n地址: " + selectedSupplier.getAddress());
                loadPurchaseRecords(selectedSupplier.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSupplierDetails.setText("");
            }
        });

        // 按钮点击监听
        btnAddSupplier.setOnClickListener(v -> showAddSupplierDialog());
        btnUpdateSupplier.setOnClickListener(v -> showUpdateSupplierDialog());
        btnAddPurchaseRecord.setOnClickListener(v -> showAddPurchaseRecordDialog());

        // 加载初始数据
        loadSuppliers();
        loadInventoryItems();
    }

    // 加载供应商数据
    private void loadSuppliers() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT id, name, phone, address FROM suppliers";
                    PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();

                    supplierList.clear();
                    while (resultSet.next()) {
                        supplierList.add(new Supplier(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("phone"),
                                resultSet.getString("address")
                        ));
                    }

                    runOnUiThread(() -> supplierAdapter.notifyDataSetChanged());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 加载库存商品数据
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
                        inventoryList.add(new InventoryItem(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("num"),
                                resultSet.getInt("stock"),
                                resultSet.getDouble("price")
                        ));
                    }

                    runOnUiThread(() -> inventoryAdapter.notifyDataSetChanged());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 加载采购记录（修改后的方法）
    private void loadPurchaseRecords(int supplierId) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT id, name, num, quantity, price, purchase_date, total_price, freight " +
                            "FROM records_suppliers " +
                            "WHERE sid = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, supplierId);
                    ResultSet resultSet = statement.executeQuery();

                    purchaseRecordList.clear();
                    while (resultSet.next()) {
                        purchaseRecordList.add(new PurchaseRecord(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("num"),
                                resultSet.getInt("quantity"),
                                resultSet.getDouble("price"),
                                resultSet.getString("purchase_date"),
                                resultSet.getDouble("total_price"),
                                resultSet.getDouble("freight")
                        ));
                    }

                    runOnUiThread(() -> purchaseRecordAdapter.notifyDataSetChanged());
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(SupplierSalesActivity.this, "加载采购记录失败", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    // 添加供应商对话框（保持不变）
    private void showAddSupplierDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_suppliers, null);
        EditText edtSupplierName = dialogView.findViewById(R.id.edtSupplierName);
        EditText edtSupplierPhone = dialogView.findViewById(R.id.edtSupplierPhone);
        EditText edtSupplierAddress = dialogView.findViewById(R.id.edtSupplierAddress);

        new AlertDialog.Builder(this)
                .setTitle("新增供应商")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    String name = edtSupplierName.getText().toString();
                    String phone = edtSupplierPhone.getText().toString();
                    String address = edtSupplierAddress.getText().toString();
                    addSupplierToDatabase(name, phone, address);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void addSupplierToDatabase(String name, String phone, String address) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "INSERT INTO suppliers (name, phone, address) VALUES (?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, name);
                    statement.setString(2, phone);
                    statement.setString(3, address);

                    int rows = statement.executeUpdate();
                    if (rows > 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(SupplierSalesActivity.this, "供应商信息添加成功！", Toast.LENGTH_SHORT).show();
                            loadSuppliers();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showUpdateSupplierDialog() {
        int position = spnSuppliers.getSelectedItemPosition();
        if (position == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "请选择一个供应商", Toast.LENGTH_SHORT).show();
            return;
        }
        Supplier supplier = supplierList.get(position);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_supplier, null);
        EditText edtSupplierName = dialogView.findViewById(R.id.edtSupplierName);
        EditText edtSupplierPhone = dialogView.findViewById(R.id.edtSupplierPhone);
        EditText edtSupplierAddress = dialogView.findViewById(R.id.edtSupplierAddress);

        edtSupplierName.setText(supplier.getName());
        edtSupplierPhone.setText(supplier.getPhone());
        edtSupplierAddress.setText(supplier.getAddress());

        new AlertDialog.Builder(this)
                .setTitle("更新供应商信息")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    String name = edtSupplierName.getText().toString();
                    String phone = edtSupplierPhone.getText().toString();
                    String address = edtSupplierAddress.getText().toString();
                    updateSupplierInDatabase(supplier, name, phone, address);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateSupplierInDatabase(Supplier supplier, String name, String phone, String address) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "UPDATE suppliers SET name = ?, phone = ?, address = ? WHERE id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, name);
                    statement.setString(2, phone);
                    statement.setString(3, address);
                    statement.setInt(4, supplier.getId());

                    int rows = statement.executeUpdate();
                    if (rows > 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(SupplierSalesActivity.this, "供应商信息更新成功", Toast.LENGTH_SHORT).show();
                            loadSuppliers();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showAddPurchaseRecordDialog() {
        // 检查是否已选择供应商
        int position = spnSuppliers.getSelectedItemPosition();
        if (position == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "请选择一个供应商", Toast.LENGTH_SHORT).show();
            return;
        }
        Supplier selectedSupplier = supplierList.get(position);

        // 初始化对话框视图
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_purchase_records, null);
        TextView tvSelectedSupplier = dialogView.findViewById(R.id.tvSelectedSupplier); // 当前供应商显示
        EditText edtProductName = dialogView.findViewById(R.id.edtProductName); // 产品名称输入框
        EditText edtBatchNo = dialogView.findViewById(R.id.edtBatchNo); // 批次号输入框
        EditText edtQuantity = dialogView.findViewById(R.id.edtQuantity); // 数量输入框
        EditText edtUnitPrice = dialogView.findViewById(R.id.edtUnitPrice); // 单价输入框
        TextView tvReceivablePrice = dialogView.findViewById(R.id.tvReceivablePrice); // 应收金额显示
        EditText edtActualAmount = dialogView.findViewById(R.id.edtActualAmount); // 实收金额输入框
        EditText edtFreight = dialogView.findViewById(R.id.edtFreight); // 运费输入框

        // 显示当前选中的供应商
        tvSelectedSupplier.setText(selectedSupplier.getName());

        // 数量输入监听
        edtQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateReceivablePrice(edtQuantity, edtUnitPrice, edtFreight, tvReceivablePrice);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 单价输入监听
        edtUnitPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateReceivablePrice(edtQuantity, edtUnitPrice, edtFreight, tvReceivablePrice);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 运费输入监听
        edtFreight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateReceivablePrice(edtQuantity, edtUnitPrice, edtFreight, tvReceivablePrice);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("添加采购记录")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    try {
                        String productName = edtProductName.getText().toString().trim();
                        String batchNo = edtBatchNo.getText().toString().trim();
                        int quantity = Integer.parseInt(edtQuantity.getText().toString());
                        double unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
                        double actualAmount = Double.parseDouble(edtActualAmount.getText().toString());
                        double freight = Double.parseDouble(edtFreight.getText().toString());

                        // 检查产品名称和批次号是否为空
                        if (productName.isEmpty() || batchNo.isEmpty()) {
                            Toast.makeText(this, "请输入产品名称和批次号", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addPurchaseRecordToDatabase(
                                selectedSupplier,
                                productName,
                                batchNo,
                                quantity,
                                unitPrice,
                                actualAmount,
                                freight
                        );
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "请输入有效的数值", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void calculateReceivablePrice(EditText edtQuantity, EditText edtUnitPrice, EditText edtFreight, TextView tvReceivablePrice) {
        try {
            int quantity = Integer.parseInt(edtQuantity.getText().toString());
            double unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
            double freight = Double.parseDouble(edtFreight.getText().toString());
            double receivable = quantity * unitPrice + freight;
            tvReceivablePrice.setText(String.format("¥%.2f", receivable));
        } catch (NumberFormatException e) {
            tvReceivablePrice.setText("¥0.00");
        }
    }

    private void addPurchaseRecordToDatabase(Supplier supplier, String productName,
                                             String batchNo, int quantity,
                                             double unitPrice, double actualAmount,
                                             double freight) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false);

                    // 插入采购记录
                    String insertQuery = "INSERT INTO records_suppliers " +
                            "(sid, name, num, quantity, price, total_price, purchase_date, state, freight) " +
                            "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?)";

                    PreparedStatement pstmt = connection.prepareStatement(insertQuery);
                    pstmt.setInt(1, supplier.getId());
                    pstmt.setString(2, productName); // 使用输入的产品名称
                    pstmt.setString(3, batchNo);     // 使用输入的批次号
                    pstmt.setInt(4, quantity);
                    pstmt.setDouble(5, unitPrice);
                    pstmt.setDouble(6, actualAmount < (quantity * unitPrice + freight) ? actualAmount : (quantity * unitPrice + freight)); // 修改 total_price 计算
                    pstmt.setString(7, actualAmount < (quantity * unitPrice + freight) ? "0" : "1"); // 状态判断
                    pstmt.setDouble(8, freight); // 添加运费字段
                    pstmt.executeUpdate();

                    connection.commit();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "采购记录添加成功", Toast.LENGTH_SHORT).show();
                        loadPurchaseRecords(supplier.getId());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    runOnUiThread(() ->
                            Toast.makeText(this, "添加失败：" + e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }
}