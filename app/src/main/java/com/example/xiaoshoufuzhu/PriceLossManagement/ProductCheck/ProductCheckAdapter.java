package com.example.xiaoshoufuzhu.PriceLossManagement.ProductCheck;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

public class ProductCheckAdapter extends BaseAdapter {
    private Context context;
    private List<PendingInventory> pendingInventoryList;

    public ProductCheckAdapter(Context context, List<PendingInventory> pendingInventoryList) {
        this.context = context;
        this.pendingInventoryList = pendingInventoryList;
    }

    @Override
    public int getCount() {
        return pendingInventoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return pendingInventoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pending_inventory, parent, false);
        }

        PendingInventory inventory = pendingInventoryList.get(position);

        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvBatchNumber = convertView.findViewById(R.id.tv_batch_number);
        TextView tvQuantity = convertView.findViewById(R.id.tv_quantity);
        TextView tvPrice = convertView.findViewById(R.id.tv_price);
        TextView tvSupplier = convertView.findViewById(R.id.tv_supplier);
        Button btnAbnormal = convertView.findViewById(R.id.btn_abnormal);
        Button btnStockIn = convertView.findViewById(R.id.btn_stock_in);
        CheckBox checkBox = convertView.findViewById(R.id.checkbox);

        tvName.setText(inventory.getName());
        tvBatchNumber.setText(inventory.getBatchNumber());
        tvQuantity.setText(String.valueOf(inventory.getQuantity()));
        tvPrice.setText(String.valueOf(inventory.getPrice()));
        tvSupplier.setText(inventory.getSupplierName());

        btnAbnormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbnormalDialog(inventory);
            }
        });

        btnStockIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StockInTask().execute(inventory);
            }
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Add to selected items list
            } else {
                // Remove from selected items list
            }
        });

        return convertView;
    }

    public List<PendingInventory> getPendingInventories() {
        return pendingInventoryList;
    }

    public void updatePendingInventories(List<PendingInventory> newPendingInventories) {
        this.pendingInventoryList.clear();
        this.pendingInventoryList.addAll(newPendingInventories);
        notifyDataSetChanged();
    }

    public void removePendingInventory(PendingInventory inventory) {
        this.pendingInventoryList.remove(inventory);
        notifyDataSetChanged();
    }

    private void showAbnormalDialog(PendingInventory inventory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择异常类型");

        String[] types = {"损耗", "其他"};
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { // 损耗
                    showLossDialog(inventory);
                } else { // 其他
                    showOtherAbnormalDialog(inventory);
                }
            }
        });

        builder.show();
    }

    private void showLossDialog(PendingInventory inventory) {
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
                new RecordLossTask(inventory, quantity, reason).execute();
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

    private void showOtherAbnormalDialog(PendingInventory inventory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("记录其他异常");

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_abnormal, null);
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
                new RecordOtherAbnormalTask(inventory, quantity, reason).execute();
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
        private PendingInventory inventory;
        private double quantity;
        private String reason;

        public RecordLossTask(PendingInventory inventory, double quantity, String reason) {
            this.inventory = inventory;
            this.quantity = quantity;
            this.reason = reason;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return recordAbnormalAndUpdatePending(inventory, quantity, reason, "损耗");
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                inventory.setQuantity(inventory.getQuantity() - quantity);
                notifyDataSetChanged();
                Toast.makeText(context, "损耗已记录", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "损耗记录失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RecordOtherAbnormalTask extends AsyncTask<Void, Void, Boolean> {
        private PendingInventory inventory;
        private double quantity;
        private String reason;

        public RecordOtherAbnormalTask(PendingInventory inventory, double quantity, String reason) {
            this.inventory = inventory;
            this.quantity = quantity;
            this.reason = reason;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return recordAbnormalAndUpdatePending(inventory, quantity, reason, "其他");
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                inventory.setQuantity(inventory.getQuantity() - quantity);
                notifyDataSetChanged();
                Toast.makeText(context, "异常已记录", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "异常记录失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean recordAbnormalAndUpdatePending(PendingInventory inventory, double quantity, String reason, String type) {
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            ResultSet resultSet = null;
            try {
                connection.setAutoCommit(false);  // 开启事务

                // 获取产品的单价
                double price = inventory.getPrice();

                // 计算异常金额
                double abnormalAmount = price * quantity;

                // 插入异常记录
                String insertQuery = "INSERT INTO abnormal (name, num, quantity, price, date, reason, sid) VALUES (?, ?, ?, ?, NOW(), ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, inventory.getName());
                insertStatement.setString(2, inventory.getBatchNumber());
                insertStatement.setDouble(3, quantity);
                insertStatement.setDouble(4, price);
                insertStatement.setString(5, reason);
                insertStatement.setInt(6, inventory.getProductId());
                int rowsInserted = insertStatement.executeUpdate();

                // 更新待盘点产品数量
                String updateQuery = "UPDATE stockpending SET quantity = quantity - ?, updated_at = NOW() WHERE id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setDouble(1, quantity);
                updateStatement.setInt(2, inventory.getId());
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

    private class StockInTask extends AsyncTask<PendingInventory, Void, Boolean> {
        private PendingInventory inventory;

        @Override
        protected Boolean doInBackground(PendingInventory... params) {
            inventory = params[0];
            return stockInProduct(inventory);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(context, "产品已入库", Toast.LENGTH_SHORT).show();
                pendingInventoryList.remove(inventory);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "入库操作失败", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean stockInProduct(PendingInventory inventory) {
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false);  // 开启事务

                    // 更新产品状态为入库，并记录更新时间
                    String updateQuery = "UPDATE stockpending SET state = '入库', updated_at = NOW() WHERE id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setInt(1, inventory.getId());
                    int rowsUpdated = updateStatement.executeUpdate();

                    if (rowsUpdated > 0) {
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
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return false;
        }
    }
}