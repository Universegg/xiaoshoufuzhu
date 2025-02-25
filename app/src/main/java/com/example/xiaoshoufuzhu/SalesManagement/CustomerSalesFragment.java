package com.example.xiaoshoufuzhu.SalesManagement;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SalesManagement.adapter.CustomerAdapter;
import com.example.xiaoshoufuzhu.Inventory.InventoryAdapter;
import com.example.xiaoshoufuzhu.SalesManagement.adapter.SalesRecordAdapter;
import com.example.xiaoshoufuzhu.SalesManagement.model.Customer;
import com.example.xiaoshoufuzhu.Inventory.InventoryItem;
import com.example.xiaoshoufuzhu.SalesManagement.model.SalesRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerSalesFragment extends Fragment {

    private Spinner spnCustomers, spnFilter;
    private ListView lvCustomerSalesRecords;
    private TextView tvCustomerDetails;
    private Button btnAddCustomer, btnUpdateCustomer, btnAddSalesRecord,btnSettleAll;
    private ArrayList<Customer> customerList;
    private ArrayList<SalesRecord> salesRecordList;
    private ArrayList<InventoryItem> inventoryList;
    private CustomerAdapter customerAdapter;
    private SalesRecordAdapter salesRecordAdapter;
    private InventoryAdapter inventoryAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SalesDataReceiver salesDataReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_sales, container, false);

        spnCustomers = view.findViewById(R.id.spnCustomers);
        spnFilter = view.findViewById(R.id.spnFilter);
        lvCustomerSalesRecords = view.findViewById(R.id.lvCustomerSalesRecords);
        tvCustomerDetails = view.findViewById(R.id.tvCustomerDetails);
        btnAddCustomer = view.findViewById(R.id.btnAddCustomer);
        btnUpdateCustomer = view.findViewById(R.id.btnUpdateCustomer);
        btnAddSalesRecord = view.findViewById(R.id.btnAddSalesRecord);
        btnSettleAll = view.findViewById(R.id.btnSettleAll);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        customerList = new ArrayList<>();
        salesRecordList = new ArrayList<>();
        inventoryList = new ArrayList<>();
        customerAdapter = new CustomerAdapter(getContext(), R.layout.custom_spinner_item, customerList);
        salesRecordAdapter = new SalesRecordAdapter(getContext(), salesRecordList);
        inventoryAdapter = new InventoryAdapter(getContext(), inventoryList);
        btnSettleAll.setOnClickListener(v -> settleAllCreditSales());
        spnCustomers.setAdapter(customerAdapter);
        lvCustomerSalesRecords.setAdapter(salesRecordAdapter);

        // 注册 BroadcastReceiver
        salesDataReceiver = new SalesDataReceiver(tvCustomerDetails, spnCustomers);
        IntentFilter filter = new IntentFilter("com.example.UPDATE_SALES_DATA");
        getContext().registerReceiver(salesDataReceiver, filter);

        loadCustomers();
        loadInventoryItems();

        spnCustomers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Customer selectedCustomer = customerList.get(position);
                loadCustomerSalesRecords(selectedCustomer.getId());

                // 更新采购商详细信息
                String customerDetails = "  名称: " + selectedCustomer.getName() + "\n" +
                        "  电话: " + selectedCustomer.getPhone() + "\n" +
                        "  地址: " + selectedCustomer.getAddress() + "\n" +
                        "  赊账金额: " + selectedCustomer.getAmount();
                tvCustomerDetails.setText(customerDetails);

                // 在 Spinner 中显示当前选中的采购商名称
                if (view instanceof TextView) {
                    ((TextView) view).setText(selectedCustomer.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                salesRecordList.clear();
                salesRecordAdapter.notifyDataSetChanged();
                tvCustomerDetails.setText(""); // 清空详细信息
            }
        });

        btnAddCustomer.setOnClickListener(v -> showAddCustomerDialog());
        btnUpdateCustomer.setOnClickListener(v -> showUpdateCustomerDialog());
        btnAddSalesRecord.setOnClickListener(v -> showAddSalesRecordDialog());

        // 设置筛选器选项
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFilter.setAdapter(filterAdapter);

        // 设置筛选器监听器
        spnFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 根据选择的筛选器选项进行筛选
                String filterOption = parent.getItemAtPosition(position).toString();
                filterSalesRecords(filterOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // 设置 SwipeRefreshLayout 下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 获取当前选中的采购商
            int selectedPosition = spnCustomers.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION) {
                Customer selectedCustomer = customerList.get(selectedPosition);
                loadCustomerSalesRecords(selectedCustomer.getId());
            }

            // 结束刷新动画
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销 BroadcastReceiver
        getContext().unregisterReceiver(salesDataReceiver);
    }


    private void loadCustomers() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT * FROM customers";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    customerList.clear();
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        String phone = resultSet.getString("phone");
                        String address = resultSet.getString("address");
                        double amount = resultSet.getDouble("amount");

                        customerList.add(new Customer(id, name, phone, address, amount));
                    }

                    getActivity().runOnUiThread(() -> customerAdapter.notifyDataSetChanged());
                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "采购商信息加载失败", Toast.LENGTH_SHORT).show());
                }
            } else {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Database connection failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadCustomerSalesRecords(int customerId) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    // 更新查询以确保列名和表名正确
                    String query = "SELECT rc.id, rc.product_id, rc.cid, p.name AS product_name, p.num AS batch_no, rc.quantity, rc.price, rc.sale_date, rc.total_price, rc.state " +
                            "FROM records_customers rc " +
                            "JOIN t_customers_records tcr ON rc.id = tcr.rid " +
                            "JOIN products p ON rc.product_id = p.id " +
                            "WHERE tcr.cid = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, customerId);
                    ResultSet resultSet = statement.executeQuery();

                    salesRecordList.clear();
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        int productId = resultSet.getInt("product_id");
                        int customerIdFromDB = resultSet.getInt("cid"); // 获取 cid
                        String productName = resultSet.getString("product_name");
                        String batchNo = resultSet.getString("batch_no");
                        int quantity = resultSet.getInt("quantity");
                        double price = resultSet.getDouble("price");
                        String saleDate = resultSet.getString("sale_date");
                        double totalPrice = resultSet.getDouble("total_price");
                        String state = resultSet.getString("state");

                        salesRecordList.add(new SalesRecord(id, productId, customerIdFromDB, quantity, saleDate, totalPrice, productName, batchNo, price, state));
                    }

                    getActivity().runOnUiThread(() -> salesRecordAdapter.notifyDataSetChanged());
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

    private void filterSalesRecords(String filterOption) {
        List<SalesRecord> filteredList = new ArrayList<>();
        for (SalesRecord record : salesRecordList) { // 使用完整的原始数据
            if (filterOption.equals("显示全部") ||
                    (filterOption.equals("仅显示赊账") && "赊账".equals(record.getState())) ||
                    (filterOption.equals("仅显示结清") && "结清".equals(record.getState()))) {
                filteredList.add(record);
            }
        }
        salesRecordAdapter.clear();
        salesRecordAdapter.addAll(filteredList);
        salesRecordAdapter.notifyDataSetChanged();
    }

    private void loadInventoryItems() {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT id, name, num, stock, price FROM products";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void settleAllCreditSales() {
        int position = spnCustomers.getSelectedItemPosition();
        if (position == Spinner.INVALID_POSITION) {
            Toast.makeText(getContext(), "请先选择采购商", Toast.LENGTH_SHORT).show();
            return;
        }
        Customer customer = customerList.get(position);

        new AlertDialog.Builder(getContext())
                .setTitle("一键结算")
                .setMessage("确定要结算所有赊账记录吗？")
                .setPositiveButton("确定", (dialog, which) -> processSettlement(customer))
                .setNegativeButton("取消", null)
                .show();
    }

    private void processSettlement(Customer customer) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false);

                    // 计算总赊账金额
                    String sumQuery = "SELECT SUM(total_price) AS total FROM records_customers WHERE cid=? AND state='赊账'";
                    PreparedStatement sumStmt = connection.prepareStatement(sumQuery);
                    sumStmt.setInt(1, customer.getId());
                    ResultSet rs = sumStmt.executeQuery();
                    double total = rs.next() ? rs.getDouble("total") : 0;

                    if (total == 0) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "没有赊账记录", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // 更新记录状态
                    String updateQuery = "UPDATE records_customers SET state='结清' WHERE cid=? AND state='赊账'";
                    PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                    updateStmt.setInt(1, customer.getId());
                    int updated = updateStmt.executeUpdate();

                    // 更新客户金额
                    String customerQuery = "UPDATE customers SET amount=amount-? WHERE id=?";
                    PreparedStatement customerStmt = connection.prepareStatement(customerQuery);
                    customerStmt.setDouble(1, total);
                    customerStmt.setInt(2, customer.getId());
                    customerStmt.executeUpdate();

                    connection.commit();

                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "已结算" + updated + "条记录", Toast.LENGTH_SHORT).show();
                        loadCustomerSalesRecords(customer.getId());
                        loadCustomers();
                        // 更新客户详情显示
                        getCustomerDetails(customer.getId(), details -> {
                            Intent intent = new Intent("com.example.UPDATE_SALES_DATA");
                            intent.putExtra("customer_details", details);
                            getContext().sendBroadcast(intent);
                        });
                    });
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                } finally {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void showAddCustomerDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_customer, null);
        EditText edtCustomerName = dialogView.findViewById(R.id.edtCustomerName);
        EditText edtCustomerPhone = dialogView.findViewById(R.id.edtCustomerPhone);
        EditText edtCustomerAddress = dialogView.findViewById(R.id.edtCustomerAddress);

        new AlertDialog.Builder(getContext())
                .setTitle("新增采购商")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    String name = edtCustomerName.getText().toString();
                    String phone = edtCustomerPhone.getText().toString();
                    String address = edtCustomerAddress.getText().toString();
                    addCustomerToDatabase(name, phone, address);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void addCustomerToDatabase(String name, String phone, String address) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "INSERT INTO customers (name, phone, address, amount) VALUES (?, ?, ?, 0)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, name);
                    statement.setString(2, phone);
                    statement.setString(3, address);

                    int rows = statement.executeUpdate();
                    if (rows > 0) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "采购商信息添加成功！", Toast.LENGTH_SHORT).show();
                            loadCustomers();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showUpdateCustomerDialog() {
        int position = spnCustomers.getSelectedItemPosition();
        if (position == Spinner.INVALID_POSITION) {
            Toast.makeText(getContext(), "请选择一个采购商", Toast.LENGTH_SHORT).show();
            return;
        }
        Customer customer = customerList.get(position);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_customer, null);
        EditText edtCustomerName = dialogView.findViewById(R.id.edtCustomerName);
        EditText edtCustomerPhone = dialogView.findViewById(R.id.edtCustomerPhone);
        EditText edtCustomerAddress = dialogView.findViewById(R.id.edtCustomerAddress);

        edtCustomerName.setText(customer.getName());
        edtCustomerPhone.setText(customer.getPhone());
        edtCustomerAddress.setText(customer.getAddress());

        new AlertDialog.Builder(getContext())
                .setTitle("更新采购商信息")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    String name = edtCustomerName.getText().toString();
                    String phone = edtCustomerPhone.getText().toString();
                    String address = edtCustomerAddress.getText().toString();
                    updateCustomerInDatabase(customer, name, phone, address);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateCustomerInDatabase(Customer customer, String name, String phone, String address) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "UPDATE customers SET name = ?, phone = ?, address = ? WHERE id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, name);
                    statement.setString(2, phone);
                    statement.setString(3, address);
                    statement.setInt(4, customer.getId());

                    int rows = statement.executeUpdate();
                    if (rows > 0) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "采购商信息更新成功", Toast.LENGTH_SHORT).show();
                            loadCustomers();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showAddSalesRecordDialog() {
        int position = spnCustomers.getSelectedItemPosition();
        if (position == Spinner.INVALID_POSITION) {
            Toast.makeText(getContext(), "请选择一个采购商", Toast.LENGTH_SHORT).show();
            return;
        }
        Customer customer = customerList.get(position);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_sales_record, null);
        Spinner spnProducts = dialogView.findViewById(R.id.spnProducts);
        EditText edtQuantity = dialogView.findViewById(R.id.edtQuantity);
        EditText edtUnitPrice = dialogView.findViewById(R.id.edtUnitPrice);
        EditText edtActualAmount = dialogView.findViewById(R.id.edtActualAmount); // 实收金额输入框

        spnProducts.setAdapter(inventoryAdapter);

        spnProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                InventoryItem selectedItem = inventoryList.get(position);
                edtUnitPrice.setText(String.valueOf(selectedItem.getPrice())); // 显示单价
                updateActualAmount(edtQuantity, edtUnitPrice, edtActualAmount);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                edtUnitPrice.setText(""); // 清空单价
            }
        });

        edtQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateActualAmount(edtQuantity, edtUnitPrice, edtActualAmount);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        new AlertDialog.Builder(getContext())
                .setTitle("添加销售记录")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    InventoryItem selectedItem = (InventoryItem) spnProducts.getSelectedItem();
                    int quantity = Integer.parseInt(edtQuantity.getText().toString());
                    double unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
                    double actualAmount = Double.parseDouble(edtActualAmount.getText().toString());
                    addSalesRecordToDatabase(customer, selectedItem, quantity, unitPrice, actualAmount);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateActualAmount(EditText edtQuantity, EditText edtUnitPrice, EditText edtActualAmount) {
        String quantityStr = edtQuantity.getText().toString();
        String unitPriceStr = edtUnitPrice.getText().toString();
        if (!quantityStr.isEmpty() && !unitPriceStr.isEmpty()) {
            int quantity = Integer.parseInt(quantityStr);
            double unitPrice = Double.parseDouble(unitPriceStr);
            double actualAmount = quantity * unitPrice;
            edtActualAmount.setText(String.valueOf(actualAmount));
        }
    }

    private void addSalesRecordToDatabase(Customer customer, InventoryItem item, int quantity, double unitPrice, double actualAmount) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false); // 开始事务

                    // 插入销售记录到 records_customers 表
                    String insertRecordQuery = "INSERT INTO records_customers (cid, product_id, quantity, price, sale_date, total_price, state) " +
                            "VALUES (?, ?, ?, ?, NOW(), ?, '赊账')";
                    PreparedStatement insertRecordStmt = connection.prepareStatement(insertRecordQuery, Statement.RETURN_GENERATED_KEYS);
                    insertRecordStmt.setInt(1, customer.getId());
                    insertRecordStmt.setInt(2, item.getId());
                    insertRecordStmt.setInt(3, quantity);
                    insertRecordStmt.setDouble(4, unitPrice);
                    insertRecordStmt.setDouble(5, actualAmount);
                    insertRecordStmt.executeUpdate();

                    int rid = -1;
                    ResultSet generatedKeys = insertRecordStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        rid = generatedKeys.getInt(1);
                    }

                    // 插入中间表 t_customers_records
                    if (rid != -1) {
                        String insertCustomerRecordQuery = "INSERT INTO t_customers_records (cid, rid) VALUES (?, ?)";
                        PreparedStatement insertCustomerRecordStmt = connection.prepareStatement(insertCustomerRecordQuery);
                        insertCustomerRecordStmt.setInt(1, customer.getId());
                        insertCustomerRecordStmt.setInt(2, rid);
                        insertCustomerRecordStmt.executeUpdate();
                    }

                    // 更新采购商赊账金额
                    String updateCustomerQuery = "UPDATE customers SET amount = amount + ? WHERE id = ?";
                    PreparedStatement updateCustomerStmt = connection.prepareStatement(updateCustomerQuery);
                    updateCustomerStmt.setDouble(1, actualAmount);
                    updateCustomerStmt.setInt(2, customer.getId());
                    updateCustomerStmt.executeUpdate();

                    // 更新产品库存
                    String updateProductQuery = "UPDATE products SET stock = stock - ? WHERE id = ?";
                    PreparedStatement updateProductStmt = connection.prepareStatement(updateProductQuery);
                    updateProductStmt.setInt(1, quantity);
                    updateProductStmt.setInt(2, item.getId());
                    updateProductStmt.executeUpdate();

                    connection.commit(); // 提交事务
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "销售记录添加成功", Toast.LENGTH_SHORT).show();
                        loadCustomerSalesRecords(customer.getId());

                        // 获取采购商详细信息并发送广播
                        getCustomerDetails(customer.getId(), customerDetails -> {
                            Intent intent = new Intent("com.example.UPDATE_SALES_DATA");
                            intent.putExtra("customer_details", customerDetails);
                            getContext().sendBroadcast(intent);
                        });
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        connection.rollback(); // 出现异常时回滚事务
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                } finally {
                    try {
                        connection.setAutoCommit(true); // 恢复自动提交
                    } catch (SQLException autoCommitEx) {
                        autoCommitEx.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void getCustomerDetails(int customerId, OnCustomerDetailsFetchedListener listener) {
        new Thread(() -> {
            String customerDetails = "";
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT name, phone, address, amount FROM customers WHERE id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, customerId);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        String name = resultSet.getString("name");
                        String phone = resultSet.getString("phone");
                        String address = resultSet.getString("address");
                        double amount = resultSet.getDouble("amount");

                        customerDetails = "  名称: " + name + "\n" +
                                "  电话: " + phone + "\n" +
                                "  地址: " + address + "\n" +
                                "  赊账金额: " + amount;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "获取采购商详细信息失败", Toast.LENGTH_SHORT).show()
                    );
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "数据库连接失败", Toast.LENGTH_SHORT).show()
                );
            }
            String finalCustomerDetails = customerDetails;
            getActivity().runOnUiThread(() -> listener.onCustomerDetailsFetched(finalCustomerDetails));
        }).start();
    }

    interface OnCustomerDetailsFetchedListener {
        void onCustomerDetailsFetched(String customerDetails);
    }

    public void onToggleSalesState(SalesRecord salesRecord) {
        new Thread(() -> {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false);

                    // 更新销售记录状态
                    String updateStateQuery = "UPDATE records_customers SET state = '结清' WHERE id = ?";
                    PreparedStatement updateStateStmt = connection.prepareStatement(updateStateQuery);
                    updateStateStmt.setInt(1, salesRecord.getId());
                    updateStateStmt.executeUpdate();

                    // 减少采购商的赊账金额
                    String updateCustomerQuery = "UPDATE customers SET amount = amount - ? WHERE id = ?";
                    PreparedStatement updateCustomerStmt = connection.prepareStatement(updateCustomerQuery);
                    updateCustomerStmt.setDouble(1, salesRecord.getTotalPrice());
                    updateCustomerStmt.setInt(2, salesRecord.getCid());
                    updateCustomerStmt.executeUpdate();

                    connection.commit();

                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "状态更新成功", Toast.LENGTH_SHORT).show();
                        loadCustomerSalesRecords(salesRecord.getCid());

                        // 获取最新的采购商详细信息并发送广播
                        getCustomerDetails(salesRecord.getCid(), customerDetails -> {
                            Intent intent = new Intent("com.example.UPDATE_SALES_DATA");
                            intent.putExtra("customer_details", customerDetails);
                            getContext().sendBroadcast(intent);
                        });
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException autoCommitEx) {
                        autoCommitEx.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public interface CustomerSalesListener {
        void onToggleSalesState(SalesRecord salesRecord);
    }

}