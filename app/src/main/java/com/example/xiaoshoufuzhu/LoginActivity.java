package com.example.xiaoshoufuzhu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xiaoshoufuzhu.Home.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化UI组件
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 设置点击登录按钮后的行为
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到主界面（MainActivity）
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // 可选：关闭当前活动界面，防止返回登录界面
                finish();
            }
        });
    }
}
