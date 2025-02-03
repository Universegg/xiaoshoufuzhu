package com.example.xiaoshoufuzhu.PriceLossManagement.CurrentStock;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        }

        Product product = productList.get(position);

        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvNum = convertView.findViewById(R.id.tv_num);
        EditText etPrice = convertView.findViewById(R.id.et_price);
        TextView tvStock = convertView.findViewById(R.id.tv_stock);
        Button btnUpdatePrice = convertView.findViewById(R.id.btn_update_price);
        Button btnLoss = convertView.findViewById(R.id.btn_loss);

        tvName.setText(product.getName());
        tvNum.setText(product.getNum());
        etPrice.setText(String.valueOf(product.getPrice()));
        tvStock.setText(String.valueOf(product.getStock()));

        btnUpdatePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double newPrice;
                try {
                    newPrice = Double.parseDouble(etPrice.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "价格输入无效", Toast.LENGTH_SHORT).show();
                    return;
                }

                new UpdatePriceTask(product, newPrice).execute();
            }
        });

        btnLoss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLossDialog(product);
            }
        });

        return convertView;
    }

    public void updateProducts(List<Product> productList) {
        this.productList.clear();
        this.productList.addAll(productList);
        notifyDataSetChanged();
    }

    private class UpdatePriceTask extends AsyncTask<Void, Void, Boolean> {
        private Product product;
        private double newPrice;

        public UpdatePriceTask(Product product, double newPrice) {
            this.product = product;
            this.newPrice = newPrice;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return updateProductPrice(product.getId(), newPrice);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                product.setPrice(newPrice);
                notifyDataSetChanged();
                Toast.makeText(context, "价格已更新", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "价格更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean updateProductPrice(int productId, double newPrice) {
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            try {
                String updateQuery = "UPDATE products SET price = ? WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setDouble(1, newPrice);
                preparedStatement.setInt(2, productId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void showLossDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("记录损耗");

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_loss, null);
        builder.setView(viewInflated);

        final EditText inputQuantity = viewInflated.findViewById(R.id.input_quantity);
        final EditText inputReason = viewInflated.findViewById(R.id.input_reason);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                double quantity;
                try {
                    quantity = Double.parseDouble(inputQuantity.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "数量输入无效", Toast.LENGTH_SHORT).show();
                    return;
                }
                String reason = inputReason.getText().toString();
                new RecordLossTask(product, quantity, reason).execute();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class RecordLossTask extends AsyncTask<Void, Void, Boolean> {
        private Product product;
        private double quantity;
        private String reason;

        public RecordLossTask(Product product, double quantity, String reason) {
            this.product = product;
            this.quantity = quantity;
            this.reason = reason;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return recordLossAndUpdateStock(product.getId(), quantity, reason);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                product.setStock(product.getStock() - quantity);
                notifyDataSetChanged();
                Toast.makeText(context, "损耗已记录", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "损耗记录失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean recordLossAndUpdateStock(int productId, double quantity, String reason) {
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            ResultSet resultSet = null;
            try {
                connection.setAutoCommit(false);  // 开启事务

                // 获取产品的单价
                double price = 0.0;
                String priceQuery = "SELECT price FROM products WHERE id = ?";
                PreparedStatement priceStatement = connection.prepareStatement(priceQuery);
                priceStatement.setInt(1, productId);
                resultSet = priceStatement.executeQuery();
                if (resultSet.next()) {
                    price = resultSet.getDouble("price");
                }

                // 计算损耗金额
                double lossAmount = price * quantity;

                // 插入损耗记录
                String insertQuery = "INSERT INTO losses (product_id, quantity, loss_reason, loss_amount, loss_date) VALUES (?, ?, ?, ?, NOW())";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setInt(1, productId);
                insertStatement.setDouble(2, quantity);
                insertStatement.setString(3, reason);
                insertStatement.setDouble(4, lossAmount);
                int rowsInserted = insertStatement.executeUpdate();

                // 更新产品库存
                String updateQuery = "UPDATE products SET stock = stock - ? WHERE id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setDouble(1, quantity);
                updateStatement.setInt(2, productId);
                int rowsUpdated = updateStatement.executeUpdate();

                if (rowsInserted > 0 && rowsUpdated > 0) {
                    connection.commit();  // 提交事务
                    return true;
                } else {
                    connection.rollback();  // 回滚事务
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    connection.rollback();  // 回滚事务
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            } finally {
                try {
                    connection.setAutoCommit(true);  // 重新开启自动提交
                    if (resultSet != null) {
                        resultSet.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }
}