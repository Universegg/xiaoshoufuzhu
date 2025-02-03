package com.example.xiaoshoufuzhu.SalesManagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.SalesManagement.model.Customer;

import java.util.List;

public class CustomerAdapter extends ArrayAdapter<Customer> {
    private int resource;

    // 原有的构造函数
    public CustomerAdapter(Context context, List<Customer> customers) {
        super(context, 0, customers);
        this.resource = R.layout.list_item_customer; // 默认使用的布局
    }

    // 新增的构造函数，允许传入布局资源 ID
    public CustomerAdapter(Context context, int resource, List<Customer> customers) {
        super(context, resource, customers);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Customer customer = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        }

        // 获取视图中的元素
        TextView tvName = convertView.findViewById(R.id.tvCustomerName);
        TextView tvPhone = convertView.findViewById(R.id.tvCustomerPhone);
        TextView tvAddress = convertView.findViewById(R.id.tvCustomerAddress);
        TextView tvAmount = convertView.findViewById(R.id.tvCustomerAmount);

        // 设置数据
        if (customer != null) {
            if (tvName != null) {
                tvName.setText(customer.getName());
            }
            if (tvPhone != null) {
                tvPhone.setText(customer.getPhone());
            }
            if (tvAddress != null) {
                tvAddress.setText(customer.getAddress());
            }
            if (tvAmount != null) {
                tvAmount.setText(String.format("赊账金额: %.2f", customer.getAmount()));
            }
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // 如果需要设置下拉菜单的视图，可以在这里进行自定义
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        Customer customer = getItem(position);
        TextView textView = convertView.findViewById(android.R.id.text1);
        if (customer != null && textView != null) {
            textView.setText(customer.getName());
        }

        return convertView;
    }
}