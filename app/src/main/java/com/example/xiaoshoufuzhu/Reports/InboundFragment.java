package com.example.xiaoshoufuzhu.Reports;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InboundFragment extends Fragment {

    private TextView tvTotalQuantity;
    private TextView tvTotalPrice;
    private TextView tvFreight;
    private TextView tvTotalPayment;
    private Button btnPurchaseDetails;
    private Button btnFreightInfo;
    private ScrollView svPurchaseDetails;
    private ScrollView svFreightDetails;
    private LinearLayout llPurchaseDetails;
    private LinearLayout llFreightDetails;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbound, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图组件
        tvTotalQuantity = view.findViewById(R.id.tvCustomerQuantity);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvFreight = view.findViewById(R.id.tvFreight);
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment);
        btnPurchaseDetails = view.findViewById(R.id.btnPurchaseDetails);
        btnFreightInfo = view.findViewById(R.id.btnFreightInfo);
        svPurchaseDetails = view.findViewById(R.id.svPurchaseDetails);
        svFreightDetails = view.findViewById(R.id.svFreightDetails);
        llPurchaseDetails = view.findViewById(R.id.llPurchaseDetails);
        llFreightDetails = view.findViewById(R.id.llFreightDetails);

        // 获取传递过来的产品信息
        String productName = getActivity().getIntent().getStringExtra("productName");
        String productNum = getActivity().getIntent().getStringExtra("productNum");

        // 加载采购数据
        loadPurchaseData(productName, productNum);

        // 设置按钮点击监听
        btnPurchaseDetails.setOnClickListener(v -> toggleDetails(svPurchaseDetails, productName, productNum, true));
        btnFreightInfo.setOnClickListener(v -> toggleDetails(svFreightDetails, productName, productNum, false));
    }

    private void loadPurchaseData(String productName, String productNum) {
        new Thread(() -> {
            try (Connection connection = DatabaseHelper.getConnection()) {
                if (connection == null) {
                    showError("数据库连接失败");
                    return;
                }

                // 查询汇总数据
                String query = "SELECT SUM(quantity) AS total_quantity, " +
                        "SUM(total_price) AS total_price, " +
                        "SUM(freight) AS total_freight " +
                        "FROM records_suppliers " +
                        "WHERE name = ? AND num = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, productName);
                statement.setString(2, productNum);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    double totalQuantity = rs.getDouble("total_quantity");
                    double totalPrice = rs.getDouble("total_price");
                    double totalFreight = rs.getDouble("total_freight");
                    double totalPayment = totalPrice + totalFreight;

                    getActivity().runOnUiThread(() -> {
                        tvTotalQuantity.setText(String.format("采购总数: %.2f 斤", totalQuantity));
                        tvTotalPrice.setText(String.format("采购总金额: %.2f 元", totalPrice));
                        tvFreight.setText(String.format("运费: %.2f 元", totalFreight));
                        tvTotalPayment.setText(String.format("实付金额: %.2f 元", totalPayment));
                    });
                }
            } catch (SQLException e) {
                showError("数据库查询错误: " + e.getMessage());
            } catch (Exception e) {
                showError("系统错误: " + e.getMessage());
            }
        }).start();
    }

    private void toggleDetails(ScrollView scrollView, String productName, String productNum, boolean isPurchase) {
        if (scrollView.getVisibility() == View.VISIBLE) {
            scrollView.setVisibility(View.GONE);
        } else {
            if (isPurchase) {
                loadPurchaseDetails(productName, productNum);
            } else {
                loadFreightDetails(productName, productNum);
            }
            scrollView.setVisibility(View.VISIBLE);
        }
    }

    private void loadPurchaseDetails(String productName, String productNum) {
        new Thread(() -> {
            try (Connection connection = DatabaseHelper.getConnection()) {
                String query = "SELECT s.name AS supplier_name, rs.quantity, " +
                        "rs.price, rs.freight, rs.total_price, rs.purchase_date " +
                        "FROM records_suppliers rs " +
                        "LEFT JOIN suppliers s ON rs.sid = s.id " +
                        "WHERE rs.name = ? AND rs.num = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, productName);
                statement.setString(2, productNum);
                ResultSet rs = statement.executeQuery();

                List<PurchaseDetail> details = new ArrayList<>();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                while (rs.next()) {
                    String supplier = rs.getString("supplier_name");
                    double quantity = rs.getDouble("quantity");
                    double price = rs.getDouble("price");
                    double freight = rs.getDouble("freight");
                    double total = rs.getDouble("total_price");
                    String date = sdf.format(rs.getTimestamp("purchase_date"));

                    details.add(new PurchaseDetail(supplier, quantity, price, freight, total, date));
                }

                getActivity().runOnUiThread(() -> populatePurchaseDetails(details));
            } catch (Exception e) {
                showError("加载详情失败: " + e.getMessage());
            }
        }).start();
    }

    private void loadFreightDetails(String productName, String productNum) {
        new Thread(() -> {
            try (Connection connection = DatabaseHelper.getConnection()) {
                String query = "SELECT s.name AS supplier_name, rs.freight " +
                        "FROM records_suppliers rs " +
                        "LEFT JOIN suppliers s ON rs.sid = s.id " +
                        "WHERE rs.name = ? AND rs.num = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, productName);
                statement.setString(2, productNum);
                ResultSet rs = statement.executeQuery();

                List<FreightDetail> details = new ArrayList<>();

                while (rs.next()) {
                    String supplier = rs.getString("supplier_name");
                    double freight = rs.getDouble("freight");
                    details.add(new FreightDetail(supplier, freight));
                }

                getActivity().runOnUiThread(() -> populateFreightDetails(details));
            } catch (Exception e) {
                showError("加载运费失败: " + e.getMessage());
            }
        }).start();
    }

    private void populatePurchaseDetails(List<PurchaseDetail> details) {
        llPurchaseDetails.removeAllViews();
        for (PurchaseDetail detail : details) {
            addDetailView(llPurchaseDetails,
                    String.format("供应商: %s\n数量: %.2f斤\n单价: %.2f元\n运费: %.2f元\n总价: %.2f元\n时间: %s",
                            detail.supplier,
                            detail.quantity,
                            detail.price,
                            detail.freight,
                            detail.total,
                            detail.date));
        }
    }

    private void populateFreightDetails(List<FreightDetail> details) {
        llFreightDetails.removeAllViews();
        for (FreightDetail detail : details) {
            addDetailView(llFreightDetails,
                    String.format("供应商: %s\n运费: %.2f元",
                            detail.supplier,
                            detail.freight));
        }
    }

    private void addDetailView(LinearLayout layout, String text) {
        TextView tv = new TextView(getContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary));
        tv.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        tv.setBackgroundResource(R.drawable.bg_detail_item);
        tv.setText(text);
        layout.addView(tv);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics());
    }

    private void showError(String message) {
        getActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show());
    }

    // 数据类
    private static class PurchaseDetail {
        String supplier;
        double quantity;
        double price;
        double freight;
        double total;
        String date;

        PurchaseDetail(String supplier, double quantity, double price,
                       double freight, double total, String date) {
            this.supplier = supplier;
            this.quantity = quantity;
            this.price = price;
            this.freight = freight;
            this.total = total;
            this.date = date;
        }
    }

    private static class FreightDetail {
        String supplier;
        double freight;

        FreightDetail(String supplier, double freight) {
            this.supplier = supplier;
            this.freight = freight;
        }
    }
}