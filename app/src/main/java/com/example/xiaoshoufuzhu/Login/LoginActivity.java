package com.example.xiaoshoufuzhu.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ScrollView;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.Home.MainActivity;
import com.example.xiaoshoufuzhu.MyApplication;
import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SettingsAndUsers.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword, tvUserAgreement;
    private CheckBox cbAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化所有界面组件
        initializeViews();
        setupLoginButton();
        setupClickListeners();
    }

    private void initializeViews() {
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvUserAgreement = findViewById(R.id.tv_user_agreement);
        cbAgree = findViewById(R.id.cb_agree);
    }

    private void setupLoginButton() {
        // 默认禁用登录按钮
        btnLogin.setEnabled(false);

        // 勾选框状态监听
        cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnLogin.setEnabled(isChecked);
        });

        btnLogin.setOnClickListener(v -> {
            if (!cbAgree.isChecked()) {
                showToast("请先同意用户协议");
                return;
            }
            attemptLogin();
        });
    }

    private void setupClickListeners() {
        // 忘记密码点击事件
        tvForgotPassword.setOnClickListener(v -> showContactAdminDialog());

        // 用户协议点击事件
        tvUserAgreement.setOnClickListener(v -> showUserAgreementDialog());
    }

    // 显示联系管理员对话框
    private void showContactAdminDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.forgot_password_title))
                .setMessage(getString(R.string.contact_admin_message))
                .setPositiveButton(getString(R.string.ok_button), null)
                .show();
    }

    // 显示用户协议对话框（带滚动条）
    private void showUserAgreementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.user_agreement_title)
                .setPositiveButton(R.string.agree_button, null);

        // 创建带滚动条的文本内容
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(R.string.user_agreement_content);
        textView.setTextSize(16f);
        textView.setPadding(32, 32, 32, 32);

        scrollView.addView(textView);
        builder.setView(scrollView);
        builder.show();
    }

    // 原有登录逻辑保持不变
    private void attemptLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showToast(getString(R.string.empty_fields_warning));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        new LoginTask().execute(username, password);
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";
        private User loggedInUser;

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // 1. 验证用户凭证
                connection = DatabaseHelper.getConnection();
                String sql = "SELECT password, user_type FROM sales.sales_user WHERE username = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, username);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // 2. 密码验证
                    String storedHash = resultSet.getString("password");
                    boolean valid = PasswordUtils.checkPassword(password, storedHash);

                    if (valid) {
                        // 3. 获取完整用户信息（包含权限）
                        loggedInUser = DatabaseHelper.getUserByUsername(username);
                        if (loggedInUser == null) {
                            errorMessage = "用户信息获取失败";
                            return false;
                        }
                        // 4. 记录用户类型用于后续查询
                        int userType = resultSet.getInt("user_type");
                        loggedInUser.setUserType(userType);
                        return true;
                    }
                }
                errorMessage = "用户名或密码错误";
                return false;
            } catch (SQLException e) {
                Log.e("DB", "登录错误: " + e.getMessage());
                errorMessage = "数据库连接失败";
                return false;
            } catch (Exception e) {
                Log.e("APP", "未知错误: " + e.getMessage());
                errorMessage = "系统异常";
                return false;
            } finally {
                DatabaseHelper.close(connection, statement, resultSet);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            if (success) {
                handleLoginSuccess();
            } else {
                handleLoginFailure();
            }
        }

        private void handleLoginSuccess() {
            MyApplication.setCurrentUser(loggedInUser);
            String username = edtUsername.getText().toString();
            String successMsg = getString(R.string.login_success, username);
            showToast("\uD83C\uDF89 " + successMsg + " \uD83C\uDF89");

            if (loggedInUser != null && loggedInUser.getPermission() != null) {
                Log.d("LoginDebug", "用户权限已绑定: " + loggedInUser.getPermission().toString());
            } else {
                Log.e("LoginDebug", "⚠️ 用户权限未正确绑定");
            }


            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String storedUser = prefs.getString("username", "未找到");
            Log.d("Prefs", "存储的用户名: " + storedUser);

            prefs.edit().putString("username", edtUsername.getText().toString()).apply();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }, 500);
        }

        private void handleLoginFailure() {
            String message = !errorMessage.isEmpty() ?
                    getString(R.string.login_failed) + errorMessage :
                    getString(R.string.invalid_credentials);
            showToast(message);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        super.onDestroy();
    }
}