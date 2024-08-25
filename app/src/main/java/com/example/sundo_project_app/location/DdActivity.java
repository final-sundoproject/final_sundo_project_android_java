package com.example.sundo_project_app.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.utill.UrlManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DdActivity extends Activity {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private String projectId;
    private String locationId;
    private Serializable currentProject;
    private String registerName;

    private static final String TAG = "DdActivity";
    private static final String SERVER_URL = UrlManager.BASE_URL + "/location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_dd_input); // 다이얼로그 레이아웃 설정


        // UI 요소 초기화
        EditText etLatitude = findViewById(R.id.et_latitude);
        EditText etLongitude = findViewById(R.id.et_longitude);
        Button btnSubmit = findViewById(R.id.btn_submit);
        TextView btnClose = findViewById(R.id.btn_close);

        // Intent로부터 데이터 추출
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        projectId = intent.getStringExtra("project_id");
        currentProject = intent.getSerializableExtra("currentProject");
        registerName = intent.getStringExtra("registerName");

        // 데이터 로그 출력
        Log.d(TAG, "currentProject: " + currentProject);
        Log.d(TAG, "registerName: " + registerName);

        // 입력 필드에 초기값 설정
        etLatitude.setText(String.valueOf(latitude));
        etLongitude.setText(String.valueOf(longitude));

        // 닫기 버튼 클릭 리스너
        btnClose.setOnClickListener(v -> finish());

        // 제출 버튼 클릭 리스너
        btnSubmit.setOnClickListener(v -> handleSubmit(etLatitude, etLongitude));
    }

    private void handleSubmit(EditText etLatitude, EditText etLongitude) {
        try {
            double latitude = Double.parseDouble(etLatitude.getText().toString());
            double longitude = Double.parseDouble(etLongitude.getText().toString());

            // 유효성 검사
            if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
                showToast("위도는 " + MIN_LATITUDE + "와 " + MAX_LATITUDE + "사이여야 합니다.");
                return;
            }

            if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
                showToast("경도는 " + MIN_LONGITUDE + "와 " + MAX_LONGITUDE + "사이여야 합니다.");
                return;
            }

            // JSON 데이터 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("registerName", registerName);

            if (projectId != null) {
                jsonObject.put("projectId", projectId);
            }

            // 서버로 데이터 전송
            sendCoordinates(jsonObject.toString());

        } catch (NumberFormatException e) {
            showToast("유효한 숫자를 입력하시오.");
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
            showToast("An error occurred: " + e.getMessage());
        }
    }

    private void sendCoordinates(String jsonData) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "jsonData: " + jsonData);

        executor.execute(() -> {
            String result = null;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(SERVER_URL + (projectId != null ? "/" + projectId : ""));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setDoOutput(true);

                // 데이터 전송
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // 서버 응답 처리
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String response = responseBuilder.toString();
                    Log.d(TAG, "Response from server: " + response);

                    if ("DUPLICATE".equals(response)) {
                        result = "The coordinates you entered already exist.";
                    } else {
                        result = "Coordinates successfully submitted.";
                        locationId = response;
                    }
                } else {
                    result = "Server error occurred. Response code: " + responseCode;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);
                result = "An error occurred while sending data: " + e.getMessage();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing reader: " + e.getMessage());
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            final String finalResult = result;
            handler.post(() -> {
                showToast(finalResult);
                if (locationId != null) {
                    Intent intent = new Intent(DdActivity.this, GeneratorActivity.class);
                    intent.putExtra("locationId", locationId);
                    intent.putExtra("currentProject", currentProject);
                    intent.putExtra("registerName", registerName);
                    startActivity(intent);
                }
                finish();
            });
        });
    }

    private void showToast(String message) {
        Toast.makeText(DdActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
