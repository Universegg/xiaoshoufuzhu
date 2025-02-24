package com.example.xiaoshoufuzhu;

import android.util.Log;

import com.example.xiaoshoufuzhu.Login.PasswordUtils;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

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


    public static boolean validateUser(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            String sql = "SELECT password FROM sales.sales_user WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedHash = resultSet.getString("password");
                return PasswordUtils.checkPassword(password, storedHash);
            }
            return false;
        } catch (SQLException e) {
            Log.e("DB", "验证错误: " + e.getErrorCode());
            return false;
        } finally {
            close(connection, statement, resultSet);
        }
    }

    // DatabaseHelper.java 优化查询方法
    public static User getUserByUsername(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            String sql = "SELECT id, name, sex, age, email, mobile, user_type " +
                    "FROM sales.sales_user WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("id"),
                        username,
                        resultSet.getString("name"),
                        resultSet.getString("sex"),
                        resultSet.getInt("age"),
                        resultSet.getString("email"),
                        resultSet.getString("mobile"),
                        resultSet.getInt("user_type")
                );
                Log.d("DB", "用户数据解析成功: " + user.toString());
                return user;
            }
        } catch (SQLException e) {
            Log.e("DB", "查询错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(connection, statement, resultSet);
        }
        return null;
    }

    public static boolean updateUser(User user) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            String sql = "UPDATE sales.sales_user SET " +
                    "name = ?, " +
                    "sex = ?, " +
                    "age = ?, " +
                    "email = ?, " +
                    "mobile = ? " +
                    "WHERE id = ?";

            statement = connection.prepareStatement(sql);
            statement.setString(1, user.getName());
            statement.setString(2, user.getSex());
            statement.setInt(3, user.getAge());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getMobile());
            statement.setInt(6, user.getId());

            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Log.e("DB", "更新用户失败: " + e.getMessage());
            return false;
        } finally {
            close(connection, statement, null);
        }
    }
}
