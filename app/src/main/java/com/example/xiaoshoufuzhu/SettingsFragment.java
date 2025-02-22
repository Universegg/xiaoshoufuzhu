package com.example.xiaoshoufuzhu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// SettingsFragment.java
public class SettingsFragment extends Fragment {
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        loadUserData();
        return view;
    }

    private void loadUserData() {
        // 从SharedPreferences获取当前用户名
        SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        new LoadUserTask().execute(username);
    }

    private void updateUI() {
        if (getView() == null) {
            Log.e("UI", "View为null");
            return;
        }
        if (currentUser == null) {
            Log.e("UI", "currentUser为null");
            return;
        }

        Log.d("UI", "开始更新界面");

        try {
            // 基本信息
            TextView tvName = getView().findViewById(R.id.tv_name);
            TextView tvRole = getView().findViewById(R.id.tv_role);
            Log.d("UI", "姓名: " + currentUser.getName());
            Log.d("UI", "角色: " + currentUser.getRoleName());
            tvName.setText(currentUser.getName());
            tvRole.setText(currentUser.getRoleName());

            // 详细信息
            setInfoView(R.id.tv_username, currentUser.getUsername());
            setInfoView(R.id.tv_sex, currentUser.getSex());
            setInfoView(R.id.tv_age, String.valueOf(currentUser.getAge()));
            setInfoView(R.id.tv_email, currentUser.getEmail());
            setInfoView(R.id.tv_mobile, currentUser.getSafeMobile());
        } catch (Exception e) {
            Log.e("UI", "更新界面异常: " + e.getMessage());
        }
    }

    private void setInfoView(int viewId, String value) {
        try {
            InfoItemView view = getView().findViewById(viewId);
            if (view != null) {
                view.setInfoText(value);
                Log.d("UI", "设置视图 " + viewId + " 成功");
            } else {
                Log.e("UI", "找不到视图: " + viewId);
            }
        } catch (Exception e) {
            Log.e("UI", "设置视图错误: " + e.getMessage());
        }
    }

    private class LoadUserTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            Log.d("UserData", "正在查询用户: " + params[0]);
            User user = DatabaseHelper.getUserByUsername(params[0]);
            if (user != null) {
                Log.d("UserData", "获取到用户数据: " + user.toString());
            } else {
                Log.e("UserData", "用户数据为空");
            }
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                Log.d("UserData", "准备更新UI");
                currentUser = user;
                updateUI();
            } else {
                Log.e("UserData", "用户数据加载失败");
                Toast.makeText(getActivity(), "用户信息加载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}