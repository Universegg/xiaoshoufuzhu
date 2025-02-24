package com.example.xiaoshoufuzhu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.xiaoshoufuzhu.Login.LoginActivity;


public class SettingsFragment extends Fragment {
    private User currentUser;
    private View rootView;
    private LoadUserTask loadUserTask;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initUserDataLoading();
        setupToolbarButtons();
        return rootView;
    }


    private void setupToolbarButtons() {
        // 设置按钮
        ImageView ivSettings = rootView.findViewById(R.id.iv_settings);
        ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT,
                    SettingsActivity.FRAGMENT_SYSTEM_SETTINGS);
            startActivity(intent);
        });

        // 退出按钮
        ImageView ivLogout = rootView.findViewById(R.id.iv_logout);
        ivLogout.setOnClickListener(v -> showLogoutDialog());

        // 编辑按钮
        ImageView ivEdit = rootView.findViewById(R.id.iv_edit);
        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void navigateToSettings() {
        startActivity(new Intent(requireActivity(), SettingsActivity.class));
    }

    private void initUserDataLoading() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        if (!username.isEmpty()) {
            loadUserTask = new LoadUserTask();
            loadUserTask.execute(username);
        } else {
            handleUserNotFound();
        }
    }

    private void updateUI() {
        if (rootView == null || currentUser == null) return;

        try {
            // 基本信息绑定
            TextView tvName = rootView.findViewById(R.id.tv_name);
            TextView tvRole = rootView.findViewById(R.id.tv_role);
            tvName.setText(currentUser.getName());
            tvRole.setText(currentUser.getRoleName());

            // 详细信息绑定
            bindInfoViews();
        } catch (Exception e) {
            Log.e("UI_UPDATE", "界面更新失败", e);
            showErrorMessage("数据加载异常");
        }
    }

    private void bindInfoViews() {
        setInfoView(R.id.tv_username, currentUser.getUsername());
        setInfoView(R.id.tv_sex, currentUser.getSex());
        setInfoView(R.id.tv_age, String.valueOf(currentUser.getAge()));
        setInfoView(R.id.tv_email, currentUser.getEmail());
        setInfoView(R.id.tv_mobile, currentUser.getSafeMobile());
    }

    private void setInfoView(int viewId, String value) {
        InfoItemView view = rootView.findViewById(viewId);
        if (view != null) {
            view.setValue(value != null ? value : "未设置");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadUserTask != null && !loadUserTask.isCancelled()) {
            loadUserTask.cancel(true);
        }
        rootView = null;
    }

    // 异步任务优化
    private class LoadUserTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            try {
                return DatabaseHelper.getUserByUsername(params[0]);
            } catch (Exception e) {
                Log.e("DB_ERROR", "用户查询失败", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user == null) {
                handleUserNotFound();
                return;
            }
            currentUser = user;
            updateUI();
        }
    }

    private void handleUserNotFound() {
        showErrorMessage("用户信息不存在");
        requireActivity().onBackPressed();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 必须保留
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("确认退出")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出", (dialog, which) -> performLogout())
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        // 清除登录状态
        SharedPreferences prefs = requireActivity().getSharedPreferences(
                "user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // 跳转登录页
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

}