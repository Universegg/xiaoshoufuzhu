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
import com.example.xiaoshoufuzhu.Inventory.InventoryAdapter;
import com.example.xiaoshoufuzhu.Inventory.InventoryItem;
import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.adapter.PurchaseRecordAdapter;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.adapter.SupplierAdapter;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.model.PurchaseRecord;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.model.Supplier;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    // 加载采购记录
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

    // 显示添加供应商对话框
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

    // 添加供应商到数据库
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

    // 显示更新供应商对话框
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

    // 更新供应商信息
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

    // 显示添加采购记录对话框
    private void showAddPurchaseRecordDialog() {
        int position = spnSuppliers.getSelectedItemPosition();
        if (position == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "请选择一个供应商", Toast.LENGTH_SHORT).show();
            return;
        }
        Supplier selectedSupplier = supplierList.get(position);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_purchase_records, null);
        TextView tvSelectedSupplier = dialogView.findViewById(R.id.tvSelectedSupplier);
        EditText edtProductName = dialogView.findViewById(R.id.edtProductName);
        EditText edtBatchNo = dialogView.findViewById(R.id.edtBatchNo);
        EditText edtQuantity = dialogView.findViewById(R.id.edtQuantity);
        EditText edtUnitPrice = dialogView.findViewById(R.id.edtUnitPrice);
        TextView tvReceivablePrice = dialogView.findViewById(R.id.tvReceivablePrice);
        EditText edtActualAmount = dialogView.findViewById(R.id.edtActualAmount);
        EditText edtFreight = dialogView.findViewById(R.id.edtFreight);
        Button btnGenerateBatchNo = dialogView.findViewById(R.id.btnGenerateBatchNo);


        // 初始化修改标志
        boolean[] isAmountModified = {false};

        tvSelectedSupplier.setText(selectedSupplier.getName());

        // 生成批号按钮点击事件
        btnGenerateBatchNo.setOnClickListener(v -> {
            String productName = edtProductName.getText().toString().trim();
            if (productName.isEmpty()) {
                Toast.makeText(this, "请输入产品名称", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Connection connection = DatabaseHelper.getConnection();
                try {
                    // 1. 查询产品或生成新编码
                    Map<String, Object> codeResult = getProductCode(connection, productName);
                    String productCode = (String) codeResult.get("code");
                    boolean isNewProduct = (boolean) codeResult.get("isNew");

                    // 2. 获取最新序列号
                    int maxSequence = isNewProduct ? 0 : getMaxSequence(connection, productCode);
                    String newSequence = String.format(Locale.getDefault(), "%03d", maxSequence + 1);

                    // 3. 生成完整批号
                    SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.getDefault());
                    String batchNo = productCode + "-" + sdf.format(new Date()) + "-" + newSequence;

                    runOnUiThread(() -> {
                        edtBatchNo.setText(batchNo);
                        // 安全设置光标位置
                        int safePosition = Math.min(batchNo.length(), edtBatchNo.getText().length());
                        edtBatchNo.setSelection(safePosition);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "批号生成失败：" + e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    try {
                        if (connection != null) connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        });
        // 金额计算监听器
        TextWatcher calculatorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculatePrices(
                        edtQuantity,
                        edtUnitPrice,
                        edtFreight,
                        tvReceivablePrice,
                        edtActualAmount,
                        isAmountModified
                );
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // 监听实收金额的手动修改
        edtActualAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || before > 0) {
                    isAmountModified[0] = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtQuantity.addTextChangedListener(calculatorWatcher);
        edtUnitPrice.addTextChangedListener(calculatorWatcher);
        edtFreight.addTextChangedListener(calculatorWatcher);

        new AlertDialog.Builder(this)
                .setTitle("添加采购记录")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    try {
                        String productName = edtProductName.getText().toString().trim();
                        String batchNo = edtBatchNo.getText().toString().trim();
                        int quantity = Integer.parseInt(edtQuantity.getText().toString());
                        double unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
                        double freight = Double.parseDouble(edtFreight.getText().toString());
                        double actualAmount = Double.parseDouble(edtActualAmount.getText().toString());

                        // 校验批号格式（新规则：XXX-231115-001）
                        if (!isValidBatchNo(batchNo)) {
                            Toast.makeText(this, "批号格式错误！示例：P100-231115-001", Toast.LENGTH_SHORT).show();
                            return;
                        }

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

    // 实时计算价格
    private void calculatePrices(EditText edtQuantity,
                                 EditText edtUnitPrice,
                                 EditText edtFreight,
                                 TextView tvReceivablePrice,
                                 EditText edtActualAmount,
                                 boolean[] isAmountModified) {
        try {
            int quantity = Integer.parseInt(edtQuantity.getText().toString());
            double unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
            double freight = Double.parseDouble(edtFreight.getText().toString());
            double receivable = quantity * unitPrice + freight;

            tvReceivablePrice.setText(String.format("应收金额：¥%.2f", receivable));

            // 仅当用户未手动修改时自动更新
            if (!isAmountModified[0]) {
                edtActualAmount.setText(String.format("%.2f", receivable));
            }
        } catch (NumberFormatException e) {
            tvReceivablePrice.setText("应收金额：¥0.00");
            if (!isAmountModified[0]) {
                edtActualAmount.setText("0.00");
            }
        }
    }

    // 添加采购记录到数据库
    private void addPurchaseRecordToDatabase(Supplier supplier,
                                             String productName,
                                             String batchNo,
                                             int quantity,
                                             double unitPrice,
                                             double actualAmount,
                                             double freight) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false);

                    String insertQuery = "INSERT INTO records_suppliers " +
                            "(sid, name, num, quantity, price, total_price, purchase_date, state, freight) " +
                            "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?)";

                    PreparedStatement pstmt = connection.prepareStatement(insertQuery);
                    pstmt.setInt(1, supplier.getId());
                    pstmt.setString(2, productName);
                    pstmt.setString(3, batchNo);
                    pstmt.setInt(4, quantity);
                    pstmt.setDouble(5, unitPrice);
                    pstmt.setDouble(6, actualAmount);
                    pstmt.setString(7, "0"); // 固定状态为0
                    pstmt.setDouble(8, freight);
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
    // 校验批号格式方法
    private boolean isValidBatchNo(String batchNo) {
        // 格式：产品编码-6位日期-3位序号（如 P100-231115-001）
        String regex = "^[A-Za-z0-9]+-\\d{6}-\\d{3}$";
        return batchNo.matches(regex);
    }

    private Map<String, Object> getProductCode(Connection connection, String productName) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        // 尝试查询现有产品
        String query = "SELECT num FROM products WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, productName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                result.put("code", rs.getString("num"));
                result.put("isNew", false);
            } else {
                // 生成新编码：中文转拼音首字母+数字
                String pinyin = convertToPinyinCode(productName);
                result.put("code", generateNewCode(pinyin));
                result.put("isNew", true);
            }
        }
        return result;
    }

    private int getMaxSequence(Connection connection, String productCode) throws SQLException {
        String query = "SELECT MAX(CAST(SUBSTRING_INDEX(num, '-', -1) AS UNSIGNED)) " +
                "FROM records_suppliers WHERE num LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, productCode + "-%");
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    // 中文转拼音处理
    private String convertToPinyinCode(String chinese) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuilder code = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]")) {
                try {
                    String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyin != null && pinyin.length > 0) {
                        code.append(pinyin[0].charAt(0)); // 只取首字母
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    code.append('X'); // 异常字符替换为X
                }
            } else if (Character.isLetter(c)) { // 仅保留字母
                code.append(Character.toUpperCase(c));
            }
        }
        return code.toString();
    }
    private String generateNewCode(String rawCode) {
        // 规范编码格式：字母开头，总长4位
        String cleanCode = rawCode.replaceAll("[^A-Z]", "");
        // 处理空值情况
        if (cleanCode.isEmpty()) return "AAA";

        // 确保3位字母：不足补A，超长截断
        return cleanCode.length() >= 3 ?
                cleanCode.substring(0, 3) :
                String.format("%-3s", cleanCode).replace(' ', 'A');

    }
    
}