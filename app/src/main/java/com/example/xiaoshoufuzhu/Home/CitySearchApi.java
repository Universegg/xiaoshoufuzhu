package com.example.xiaoshoufuzhu.Home;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CitySearchApi {
    @GET("v2/city/lookup")
    Call<CitySearchResponse> searchCity(
            @Query("location") String location,
            @Query("key") String apiKey,
            @Query("number") int number,
            @Query("range") String range
    );
}