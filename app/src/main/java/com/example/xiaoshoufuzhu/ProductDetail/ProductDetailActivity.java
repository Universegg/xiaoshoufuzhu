package com.example.xiaoshoufuzhu.ProductDetail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.xiaoshoufuzhu.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvProductName;
    private TextView tvProductNum;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        tvProductName = findViewById(R.id.tvProductName);
        tvProductNum = findViewById(R.id.tvProductNum);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // 获取传递过来的产品名称和编号
        String productName = getIntent().getStringExtra("productName");
        String productNum = getIntent().getStringExtra("productNum");

        // 设置 TextView 的内容
        TextView tvProductName = findViewById(R.id.tvProductName);
        TextView tvProductNum = findViewById(R.id.tvProductNum);
        tvProductName.setText(productName);
        tvProductNum.setText(productNum);

        // 设置 ViewPager 和 TabLayout
        viewPager.setAdapter(new ProductDetailPagerAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("出库");
                            break;
                        case 1:
                            tab.setText("入库");
                            break;
                    }
                }).attach();
    }
}