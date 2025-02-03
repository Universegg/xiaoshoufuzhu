package com.example.xiaoshoufuzhu.PriceLossManagement.CurrentStock;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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

public class CurrentStockFragment extends Fragment {

    private ListView lvProducts;
    private ProductAdapter productAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public CurrentStockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_stock, container, false);

        lvProducts = view.findViewById(R.id.lv_products);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchProductsTask().execute();
            }
        });

        new FetchProductsTask().execute();

        return view;
    }

    private class FetchProductsTask extends AsyncTask<Void, Void, List<Product>> {
        @Override
        protected List<Product> doInBackground(Void... voids) {
            return fetchProducts();
        }

        @Override
        protected void onPostExecute(List<Product> productList) {
            if (productList != null) {
                if (productAdapter == null) {
                    productAdapter = new ProductAdapter(getContext(), productList);
                    lvProducts.setAdapter(productAdapter);
                } else {
                    productAdapter.updateProducts(productList);
                }
            } else {
                Toast.makeText(getContext(), "无法获取产品信息", Toast.LENGTH_SHORT).show();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private List<Product> fetchProducts() {
        List<Product> productList = new ArrayList<>();
        Connection connection = DatabaseHelper.getConnection();
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                String query = "SELECT * FROM products";
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    double stock = resultSet.getDouble("stock");
                    String num = resultSet.getString("num");

                    Product product = new Product(id, name, price, stock, num);
                    productList.add(product);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "产品获取失败", Toast.LENGTH_SHORT).show();
            }
        }
        return productList;
    }
}