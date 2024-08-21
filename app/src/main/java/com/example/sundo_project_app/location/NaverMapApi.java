package com.example.sundo_project_app.location;

import android.app.Application;

import com.naver.maps.map.NaverMapSdk;

public class NaverMapApi extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("nbpr5wd89w"));
    }
}