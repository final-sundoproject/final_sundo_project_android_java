package com.example.sundo_project_app.regulatedArea;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NaverGeocodeApi {
    @GET("map-geocode/v2/geocode")
    Call<NaverGeocodeResponse> getCoordinates(@Query("query") String address);
}
