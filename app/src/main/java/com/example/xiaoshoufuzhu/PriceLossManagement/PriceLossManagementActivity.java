package com.example.xiaoshoufuzhu.PriceLossManagement;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.xiaoshoufuzhu.PriceLossManagement.CurrentStock.CurrentStockFragment;
import com.example.xiaoshoufuzhu.PriceLossManagement.ProductCheck.ProductCheckFragment;
import com.example.xiaoshoufuzhu.PriceLossManagement.Records.OperationRecordFragment;
import com.example.xiaoshoufuzhu.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PriceLossManagementActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_loss_management);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new ScreenSlidePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("目前库存");
                                break;
                            case 1:
                                tab.setText("产品盘点");
                                break;
                            case 2:
                                tab.setText("操作记录");
                                break;
                        }
                    }
                }).attach();
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new CurrentStockFragment();
                case 1:
                    return new ProductCheckFragment();
                case 2:
                    return new OperationRecordFragment();
                default:
                    return new CurrentStockFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}