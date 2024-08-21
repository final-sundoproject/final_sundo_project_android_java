package com.example.sundo_project_app.evaluation;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import okhttp3.ResponseBody;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @GET("evaluation/foundAll")
    Call<ResponseBody> getAllEvaluations();

    @Multipart
    @POST("evaluation")
    Call<String> submitEvaluation(
            @Part MultipartBody.Part arImage,
            @Part("title") RequestBody title,
            @Part("registrantName") RequestBody registrantName,
            @Part("windVolume") RequestBody windVolume,
            @Part("noiseLevel") RequestBody noiseLevel,
            @Part("scenery") RequestBody scenery,
            @Part("waterDepth") RequestBody waterDepth
    );

    @DELETE("evaluation/{id}")
    Call<ResponseBody> deleteEvaluation(@Path("id") long id);


}
