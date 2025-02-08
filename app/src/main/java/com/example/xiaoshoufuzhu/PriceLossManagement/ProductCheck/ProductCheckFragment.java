package com.example.xiaoshoufuzhu.PriceLossManagement.ProductCheck;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.example.xiaoshoufuzhu.R;
import java.util.List;
import java.util.ArrayList;

public class ProductCheckFragment extends Fragment {

    private ListView lvStockPending;
    private Button btnFetch, btnOneKeyStock;
    private ProductCheckAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_check, container, false);

        lvStockPending = view.findViewById(R.id.lv_stock_pending);
        btnFetch = view.findViewById(R.id.btn_fetch);
        btnOneKeyStock = view.findViewById(R.id.btn_one_key_stock);

        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchRecordsTask().execute();
            }
        });

        btnOneKeyStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OneKeyStockInTask().execute();
            }
        });

        // 自动加载待盘点产品
        new FetchPendingInventoryTask().execute();

        return view;
    }

    private class FetchRecordsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            InventoryManager.fetchRecordsToPendingInventory();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new FetchPendingInventoryTask().execute();  // 拉取记录后刷新列表
        }
    }

    private class FetchPendingInventoryTask extends AsyncTask<Void, Void, List<PendingInventory>> {
        @Override
        protected List<PendingInventory> doInBackground(Void... voids) {
            List<PendingInventory> pendingInventories = InventoryManager.fetchPendingInventory();
            Log.d("FetchTask", "Fetched " + pendingInventories.size() + " items");
            return pendingInventories;
        }

        @Override
        protected void onPostExecute(List<PendingInventory> pendingInventories) {
            if (pendingInventories != null && !pendingInventories.isEmpty()) {
                if (adapter == null) {
                    adapter = new ProductCheckAdapter(getContext(), pendingInventories);
                    lvStockPending.setAdapter(adapter);
                } else {
                    adapter.updatePendingInventories(pendingInventories);
                }
                Log.d("FetchTask", "Adapter set with " + pendingInventories.size() + " items");
            } else {
                Toast.makeText(getContext(), "所有产品都已入库！", Toast.LENGTH_SHORT).show();
                Log.d("FetchTask", "Failed to fetch items or no items fetched");
            }
        }
    }

    private class OneKeyStockInTask extends AsyncTask<Void, Void, Boolean> {
        private List<PendingInventory> inventoriesToRemove = new ArrayList<>();

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            if (adapter != null) {
                for (PendingInventory inventory : adapter.getPendingInventories()) {
                    boolean result = InventoryManager.stockInProduct(inventory.getProductId(), inventory.getName(), inventory.getBatchNumber(), inventory.getQuantity(), inventory.getPrice());
                    if (result) {
                        inventoriesToRemove.add(inventory);  // 记录成功入库的产品
                    }
                    success &= result;
                }
            } else {
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(getContext(), "所有产品已入库", Toast.LENGTH_SHORT).show();
                for (PendingInventory inventory : inventoriesToRemove) {
                    adapter.removePendingInventory(inventory);
                }
                new FetchPendingInventoryTask().execute();  // 刷新待盘点列表
            } else {
                Toast.makeText(getContext(), "入库操作失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}