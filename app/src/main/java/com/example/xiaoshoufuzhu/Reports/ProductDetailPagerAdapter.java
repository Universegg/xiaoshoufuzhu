package com.example.xiaoshoufuzhu.Reports;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProductDetailPagerAdapter extends FragmentStateAdapter {
    public ProductDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OutboundFragment();
            case 1:
                return new InboundFragment();
            default:
                return new OutboundFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}