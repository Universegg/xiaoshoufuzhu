package com.example.xiaoshoufuzhu.Home;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.xiaoshoufuzhu.PriceLossManagement.PriceLossManagementActivity;
import com.example.xiaoshoufuzhu.R;
import com.example.xiaoshoufuzhu.Reports.ReportViewActivity;
import com.example.xiaoshoufuzhu.SalesManagement.SalesManagementActivity;
import com.example.xiaoshoufuzhu.SalesPriceAnalysisActivity;
import com.example.xiaoshoufuzhu.SupplierSalesManagement.SupplierSalesActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private FrameLayout framePriceLossManagement, frameSalesManagement, framePurchaseManagement,
            frameReportView, frameSalesPriceAnalysis;
    private TextView tvCitySearchResults, tvWeather, tvWarning;
    private ImageView ivWeatherIcon, ivAlarm;
    private LinearLayout weatherWarningLayout;

    private static final String API_KEY = "e6bf9f59547e4b6aa2fd44c48f05638c";
    private static final String BASE_URL_CITY = "https://geoapi.qweather.com/";
    private static final String BASE_URL_WEATHER = "https://devapi.qweather.com/";

    private WeatherResponse weatherResponse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化框架和TextView
        framePriceLossManagement = view.findViewById(R.id.framePriceLossManagement);
        frameSalesManagement = view.findViewById(R.id.frameSalesManagement);
        framePurchaseManagement = view.findViewById(R.id.framePurchaseManagement);
        frameReportView = view.findViewById(R.id.frameReportView);
        frameSalesPriceAnalysis = view.findViewById(R.id.frameSalesPriceAnalysis);
        tvCitySearchResults = view.findViewById(R.id.tvCitySearchResults);
        tvWeather = view.findViewById(R.id.tvWeather);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        tvWarning = view.findViewById(R.id.tvWarning);
        ivAlarm = view.findViewById(R.id.ivAlarm);
        weatherWarningLayout = view.findViewById(R.id.weatherWarningLayout);

        // 设置框架点击事件
        framePriceLossManagement.setOnClickListener(v -> {
            // 跳转到价格与损耗管理模块
            Intent intent = new Intent(getActivity(), PriceLossManagementActivity.class);
            startActivity(intent);
        });

        frameSalesManagement.setOnClickListener(v -> {
            // 跳转到销售管理模块
            Intent intent = new Intent(getActivity(), SalesManagementActivity.class);
            startActivity(intent);
        });

        framePurchaseManagement.setOnClickListener(v -> {
            // 跳转到进货管理模块
            Intent intent = new Intent(getActivity(), SupplierSalesActivity.class);
            startActivity(intent);
        });

        frameReportView.setOnClickListener(v -> {
            // 跳转到报表查看模块
            Intent intent = new Intent(getActivity(), ReportViewActivity.class);
            startActivity(intent);
        });

        frameSalesPriceAnalysis.setOnClickListener(v -> {
            // 跳转到销量与价格动态分析模块
            Intent intent = new Intent(getActivity(), SalesPriceAnalysisActivity.class);
            startActivity(intent);
        });

        // 设置tvWeather和ivWeatherIcon点击事件
        View.OnClickListener weatherClickListener = v -> showWeatherDialog(weatherResponse);
        tvWeather.setOnClickListener(weatherClickListener);
        ivWeatherIcon.setOnClickListener(weatherClickListener);

        // 搜索城市并获取天气
        searchCityAndWeather("上海");

        return view;
    }

    private void searchCityAndWeather(String location) {
        Retrofit retrofitCity = new Retrofit.Builder()
                .baseUrl(BASE_URL_CITY)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CitySearchApi citySearchApi = retrofitCity.create(CitySearchApi.class);
        Call<CitySearchResponse> callCity = citySearchApi.searchCity(location, API_KEY, 10, "cn");

        callCity.enqueue(new Callback<CitySearchResponse>() {
            @Override
            public void onResponse(Call<CitySearchResponse> call, Response<CitySearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CitySearchResponse.Location> locations = response.body().getLocation();
                    if (!locations.isEmpty()) {
                        CitySearchResponse.Location city = locations.get(0);
                        String cityInfo = "城市: " + city.getName();
                        tvCitySearchResults.setText(cityInfo);

                        // 使用城市ID获取天气信息
                        getWeather(city.getId());
                    } else {
                        tvCitySearchResults.setText("未找到相关城市");
                    }
                } else {
                    tvCitySearchResults.setText("搜索城市失败");
                }
            }

            @Override
            public void onFailure(Call<CitySearchResponse> call, Throwable t) {
                tvCitySearchResults.setText("搜索城市失败");
            }
        });
    }

    private void getWeather(String locationId) {
        Retrofit retrofitWeather = new Retrofit.Builder()
                .baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofitWeather.create(WeatherApi.class);
        Call<WeatherResponse> callWeather = weatherApi.getWeather(locationId, API_KEY, "m");

        callWeather.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherResponse = response.body();
                    String weatherText = "温度: " + weatherResponse.getNow().getTemp() + "°C" +
                            "\n体感温度: " + weatherResponse.getNow().getFeelsLike() + "°C" +
                            "\n天气状况: " + weatherResponse.getNow().getText() +
                            "\n风向: " + weatherResponse.getNow().getWindDir() +
                            "\n风速: " + weatherResponse.getNow().getWindSpeed() + " km/h" +
                            "\n湿度: " + weatherResponse.getNow().getHumidity() + "%" +
                            "\n降水量: " + weatherResponse.getNow().getPrecip() + " mm" +
                            "\n气压: " + weatherResponse.getNow().getPressure() + " hPa" +
                            "\n能见度: " + weatherResponse.getNow().getVis() + " km" +
                            "\n云量: " + weatherResponse.getNow().getCloud() + "%" +
                            "\n露点: " + weatherResponse.getNow().getDew() + "°C";
                    tvWeather.setText("查看天气");

                    // 动态添加预警信息
                    addWeatherWarnings(weatherResponse);
                } else {
                    tvWeather.setText("获取天气数据失败");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvWeather.setText("获取天气数据失败");
            }
        });
    }

    private void addWeatherWarnings(WeatherResponse weatherResponse) {
        weatherWarningLayout.setVisibility(View.VISIBLE); // 默认显示预警信息

        try {
            double temp = Double.parseDouble(weatherResponse.getNow().getTemp());
            double humidity = Double.parseDouble(weatherResponse.getNow().getHumidity());
            double windSpeed = Double.parseDouble(weatherResponse.getNow().getWindSpeed());
            double precip = Double.parseDouble(weatherResponse.getNow().getPrecip());

            StringBuilder warnings = new StringBuilder();

            if (temp > 35) {
                warnings.append("高温预警：农产品易损耗\n");
            }
            if (temp < 0) {
                warnings.append("低温预警：农产品易冻伤\n");
            }
            if (humidity > 90) {
                warnings.append("高湿度预警：农产品易发霉\n");
            }
            if (windSpeed > 50) {
                warnings.append("强风预警：农产品易受损\n");
            }
            if (precip > 50) {
                warnings.append("强降雨预警：农产品易积水腐烂\n");
            }

            if (warnings.length() > 0) {
                tvWarning.setText(warnings.toString().trim());
            } else {
                tvWarning.setText("暂无预警信息");
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            tvWarning.setText("天气数据格式错误"); // 显示错误信息
        }
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