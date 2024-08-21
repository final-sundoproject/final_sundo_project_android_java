package com.example.sundo_project_app.regulatedArea;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sundo_project_app.R;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegulatedArea extends AppCompatActivity {

    private static final String BASE_URL = "https://apis.data.go.kr/1192000/";
    private static final String SERVICE_KEY = "xyigcn2H+16RENHs6SNbyOXjPjW0t0Tastu/ePEl3PW6jMKcyrxrFErPO4Rzc+GgV2G44DvWYE/HGIeUhEIxCw==";
    private static final int PAGE_NO = 1;
    private static final int NUM_OF_ROWS = 1000;

    private TextView TextViewResult;

    private List<String> addresses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_regulate_area);

        TextViewResult = findViewById(R.id.textViewInfo);

        // Retrofit 초기화
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        // API 인터페이스 생성
        RegulateApi api = retrofit.create(RegulateApi.class);

        // API 호출
        Call<RegulateResponse> call = api.getMarineProtectArea(SERVICE_KEY, PAGE_NO, NUM_OF_ROWS);
        call.enqueue(new Callback<RegulateResponse>() {
            @Override
            public void onResponse(Call<RegulateResponse> call, Response<RegulateResponse> response) {
                if (response.isSuccessful()) {
                    RegulateResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        List<RegulateResponse.Body.Item> items = apiResponse.getBody().getItems().getItemList();
                        if (items.isEmpty()) {
                            Log.d("RegulatedArea", "No items found in the response");
                            TextViewResult.setText("No items found in the response");
                        } else {
                            StringBuilder addressBuilder = new StringBuilder();
                            for (RegulateResponse.Body.Item item : items) {
                                String address = item.gethmpgNm();

                                String[] keywords = {"해양", "습지", "해역", "주변해역"};

                                int cutIndex = address.length();
                                for (String keyword : keywords) {
                                    int index = address.indexOf(keyword);
                                    if (index != -1 && index < cutIndex) {
                                        cutIndex = index;
                                    }
                                }

                                // 잘라낸 주소를 저장
                                address = address.substring(0, cutIndex).trim();
                                addresses.add(address);
                            }

                            // GeocodeActivity로 주소 목록 전달
                            Intent intent = new Intent(RegulatedArea.this, GeocodeActivity.class);
                            intent.putStringArrayListExtra("addresses", new ArrayList<>(addresses));
                            startActivity(intent);
                        }
                    } else {
                        Log.d("RegulatedArea", "Response body is null");
                        TextViewResult.setText("Response body is null");
                    }
                } else {
                    Log.d("RegulatedArea", "API call failed with code: " + response.code());
                    TextViewResult.setText("API call failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegulateResponse> call, Throwable t) {
                Log.e("RegulatedArea", "API call failed", t);
                TextViewResult.setText("API call failed: " + t.getMessage());
            }
        });
    }
}
