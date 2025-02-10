package com.example.xiaoshoufuzhu.Reports.ProductLoss;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ProductLossReportAdapter extends ArrayAdapter<ProductLossRecord> {
    private static class ViewHolder {
        TextView tvProductName, tvBatchNumber, tvLossQuantity, tvLossAmount;
        ImageView ivDetail;
        LinearLayout llDetails, llDetailContent;
    }

    private int expandedPosition = -1;

    public ProductLossReportAdapter(Context context, List<ProductLossRecord> records) {
        super(context, 0, records);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProductLossRecord record = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_product_loss_report, parent, false);

            holder = new ViewHolder();
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvBatchNumber = convertView.findViewById(R.id.tvBatchNumber);
            holder.tvLossQuantity = convertView.findViewById(R.id.tvLossQuantity);
            holder.tvLossAmount = convertView.findViewById(R.id.tvLossAmount);
            holder.ivDetail = convertView.findViewById(R.id.ivDetail);
            holder.llDetails = convertView.findViewById(R.id.llDetails);
            holder.llDetailContent = convertView.findViewById(R.id.llDetailContent);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvProductName.setText(record.getProductName());
        holder.tvBatchNumber.setText(record.getBatchNumber());
        holder.tvLossQuantity.setText(String.valueOf(record.getLossQuantity()));
        holder.tvLossAmount.setText(String.format("¥%.2f", record.getLossAmount()));

        // 处理详情按钮点击
        holder.ivDetail.setOnClickListener(v -> handleDetailClick(position, holder, record));

        // 控制展开状态
        holder.llDetails.setVisibility(position == expandedPosition ? View.VISIBLE : View.GONE);

        return convertView;
    }

    private void handleDetailClick(int position, ViewHolder holder, ProductLossRecord record) {
        if (position == expandedPosition) {
            expandedPosition = -1;
            holder.llDetails.setVisibility(View.GONE);
        } else {
            int prev = expandedPosition;
            expandedPosition = position;
            notifyDataSetChanged();
            loadLossDetails(record, holder.llDetailContent);
        }
    }

    private void loadLossDetails(ProductLossRecord record, LinearLayout layout) {
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getConnection()) {
                ProductLossReportActivity activity = (ProductLossReportActivity) getContext();
                String sql = buildDetailSQL(activity);
                PreparedStatement stmt = conn.prepareStatement(sql);

                // 设置查询参数
                stmt.setString(1, record.getProductName());
                stmt.setString(2, record.getBatchNumber());
                setDateParams(stmt, activity);

                ResultSet rs = stmt.executeQuery();
                List<LossDetail> details = parseDetails(rs);

                activity.runOnUiThread(() -> showDetails(layout, details));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String buildDetailSQL(ProductLossReportActivity activity) {
        return "SELECT loss_date, quantity, loss_amount, loss_reason " +
                "FROM losses l " +
                "JOIN products p ON l.product_id = p.id " +
                "WHERE p.name = ? AND p.num = ? AND " + getDateCondition(activity);
    }

    private String getDateCondition(ProductLossReportActivity activity) {
        switch (activity.selectedTimePeriod) {
            case "按年": return "YEAR(loss_date) = ?";
            case "按月": return "YEAR(loss_date) = ? AND MONTH(loss_date) = ?";
            default: return "DATE(loss_date) = ?";
        }
    }

    private void setDateParams(PreparedStatement stmt, ProductLossReportActivity activity)
            throws SQLException {
        Calendar cal = activity.selectedDate;
        int paramIndex = 3;

        switch (activity.selectedTimePeriod) {
            case "按年":
                stmt.setInt(paramIndex, cal.get(Calendar.YEAR));
                break;
            case "按月":
                stmt.setInt(paramIndex++, cal.get(Calendar.YEAR));
                stmt.setInt(paramIndex, cal.get(Calendar.MONTH) + 1);
                break;
            default:
                String dateStr = String.format("%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));
                stmt.setString(paramIndex, dateStr);
        }
    }

    private List<LossDetail> parseDetails(ResultSet rs) throws SQLException {
        List<LossDetail> details = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        while (rs.next()) {
            Date date = rs.getDate("loss_date");
            details.add(new LossDetail(
                    sdf.format(date),
                    rs.getDouble("quantity"),
                    rs.getDouble("loss_amount"),
                    rs.getString("loss_reason")
            ));
        }
        return details;
    }

    private void showDetails(LinearLayout layout, List<LossDetail> details) {
        layout.removeAllViews();
        for (LossDetail detail : details) {
            TextView tv = new TextView(getContext());
            tv.setText(String.format("日期: %s\n数量: %.2f\n金额: ¥%.2f\n损耗原因: %s",
                    detail.date, detail.quantity, detail.amount, detail.reason));

            // 设置文本样式
            tv.setTextSize(14);
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            tv.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            tv.setBackgroundResource(R.drawable.bg_detail_item);

            layout.addView(tv);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics()
        );
    }

    private static class LossDetail {
        String date;
        double quantity;
        double amount;
        String reason;

        LossDetail(String date, double quantity, double amount, String reason) {
            this.date = date;
            this.quantity = quantity;
            this.amount = amount;
            this.reason = reason;
        }
    }
}