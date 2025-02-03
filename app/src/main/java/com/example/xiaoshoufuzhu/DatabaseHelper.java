package com.example.xiaoshoufuzhu;

import android.util.Log;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    private static final String URL = "jdbc:mysql://172.20.10.7:3306/agri_sales?characterEncoding=utf-8";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() {
        java.sql.Connection connection = null;
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.jdbc.Driver");//加载驱动
            // 获取数据库连接
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            Log.d("Database", "Connection successful");
            // 检查数据库是否成功连接
            if (checkConnection(connection)) {
                Log.d("Database", "Database is connected successfully.");
            } else {
                Log.e("Database", "Failed to verify database connection.");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch(SQLException e){
            e.printStackTrace();
        }
        return connection;
    }
    // 检查数据库连接是否成功
    private static boolean checkConnection(Connection connection) {
        try {
            if (connection != null) {
                Statement statement = connection.createStatement();
                // 执行一个简单的查询以验证连接
                statement.executeQuery("SELECT 1");
                return true;  // 如果查询成功，连接有效
            } else {
                return false;  // 如果连接为空，则说明连接无效
            }
        } catch (SQLException e) {
            Log.e("Database", "Failed to execute test query", e);
            return false;  // 如果查询失败，返回连接无效
        }
    }


    // 关闭数据库资源
    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            Log.e("Database", "Failed to close resources", e);
        }
    }



}
