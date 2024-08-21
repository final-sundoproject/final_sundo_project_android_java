package com.example.sundo_project_app.regulatedArea;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sundo_project_app.R;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.List;

public class GeocodeActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://naveropenapi.apigw.ntruss.com/";
    private static final String CLIENT_ID = "nbpr5wd89w";  // 실제 클라이언트 ID로 변경
    private static final String CLIENT_SECRET = "TY1vleeHFzeowIHh789eAEsIC4jOLGIxNK0lmytH";  // 실제 클라이언트 Secret으로 변경
    private List<String> addresses;
    private TextView textViewResult;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocode);

        textViewResult = findViewById(R.id.textViewGeocodeInfo);

        // OkHttpClient와 Retrofit 초기화
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                                .addHeader("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        addresses = getIntent().getStringArrayListExtra("addresses");
        Log.d("GeocodeActivity", "Received addresses: " + addresses);
        if (addresses != null) {
            for (String address : addresses) {
                getCoordinates(address);
            }
        } else {
            Log.d("GeocodeActivity", "No addresses received.");
        }
    }

    private void getCoordinates(String address) {
        NaverGeocodeApi api = retrofit.create(NaverGeocodeApi.class);
        Call<NaverGeocodeResponse> call = api.getCoordinates(address);
        call.enqueue(new Callback<NaverGeocodeResponse>() {
            @Override
            public void onResponse(Call<NaverGeocodeResponse> call, Response<NaverGeocodeResponse> response) {
                if (response.isSuccessful()) {
                    NaverGeocodeResponse geocodeResponse = response.body();
                    if (geocodeResponse != null) {
                        // 응답의 전체 내용을 로그로 출력
                        Log.d("GeocodeActivity", "Geocode Response: " + geocodeResponse.toString());

                        // addresses 배열이 비어있는지 확인
                        if (geocodeResponse.getAddresses() != null && geocodeResponse.getAddresses().length > 0) {
                            StringBuilder result = new StringBuilder();
                            for (NaverGeocodeResponse.Address address : geocodeResponse.getAddresses()) {
                                result.append("Longitude: ").append(address.getLongitude())
                                        .append(", Latitude: ").append(address.getLatitude())
                                        .append("\n");
                            }
                            textViewResult.append(result.toString());
                        } else {
                            textViewResult.append("No addresses found.\n");
                        }
                    } else {
                        Log.d("GeocodeActivity", "Response body is null");
                        textViewResult.setText("Response body is null.");
                    }
                } else {
                    Log.d("GeocodeActivity", "API call failed with code: " + response.code() +
                            ", message: " + response.message());
                    textViewResult.setText("API call failed.");
                }
            }

            @Override
            public void onFailure(Call<NaverGeocodeResponse> call, Throwable t) {
                Log.e("GeocodeActivity", "API call failed", t);
                textViewResult.setText("API call failed.");
            }
        });
    }
}
