package com.example.xiaoshoufuzhu.SalesManagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Spinner;
import android.widget.TextView;

public class SalesDataReceiver extends BroadcastReceiver {
    private TextView tvCustomerDetails;
    private Spinner spnCustomers;

    public SalesDataReceiver(TextView tvCustomerDetails, Spinner spnCustomers) {
        this.tvCustomerDetails = tvCustomerDetails;
        this.spnCustomers = spnCustomers;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 从 intent 中获取新的数据并更新采购商详细信息
        String customerDetails = intent.getStringExtra("customer_details");
        tvCustomerDetails.setText(customerDetails);

        // 你可以在这里执行更多操作，比如刷新其他相关的视图
    }
}