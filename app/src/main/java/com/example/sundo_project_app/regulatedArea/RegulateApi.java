package com.example.sundo_project_app.regulatedArea;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RegulateApi {

    @GET("MarineProtectionAreaInfoService/MarineProtectionAreaInfo")
    Call<RegulateResponse> getMarineProtectArea(
            @Query("serviceKey") String serviceKey,
            @Query("pageNo") int pageNo,
            @Query("numOfRows") int numOfRows
    );
}
