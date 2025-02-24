package com.example.xiaoshoufuzhu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class EditProfileFragment extends Fragment {
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View view) {
        // 获取当前用户
        currentUser = ((SettingsActivity)requireActivity()).getCurrentUser();

        // 初始化输入框
        InfoItemView etName = view.findViewById(R.id.et_name);
        InfoItemView etSex = view.findViewById(R.id.et_sex);
        InfoItemView etAge = view.findViewById(R.id.et_age);
        InfoItemView etEmail = view.findViewById(R.id.et_email);
        InfoItemView etMobile = view.findViewById(R.id.et_mobile);

        // 填充数据
        etName.setValue(currentUser.getName());
        etSex.setValue(currentUser.getSex());
        etAge.setValue(String.valueOf(currentUser.getAge()));
        etEmail.setValue(currentUser.getEmail());
        etMobile.setValue(currentUser.getMobile());

        // 设置可编辑
        etName.setEditable(true);
        etSex.setEditable(true);
        etAge.setEditable(true);
        etEmail.setEditable(true);
        etMobile.setEditable(true);

        // 保存按钮点击
        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveProfile(
                etName.getValue(),
                etSex.getValue(),
                etAge.getValue(),
                etEmail.getValue(),
                etMobile.getValue()
        ));
    }

    private void saveProfile(String name, String sex, String age, String email, String mobile) {
        // 输入验证
        if (name.isEmpty() || sex.isEmpty() || age.isEmpty()) {
            Toast.makeText(getContext(), "必填字段不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新用户对象
        currentUser.setName(name);
        currentUser.setSex(sex);
        currentUser.setAge(Integer.parseInt(age));
        currentUser.setEmail(email);
        currentUser.setMobile(mobile);

        new UpdateProfileTask().execute(currentUser);
    }

    private class UpdateProfileTask extends AsyncTask<User, Void, Boolean> {
        @Override
        protected Boolean doInBackground(User... users) {
            return DatabaseHelper.updateUser(users[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(getContext(), "资料更新成功", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(getContext(), "更新失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setTitle("编辑资料");
    }

}