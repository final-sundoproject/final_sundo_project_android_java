package com.example.sundo_project_app.location;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.evaluation.EvaluationActivity;

import com.example.sundo_project_app.project.model.Project;
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

public class GeneratorActivity extends AppCompatActivity {

    private EditText doosanAngleEditText;
    private EditText unisonAngleEditText;
    private Button doosanButton;
    private Button unisonButton;
    private ExecutorService executorService;
    private String locationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generator);

        doosanAngleEditText = findViewById(R.id.doosan_angle_edit_text);
        unisonAngleEditText = findViewById(R.id.unison_angle_edit_text);
        doosanButton = findViewById(R.id.doosan_select_button);
        unisonButton = findViewById(R.id.unison_select_button);


        // X 버튼을 눌렀을 때 창을 닫는 기능 추가
        findViewById(R.id.close_button).setOnClickListener(v -> finish());


        // Intent에서 locationId 받기
        locationId = getIntent().getStringExtra("locationId");
        if (locationId != null) {
            Log.d("GeneratorActivity", "Received locationId: " + locationId);
        } else {
            Log.d("GeneratorActivity", "locationId is null");
        }



        executorService = Executors.newSingleThreadExecutor();

        doosanButton.setOnClickListener(v -> {
            String angleStr = doosanAngleEditText.getText().toString();
            if (isValidAngle(angleStr)) {
                double directionAngle = Double.parseDouble(angleStr);
                sendDataToServer("DOOSAN", directionAngle);
            } else {
                Toast.makeText(GeneratorActivity.this, "유효한 각도를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        unisonButton.setOnClickListener(v -> {
            String angleStr = unisonAngleEditText.getText().toString();
            if (isValidAngle(angleStr)) {
                double directionAngle = Double.parseDouble(angleStr);
                sendDataToServer("UNISON", directionAngle);
            } else {
                Toast.makeText(GeneratorActivity.this, "유효한 각도를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidAngle(String angleStr) {
        try {
            double directionAngle = Double.parseDouble(angleStr);
            return directionAngle >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendDataToServer(String generatorType, double directionAngle) {
        executorService.execute(() -> {
            try {
                // JSON 데이터 생성
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("generatorName", generatorType);
                jsonObject.put("directionAngle", directionAngle);

                // locationId가 존재할 경우에만 포함
                if (locationId != null) {
                    jsonObject.put("locationId", locationId);
                }

                String registerName = getIntent().getStringExtra("registerName");
                Serializable currentProject = getIntent().getSerializableExtra("currentProject");

                // URL 설정
                URL url = new URL(UrlManager.BASE_URL + "/generator" + (locationId != null ? "/" + locationId : "/"+0));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setDoOutput(true);

                // 데이터 전송
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonObject.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // 응답 처리
                int responseCode = connection.getResponseCode();
                Log.d("GeneratorActivity", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String response = responseBuilder.toString();
                    Log.d("GeneratorActivity", "Response from server: " + response);

                    // Success - start EvaluationActivity with bundled data
                    runOnUiThread(() -> {
                        Toast.makeText(GeneratorActivity.this, generatorType + " 발전기가 선택되었습니다. 각도: " + directionAngle, Toast.LENGTH_SHORT).show();
                        startEvaluationActivity(generatorType, directionAngle, locationId, currentProject, registerName);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(GeneratorActivity.this, "서버 오류 발생. 응답 코드: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(GeneratorActivity.this, "전송 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void startEvaluationActivity(String generatorType, double directionAngle, String locationId, Serializable currentProject, String registerName) {
        Intent intent = new Intent(GeneratorActivity.this, EvaluationActivity.class);
        intent.putExtra("generatorType", generatorType);
        intent.putExtra("directionAngle", directionAngle);
        intent.putExtra("locationId", locationId);

        intent.putExtra("currentProject", currentProject);
        intent.putExtra("registerName", registerName);

        Log.d("GeneratorActivity", "currentProject: " + currentProject);


        Log.d("GeneratorActivity", "generatorType: " + generatorType);
        Log.d("GeneratorActivity", "directionAngle: " + directionAngle);
        Log.d("GeneratorActivity", "locationId: " + locationId);

        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
