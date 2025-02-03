package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Stock;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StockRecordsFragment extends Fragment {

    private ListView lvStockRecords;
    private StockRecordAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_records, container, false);

        lvStockRecords = view.findViewById(R.id.lv_stock_records);

        // 自动加载入库产品记录
        new FetchStockRecordsTask().execute();

        return view;
    }

    private class FetchStockRecordsTask extends AsyncTask<Void, Void, List<StockRecord>> {
        @Override
        protected List<StockRecord> doInBackground(Void... voids) {
            return fetchStockRecords();
        }

        @Override
        protected void onPostExecute(List<StockRecord> stockRecords) {
            if (stockRecords != null && !stockRecords.isEmpty()) {
                adapter = new StockRecordAdapter(getContext(), stockRecords);
                lvStockRecords.setAdapter(adapter);
            }
        }

        private List<StockRecord> fetchStockRecords() {
            List<StockRecord> stockRecords = new ArrayList<>();
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT sp.*, s.name AS supplier_name FROM stockpending sp JOIN suppliers s ON sp.sid = s.id WHERE sp.state = '入库'";
                    PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        int sid = resultSet.getInt("sid");
                        String name = resultSet.getString("name");
                        String num = resultSet.getString("num");
                        double price = resultSet.getDouble("price");
                        double quantity = resultSet.getDouble("quantity");
                        String state = resultSet.getString("state");
                        String updatedAt = resultSet.getString("updated_at");
                        String supplierName = resultSet.getString("supplier_name"); // 获取供应商名称

                        StockRecord stockRecord = new StockRecord(id, sid, name, num, price, quantity, state, updatedAt, supplierName);
                        stockRecords.add(stockRecord);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return stockRecords;
        }
    }
}