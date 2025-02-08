package com.example.xiaoshoufuzhu.Home;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.xiaoshoufuzhu.PriceLossManagement.PriceLossManagementActivity;
import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.Reports.ReportViewActivity;
import com.example.xiaoshoufuzhu.SalesManagement.SalesManagementActivity;
import com.example.xiaoshoufuzhu.SalesPriceAnalysisActivity;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.SupplierSalesActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    // 修改后的组件声明
    private MaterialCardView cardPriceLoss, cardSales, cardPurchase,
            cardReport, cardAnalysis;
    private TextView tvCitySearchResults, tvWeather, tvWarning;
    private ImageView ivWeatherIcon, ivAlarm;
    private CardView weatherWarningCard;

    private static final String API_KEY = "e6bf9f59547e4b6aa2fd44c48f05638c";
    private static final String BASE_URL_CITY = "https://geoapi.qweather.com/";
    private static final String BASE_URL_WEATHER = "https://devapi.qweather.com/";

    private WeatherResponse weatherResponse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化新组件
        cardPriceLoss = view.findViewById(R.id.card_price_loss);
        cardSales = view.findViewById(R.id.card_sales);
        cardPurchase = view.findViewById(R.id.card_purchase);
        cardReport = view.findViewById(R.id.card_report);
        cardAnalysis = view.findViewById(R.id.card_analysis);

        tvCitySearchResults = view.findViewById(R.id.tvCitySearchResults);
        tvWeather = view.findViewById(R.id.tvWeather);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        tvWarning = view.findViewById(R.id.tvWarning);
        weatherWarningCard = view.findViewById(R.id.weatherWarningCard);

        // 设置卡片点击事件
        cardPriceLoss.setOnClickListener(v -> navigateTo(PriceLossManagementActivity.class));
        cardSales.setOnClickListener(v -> navigateTo(SalesManagementActivity.class));
        cardPurchase.setOnClickListener(v -> navigateTo(SupplierSalesActivity.class));
        cardReport.setOnClickListener(v -> navigateTo(ReportViewActivity.class));
        cardAnalysis.setOnClickListener(v -> navigateTo(SalesPriceAnalysisActivity.class));

        // 天气信息点击监听
        View.OnClickListener weatherClickListener = v -> {
            if (weatherResponse != null) {
                showWeatherDialog(weatherResponse);
            }
        };
        tvWeather.setOnClickListener(weatherClickListener);
        ivWeatherIcon.setOnClickListener(weatherClickListener);

        // 获取天气数据
        fetchWeatherData("上海");

        return view;
    }

    private void navigateTo(Class<?> cls) {
        startActivity(new Intent(getActivity(), cls));
    }

    private void fetchWeatherData(String location) {
        Retrofit retrofitCity = new Retrofit.Builder()
                .baseUrl(BASE_URL_CITY)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CitySearchApi citySearchApi = retrofitCity.create(CitySearchApi.class);
        Call<CitySearchResponse> call = citySearchApi.searchCity(location, API_KEY, 10, "cn");

        call.enqueue(new Callback<CitySearchResponse>() {
            @Override
            public void onResponse(Call<CitySearchResponse> call,
                                   Response<CitySearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleCityResponse(response.body());
                } else {
                    updateCityUI("城市数据获取失败");
                }
            }

            @Override
            public void onFailure(Call<CitySearchResponse> call, Throwable t) {
                updateCityUI("网络连接异常");
            }
        });
    }

    private void handleCityResponse(CitySearchResponse response) {
        List<CitySearchResponse.Location> locations = response.getLocation();
        if (!locations.isEmpty()) {
            CitySearchResponse.Location city = locations.get(0);
            updateCityUI(city.getName());
            fetchWeatherDetails(city.getId());
        } else {
            updateCityUI("未找到相关城市");
        }
    }

    private void updateCityUI(String text) {
        tvCitySearchResults.setText(text);
    }

    private void fetchWeatherDetails(String locationId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = retrofit.create(WeatherApi.class);
        Call<WeatherResponse> call = api.getWeather(locationId, API_KEY, "m");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call,
                                   Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherResponse = response.body();
                    updateWeatherUI();
                } else {
                    updateWeatherError();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                updateWeatherError();
            }
        });
    }

    private void updateWeatherUI() {
        try {
            WeatherResponse.Now now = weatherResponse.getNow();
            String status = now.getText() + " " + now.getTemp() + "°C";
            tvWeather.setText(status);
            setWeatherIcon(ivWeatherIcon, now.getText());
            updateWarnings(now);
        } catch (Exception e) {
            tvWeather.setText("天气数据解析错误");
            weatherWarningCard.setVisibility(View.GONE);
        }
    }

    private void updateWarnings(WeatherResponse.Now now) {
        StringBuilder warnings = new StringBuilder();

        try {
            double temp = Double.parseDouble(now.getTemp());
            double humidity = Double.parseDouble(now.getHumidity());
            double windSpeed = Double.parseDouble(now.getWindSpeed());
            double precip = Double.parseDouble(now.getPrecip());

            if (temp > 35) warnings.append("• 高温预警\n");
            if (temp < 0) warnings.append("• 低温预警\n");
            if (humidity > 90) warnings.append("• 高湿预警\n");
            if (windSpeed > 50) warnings.append("• 强风预警\n");
            if (precip > 50) warnings.append("• 暴雨预警\n");

            if (warnings.length() > 0) {
                weatherWarningCard.setVisibility(View.VISIBLE);
                tvWarning.setText(warnings.toString().trim());
            } else {
                weatherWarningCard.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            weatherWarningCard.setVisibility(View.GONE);
        }
    }

    private void updateWeatherError() {
        tvWeather.setText("天气获取失败");
        weatherWarningCard.setVisibility(View.GONE);
    }

    private void showWeatherDialog(WeatherResponse weatherResponse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_weather_detail, null);
        builder.setView(dialogView);

        // 初始化视图
        ImageView ivIcon = dialogView.findViewById(R.id.ivDetailIcon);
        TextView tvTemp = dialogView.findViewById(R.id.tvTemp);
        TextView tvFeelsLike = dialogView.findViewById(R.id.tvFeelsLike);
        TextView tvWeatherStatus = dialogView.findViewById(R.id.tvWeatherStatus);
        TextView tvWindDir = dialogView.findViewById(R.id.tvWindDir);
        TextView tvWindSpeed = dialogView.findViewById(R.id.tvWindSpeed);
        TextView tvHumidity = dialogView.findViewById(R.id.tvHumidity);
        TextView tvPrecip = dialogView.findViewById(R.id.tvPrecip);
        TextView tvPressure = dialogView.findViewById(R.id.tvPressure);
        TextView tvVisibility = dialogView.findViewById(R.id.tvVisibility);
        TextView tvCloud = dialogView.findViewById(R.id.tvCloud);
        TextView tvDewPoint = dialogView.findViewById(R.id.tvDewPoint);

        // 设置天气图标
        setWeatherIcon(ivIcon, weatherResponse.getNow().getText());

        // 填充数据
        tvTemp.setText(weatherResponse.getNow().getTemp() + "°C");
        tvFeelsLike.setText(weatherResponse.getNow().getFeelsLike() + "°C");
        tvWeatherStatus.setText(weatherResponse.getNow().getText());
        tvWindDir.setText(weatherResponse.getNow().getWindDir());
        tvWindSpeed.setText(weatherResponse.getNow().getWindSpeed() + " km/h");
        tvHumidity.setText(weatherResponse.getNow().getHumidity() + "%");
        tvPrecip.setText(weatherResponse.getNow().getPrecip() + " mm");
        tvPressure.setText(weatherResponse.getNow().getPressure() + " hPa");
        tvVisibility.setText(weatherResponse.getNow().getVis() + " km");
        tvCloud.setText(weatherResponse.getNow().getCloud() + "%");
        tvDewPoint.setText(weatherResponse.getNow().getDew() + "°C");

        AlertDialog dialog = builder.create();
        dialog.show();

        // 设置窗口背景透明
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void setWeatherIcon(ImageView imageView, String weatherText) {
        int resId = R.drawable.ic_sunny; // 默认图标
        if (weatherText.contains("晴")) {
            resId = R.drawable.ic_sunny;
        } else if (weatherText.contains("云")) {
            resId = R.drawable.ic_cloudy;
        } else if (weatherText.contains("雨")) {
            resId = R.drawable.ic_rain;
        }
        imageView.setImageResource(resId);
    }
}