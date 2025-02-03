package com.example.xiaoshoufuzhu.PriceLossManagement.ProductCheck;

import android.util.Log;

import com.example.xiaoshoufuzhu.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    // 拉取进货记录到待入库表
    public static void fetchRecordsToPendingInventory() {
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            PreparedStatement selectStatement = null;
            PreparedStatement insertStatement = null;
            PreparedStatement updateStatement = null;
            ResultSet resultSet = null;
            try {
                connection.setAutoCommit(false); // 开启事务

                // 查询 state = '0' 的记录
                String selectQuery = "SELECT * FROM records_suppliers WHERE state = '0'";
                selectStatement = connection.prepareStatement(selectQuery);
                resultSet = selectStatement.executeQuery();

                // 插入到待入库表并更新记录状态
                String insertQuery = "INSERT INTO stockpending (sid, name, num, price, quantity, state) VALUES (?, ?, ?, ?, ?, '待盘点')";
                String updateQuery = "UPDATE records_suppliers SET state = '1' WHERE id = ?";
                insertStatement = connection.prepareStatement(insertQuery);
                updateStatement = connection.prepareStatement(updateQuery);

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    int productId = resultSet.getInt("sid");
                    String name = resultSet.getString("name");
                    String batchNumber = resultSet.getString("num");
                    double price = resultSet.getDouble("price");
                    double quantity = resultSet.getDouble("quantity");

                    // 插入到待入库表
                    insertStatement.setInt(1, productId);
                    insertStatement.setString(2, name);
                    insertStatement.setString(3, batchNumber);
                    insertStatement.setDouble(4, price);
                    insertStatement.setDouble(5, quantity);
                    insertStatement.executeUpdate();

                    // 更新进货记录的状态
                    updateStatement.setInt(1, id);
                    updateStatement.executeUpdate();
                }

                connection.commit(); // 提交事务
                Log.d("Database", "Records fetched and updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    connection.rollback(); // 回滚事务
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            } finally {
                DatabaseHelper.close(connection, selectStatement, resultSet);
                DatabaseHelper.close(null, insertStatement, null);
                DatabaseHelper.close(null, updateStatement, null);
            }
        }
    }

    // 从待入库表中获取待盘点记录
    public static List<PendingInventory> fetchPendingInventory() {
        List<PendingInventory> pendingInventoryList = new ArrayList<>();
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                String query = "SELECT * FROM stockpending WHERE state = '待盘点'";
                statement = connection.prepareStatement(query);
                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    int productId = resultSet.getInt("sid");
                    String name = resultSet.getString("name");
                    String batchNumber = resultSet.getString("num");
                    double price = resultSet.getDouble("price");
                    double quantity = resultSet.getDouble("quantity");
                    String state = resultSet.getString("state");
                    String supplierName = getSupplierName(productId); // 获取供应商名称

                    PendingInventory inventory = new PendingInventory(id, productId, name, batchNumber, price, quantity, state, supplierName);
                    pendingInventoryList.add(inventory);

                    Log.d("Database", "Fetched: " + name);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DatabaseHelper.close(connection, statement, resultSet);
            }
        }
        return pendingInventoryList;
    }


    // 将待盘点记录入库
    public static boolean stockInProduct(int productId, String name, String batchNumber, double quantity, double price) {
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            try {
                connection.setAutoCommit(false);  // 开启事务

                // 查找是否存在相同批号的产品
                String selectQuery = "SELECT * FROM products WHERE num = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                selectStatement.setString(1, batchNumber);
                ResultSet resultSet = selectStatement.executeQuery();

                if (resultSet.next()) {
                    // 更新库存数量
                    String updateQuery = "UPDATE products SET stock = stock + ? WHERE num = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setDouble(1, quantity);
                    updateStatement.setString(2, batchNumber);
                    updateStatement.executeUpdate();
                } else {
                    // 插入新产品
                    String insertQuery = "INSERT INTO products (name, num, price, stock) VALUES (?, ?, ?, ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setString(1, name);
                    insertStatement.setString(2, batchNumber);
                    insertStatement.setDouble(3, price);
                    insertStatement.setDouble(4, quantity);
                    insertStatement.executeUpdate();
                }

                // 更新待入库表的 state 为 '入库'
                String updatePendingQuery = "UPDATE stockpending SET state = '入库' WHERE num = ?";
                PreparedStatement updatePendingStatement = connection.prepareStatement(updatePendingQuery);
                updatePendingStatement.setString(1, batchNumber);
                updatePendingStatement.executeUpdate();

                connection.commit();  // 提交事务
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    connection.rollback();  // 回滚事务
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            } finally {
                DatabaseHelper.close(connection, null, null);
            }
        }
        return false;
    }

    // 获取供应商名称
    public static String getSupplierName(int sid) {
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                String query = "SELECT name FROM suppliers WHERE id = ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, sid);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DatabaseHelper.close(connection, statement, resultSet);
            }
        }
        return "未知供应商"; // 默认返回值
    }
}