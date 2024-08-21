package com.example.sundo_project_app.project.api;

import com.example.sundo_project_app.project.model.Project;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProjectApi {

    // 특정 회사 코드에 해당하는 모든 프로젝트를 조회하는 메서드
    @GET("api/projects")
    Call<List<Project>> getProjects(@Query("companyCode") Long companyCode);

    // 새로운 프로젝트를 추가하는 메서드
    @POST("api/projects")
    Call<Project> createProject(@Body Project project);

    // 특정 ID의 프로젝트를 삭제하는 메서드
    @DELETE("api/projects/{id}")
    Call<Void> deleteProject(@Path("id") Long projectId);
}
