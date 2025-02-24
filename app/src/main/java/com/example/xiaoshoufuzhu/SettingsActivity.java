package com.example.xiaoshoufuzhu;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {
    private User currentUser;
    private MaterialToolbar toolbar;

    public static final String EXTRA_SHOW_FRAGMENT = "show_fragment";
    public static final int FRAGMENT_EDIT_PROFILE = 1;
    public static final int FRAGMENT_SYSTEM_SETTINGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initToolbar();
        checkFragmentType();
    }

    private void checkFragmentType() {
        int fragmentType = getIntent().getIntExtra(EXTRA_SHOW_FRAGMENT, FRAGMENT_EDIT_PROFILE);

        if (fragmentType == FRAGMENT_SYSTEM_SETTINGS) {
            loadSystemSettings();
        } else {
            loadUserDataAsync(); // 保持原有编辑功能
        }
    }

    private void loadSystemSettings() {
        // 直接加载设置界面
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsPreferenceFragment())
                .commit();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadSettingsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    private void loadUserDataAsync() {
        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String username = prefs.getString("username", "");
                return DatabaseHelper.getUserByUsername(username);
            }

            @Override
            protected void onPostExecute(User user) {
                if (user != null) {
                    currentUser = user;
                    // 用户数据加载成功后，替换为编辑界面
                    loadEditProfileFragment();
                } else {
                    handleUserLoadFailure();
                }
            }
        }.execute();
    }

    private void loadEditProfileFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.settings_container, new EditProfileFragment());
        transaction.commit();
    }

    private void handleUserLoadFailure() {
        Toast.makeText(this, "无法加载用户信息", Toast.LENGTH_SHORT).show();
        Log.e("SettingsActivity", "用户数据加载失败");
        finish(); // 关闭当前界面
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}