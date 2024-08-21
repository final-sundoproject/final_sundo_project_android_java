package com.example.sundo_project_app.location;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.evaluation.EvaluationActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DdActivity extends AppCompatActivity {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private EditText etlatitude;
    private EditText etlongitude;
    private Button btnSubmit;
    private String projectId; // projectId를 클래스 변수로 변경
    private String locationId;

    private static final String TAG = "DdActivity";
    private static final String SERVER_URL = "http://172.30.1.94:8000/location"; // 변경할 서버 URL

    private Serializable currentProject;
    private String registerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_dd_input);

        etlatitude = findViewById(R.id.et_latitude);
        etlongitude = findViewById(R.id.et_longitude);
        btnSubmit = findViewById(R.id.btn_submit);

        // X 버튼을 눌렀을 때 창을 닫는 기능 추가
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());


        // Intent에서 위도, 경도, projectId 가져오기
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        projectId = intent.getStringExtra("project_id"); // Intent에서 projectId를 가져옴

        currentProject =  intent.getSerializableExtra("currentProject");
        registerName = intent.getStringExtra("registerName");

        Log.d("currentProject: {}", String.valueOf(currentProject));
        Log.d("registerName: {}", registerName);


        etlatitude.setText(String.valueOf(latitude));
        etlongitude.setText(String.valueOf(longitude));

        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        try {
            double latitude = Double.parseDouble(etlatitude.getText().toString());
            double longitude = Double.parseDouble(etlongitude.getText().toString());

            // 입력값 검증
            if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
                Toast.makeText(this, "위도는 " + MIN_LATITUDE + "와 " + MAX_LATITUDE + " 사이의 값만 입력할 수 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
                Toast.makeText(this, "경도는 " + MIN_LONGITUDE + "와 " + MAX_LONGITUDE + " 사이의 값만 입력할 수 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버로 전송할 데이터 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);

            // projectId가 존재할 경우에만 포함
            if (projectId != null) {
                jsonObject.put("projectId", projectId);
            }

            // 서버로 데이터 전송
            sendCoordinates(jsonObject.toString());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "숫자를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCoordinates(String jsonData) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "jsonData: " + jsonData);
        Log.d(TAG, "projectId: " + (projectId != null ? projectId : "null"));

        executor.execute(() -> {
            String result = null;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                // URL에 projectId를 포함시키기
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

                    // 서버로부터 locationId 수신 (중복된 경우 "DUPLICATE"라는 문자열을 반환한다고 가정)
                    if ("DUPLICATE".equals(response)) {
                        result = "입력하신 좌표는 이미 존재합니다.";
                    } else {
                        result = "좌표가 성공적으로 전송되었습니다.";
                        locationId = response; // 응답으로부터 locationId를 설정
                    }
                } else {
                    result = "서버 오류가 발생했습니다. 응답 코드: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                result = "전송 중 오류가 발생했습니다: " + e.getMessage();
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

            String finalResult = result;
            handler.post(() -> {
                Toast.makeText(DdActivity.this, finalResult, Toast.LENGTH_LONG).show();
                if (locationId != null) {
                    Intent intent = new Intent(DdActivity.this, GeneratorActivity.class);

                    Intent DdIntent = new Intent(DdActivity.this, EvaluationActivity.class);
                    intent.putExtra("locationId", locationId);
                    DdIntent.putExtra("locationId",locationId);
                    Log.d("locaitonId","locaitonId: "+locationId);

                    intent.putExtra("locationId", locationId); // locationId를 전달
                    intent.putExtra("currentProject",currentProject);
                    intent.putExtra("registerName",registerName);
                    Log.d("locationId","locationId: "+locationId);

                    startActivity(intent);

                }
            });
        });
    }
}

