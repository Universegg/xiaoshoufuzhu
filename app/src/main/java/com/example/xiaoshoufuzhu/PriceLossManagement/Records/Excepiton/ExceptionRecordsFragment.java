package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Excepiton;

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

public class ExceptionRecordsFragment extends Fragment {

    private ListView lvExceptionRecords;
    private ExceptionRecordAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exception_records, container, false);

        lvExceptionRecords = view.findViewById(R.id.lv_exception_records);

        // 自动加载异常记录
        new FetchExceptionRecordsTask().execute();

        return view;
    }

    private class FetchExceptionRecordsTask extends AsyncTask<Void, Void, List<ExceptionRecord>> {
        @Override
        protected List<ExceptionRecord> doInBackground(Void... voids) {
            return fetchExceptionRecords();
        }

        @Override
        protected void onPostExecute(List<ExceptionRecord> exceptionRecords) {
            if (exceptionRecords != null && !exceptionRecords.isEmpty()) {
                adapter = new ExceptionRecordAdapter(getContext(), exceptionRecords);
                lvExceptionRecords.setAdapter(adapter);
            }
        }

        private List<ExceptionRecord> fetchExceptionRecords() {
            List<ExceptionRecord> exceptionRecords = new ArrayList<>();
            Connection connection = DatabaseHelper.getConnection();
            if (connection != null) {
                try {
                    String query = "SELECT a.*, s.name AS supplier_name FROM abnormal a JOIN suppliers s ON a.sid = s.id";
                    PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        String num = resultSet.getString("num");
                        double quantity = resultSet.getDouble("quantity");
                        double price = resultSet.getDouble("price");
                        String date = resultSet.getString("date");
                        String reason = resultSet.getString("reason");
                        int sid = resultSet.getInt("sid");
                        String supplierName = resultSet.getString("supplier_name"); // 获取供应商名称

                        ExceptionRecord exceptionRecord = new ExceptionRecord(id, name, num, quantity, price, date, reason, sid, supplierName);
                        exceptionRecords.add(exceptionRecord);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return exceptionRecords;
        }
    }
}