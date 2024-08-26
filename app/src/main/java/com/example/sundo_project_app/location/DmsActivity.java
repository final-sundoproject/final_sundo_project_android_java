package com.example.sundo_project_app.location;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.evaluation.EvaluationActivity;
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

public class DmsActivity extends AppCompatActivity {

    private EditText etLatitudeDegrees, etLatitudeMinutes, etLatitudeSeconds, etLatitudeDirection;
    private EditText etLongitudeDegrees, etLongitudeMinutes, etLongitudeSeconds, etLongitudeDirection;
    private Button btnSubmit;
    private String projectId; // projectId를 멤버 변수로 선언

    private static final String TAG = "DmsActivity";
    private static final String SERVER_URL = UrlManager.BASE_URL + "/location"; // 변경할 서버 URL

    // locationId 변수를 멤버 변수로 선언
    private String locationId;

    private Serializable currentProject;
    private String registerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그를 모달로 표시
        showDmsDialog();
    }

    private void showDmsDialog() {
        // 다이얼로그에 사용할 레이아웃 인플레이터 생성
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_dms_input, null);

        // XML의 EditText 및 Button 참조
        etLatitudeDegrees = dialogView.findViewById(R.id.et_latitude_degrees);
        etLatitudeMinutes = dialogView.findViewById(R.id.et_latitude_minutes);
        etLatitudeSeconds = dialogView.findViewById(R.id.et_latitude_seconds);
        etLatitudeDirection = dialogView.findViewById(R.id.et_latitude_direction);

        etLongitudeDegrees = dialogView.findViewById(R.id.et_longitude_degrees);
        etLongitudeMinutes = dialogView.findViewById(R.id.et_longitude_minutes);
        etLongitudeSeconds = dialogView.findViewById(R.id.et_longitude_seconds);
        etLongitudeDirection = dialogView.findViewById(R.id.et_longitude_direction);

        btnSubmit = dialogView.findViewById(R.id.btn_submit);

        // 다이얼로그 빌더 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false); // 다이얼로그 외부를 클릭해도 닫히지 않도록 설정

        // X 버튼 클릭 시 다이얼로그 닫기
        dialogView.findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // 버튼 클릭 리스너 설정
        btnSubmit.setOnClickListener(v -> handleSubmit());

        // 다이얼로그 객체 생성 및 표시
        AlertDialog dialog = builder.create();
        dialog.show();

        // Intent에서 데이터 가져오기
        Intent intent = getIntent();
        projectId = intent.getStringExtra("project_id"); // Intent에서 projectId를 가져옴
        currentProject = intent.getSerializableExtra("currentProject");
        registerName = intent.getStringExtra("registerName");

        // 로그를 통해 projectId 확인
        Log.d(TAG, "Received projectId: " + projectId);
    }

    private void handleSubmit() {
        try {
            // 사용자가 입력한 값을 가져오기
            double latitudeDegrees = Double.parseDouble(etLatitudeDegrees.getText().toString());
            double latitudeMinutes = Double.parseDouble(etLatitudeMinutes.getText().toString());
            double latitudeSeconds = Double.parseDouble(etLatitudeSeconds.getText().toString());
            String latitudeDirection = etLatitudeDirection.getText().toString().toUpperCase();

            double longitudeDegrees = Double.parseDouble(etLongitudeDegrees.getText().toString());
            double longitudeMinutes = Double.parseDouble(etLongitudeMinutes.getText().toString());
            double longitudeSeconds = Double.parseDouble(etLongitudeSeconds.getText().toString());
            String longitudeDirection = etLongitudeDirection.getText().toString().toUpperCase();

            // 방향 값 검증 (N/S, E/W)
            if (!latitudeDirection.equals("N") && !latitudeDirection.equals("S")) {
                throw new IllegalArgumentException("잘못된 위도 방향입니다. 'N' 또는 'S'를 사용하시오.");
            }

            if (!longitudeDirection.equals("E") && !longitudeDirection.equals("W")) {
                throw new IllegalArgumentException("잘못된 위도 방향입니다. 'E' 또는 'W'를 사용하시오.");
            }

            // 서버로 전송할 데이터 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitudeDegrees", latitudeDegrees);
            jsonObject.put("latitudeMinutes", latitudeMinutes);
            jsonObject.put("latitudeSeconds", latitudeSeconds);
            jsonObject.put("latitudeDirection", latitudeDirection);
            jsonObject.put("longitudeDegrees", longitudeDegrees);
            jsonObject.put("longitudeMinutes", longitudeMinutes);
            jsonObject.put("longitudeSeconds", longitudeSeconds);
            jsonObject.put("longitudeDirection", longitudeDirection);

            // projectId가 존재할 경우에만 포함
            if (projectId != null && !projectId.isEmpty()) {
                jsonObject.put("projectId", projectId);
            } else {
                Log.e(TAG, "projectId is null");
            }

            // 서버로 데이터 전송
            sendCoordinates(jsonObject.toString());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "숫자를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCoordinates(String jsonData) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "jsonData: " + jsonData); // jsonData 값을 로그로 출력

        executor.execute(() -> {
            String result = null;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                // URL에 projectId를 포함시키기
                URL url = new URL(SERVER_URL + (projectId != null && !projectId.isEmpty() ? "/" + projectId : ""));
                Log.d(TAG, "Request URL: " + url.toString()); // 요청 URL 로그로 출력

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
                Log.d(TAG, "Response Code: " + responseCode); // 응답 코드 로그로 출력
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    locationId = responseBuilder.toString(); // 응답에서 locationId 가져오기
                    result = "좌표가 성공적으로 전송되었습니다.";
                } else {
                    result = "서버 오류가 발생했습니다. 응답 코드: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                result = "전송 중 오류가 발생했습니다: " + e.getMessage(); // 오류 메시지 포함
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

            // 메인 스레드에서 UI 업데이트
            String finalResult = result;
            handler.post(() -> {
                Toast.makeText(DmsActivity.this, finalResult, Toast.LENGTH_LONG).show();
                // 좌표 등록이 완료되면 GeneratorActivity로 이동
                if (locationId != null) {
                    Intent intent = new Intent(DmsActivity.this, GeneratorActivity.class);
                    intent.putExtra("locationId", locationId); // locationId를 전달
                    intent.putExtra("currentProject", currentProject);
                    intent.putExtra("registerName", registerName);
                    Log.d("currentProject", "currentProject: " + currentProject);
                    Log.d("registerName", "registerName: " + registerName);
                    Log.d("location", "locationID: " + locationId);
                    Log.d("locationId", "locationId: " + locationId); // jsonData 값을 로그로 출력
                    startActivity(intent);
                }
                finish();
            });
        });
    }

    private void showToast(String message) {
        Toast.makeText(DmsActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
