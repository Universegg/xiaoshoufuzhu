package com.example.xiaoshoufuzhu.Home;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("v7/weather/now")
    Call<WeatherResponse> getWeather(
            @Query("location") String location,
            @Query("key") String apiKey,
            @Query("unit") String unit
    );
}