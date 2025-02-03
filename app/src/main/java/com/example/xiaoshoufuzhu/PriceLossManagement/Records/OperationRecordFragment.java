package com.example.xiaoshoufuzhu.PriceLossManagement.Records;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.xiaoshoufuzhu.PriceLossManagement.Records.Excepiton.ExceptionRecordsFragment;
import com.example.xiaoshoufuzhu.PriceLossManagement.Records.Loss.LossRecordsFragment;
import com.example.xiaoshoufuzhu.PriceLossManagement.Records.Stock.StockRecordsFragment;
import com.example.xiaoshoufuzhu.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OperationRecordFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operation_record, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new OperationRecordPagerAdapter(getActivity()));

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("库存记录");
                        break;
                    case 1:
                        tab.setText("损耗记录");
                        break;
                    case 2:
                        tab.setText("异常记录");
                        break;
                }
            }
        }).attach();

        return view;
    }

    private static class OperationRecordPagerAdapter extends FragmentStateAdapter {

        public OperationRecordPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new StockRecordsFragment();
                case 1:
                    return new LossRecordsFragment();
                case 2:
                    return new ExceptionRecordsFragment();
                default:
                    return new StockRecordsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}