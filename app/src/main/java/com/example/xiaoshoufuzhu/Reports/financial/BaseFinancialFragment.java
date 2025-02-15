package com.example.xiaoshoufuzhu.Reports.financial;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.xiaoshoufuzhu.R;

import java.util.Calendar;

public abstract class BaseFinancialFragment extends Fragment {
    protected Spinner spinnerTimePeriod;
    protected ImageView ivDatePicker;
    protected Calendar selectedDate;
    protected String selectedTimePeriod;
    protected View rootView;
    protected ProgressBar progressBar;
    protected TextView tvError;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_financial_base, container, false);
        initTimeControls();
        return rootView;
    }

    private void initTimeControls() {
        spinnerTimePeriod = rootView.findViewById(R.id.spinner_time_period);
        ivDatePicker = rootView.findViewById(R.id.ivDatePicker);
        progressBar = rootView.findViewById(R.id.progress_bar);
        tvError = rootView.findViewById(R.id.tv_error);

        selectedDate = Calendar.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_period_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimePeriod.setAdapter(adapter);

        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = parent.getItemAtPosition(position).toString();
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimePeriod = "按日";
            }
        });

        ivDatePicker.setOnClickListener(v -> showDatePicker());
    }

    protected String getDateCondition(String tablePrefix) {
        String dateColumn = "sale_date"; // 默认字段

        // 采购相关表使用purchase_date
        if (tablePrefix.equals("rs")) {
            dateColumn = "purchase_date";
        }

        // 处理表别名
        String qualifiedColumn = tablePrefix.isEmpty() ?
                dateColumn :
                tablePrefix + "." + dateColumn;

        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH) + 1;
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        switch (selectedTimePeriod) {
            case "按年":
                return String.format("YEAR(%s) = %d", qualifiedColumn, year);
            case "按月":
                return String.format("YEAR(%s) = %d AND MONTH(%s) = %d",
                        qualifiedColumn, year, qualifiedColumn, month);
            default:
                return String.format("DATE(%s) = '%04d-%02d-%02d'",
                        qualifiedColumn, year, month, day);
        }
    }

    private boolean shouldUsePurchaseDate() {
        return this instanceof CashFlowFragment || this instanceof BalanceSheetFragment;
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            selectedDate.set(year, month, day);
            loadData();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    protected void showLoading(boolean show) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            tvError.setVisibility(View.GONE);
        });
    }

    protected void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        });
    }

    protected abstract void loadData();
}