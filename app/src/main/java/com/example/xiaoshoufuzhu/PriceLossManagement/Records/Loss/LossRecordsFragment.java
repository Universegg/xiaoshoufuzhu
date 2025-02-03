package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Loss;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.xiaoshoufuzhu.DatabaseHelper;
import com.example.xiaoshoufuzhu.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LossRecordsFragment extends Fragment {

    private ListView lvLossRecords;
    private LossRecordAdapter lossRecordAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loss_records, container, false);

        lvLossRecords = view.findViewById(R.id.lv_loss_records);
        new FetchLossRecordsTask().execute();

        return view;
    }

    private class FetchLossRecordsTask extends AsyncTask<Void, Void, List<LossRecord>> {
        @Override
        protected List<LossRecord> doInBackground(Void... voids) {
            return fetchLossRecords();
        }

        @Override
        protected void onPostExecute(List<LossRecord> lossRecords) {
            if (lossRecords != null) {
                lossRecordAdapter = new LossRecordAdapter(getContext(), lossRecords);
                lvLossRecords.setAdapter(lossRecordAdapter);
            } else {
                Toast.makeText(getContext(), "无法获取损耗记录", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<LossRecord> fetchLossRecords() {
        List<LossRecord> lossRecords = new ArrayList<>();
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                String query = "SELECT l.id, l.product_id, l.quantity, l.loss_reason, l.loss_date, l.loss_amount, p.name AS product_name, p.num AS batch_number " +
                        "FROM losses l " +
                        "JOIN products p ON l.product_id = p.id";
                resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    int productId = resultSet.getInt("product_id");
                    double quantity = resultSet.getDouble("quantity");
                    String lossReason = resultSet.getString("loss_reason");
                    String lossDate = resultSet.getString("loss_date");
                    double lossAmount = resultSet.getDouble("loss_amount");
                    String productName = resultSet.getString("product_name");
                    String batchNumber = resultSet.getString("batch_number");

                    LossRecord lossRecord = new LossRecord(id, productId, quantity, lossReason, lossDate, lossAmount, productName, batchNumber);
                    lossRecords.add(lossRecord);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DatabaseHelper.close(connection, statement, resultSet);
            }
        }
        return lossRecords;
    }
}