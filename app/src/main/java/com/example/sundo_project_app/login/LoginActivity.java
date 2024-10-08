package com.example.sundo_project_app.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sundo_project_app.MainActivity;
import com.example.sundo_project_app.R;
import com.example.sundo_project_app.project.AddbusinessActivity;
import com.example.sundo_project_app.login.PasswordFindActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private CheckBox autoLoginCheckbox;
    private Button loginButton;
    private Button signUpButton;

    private TextView findEmailLink;
    private TextView findPasswordLink;



    private Long companyCode;

    private static final String LOGIN_URL = "http://10.0.2.2:8000/api/companies/login"; // 서버의 로그인 엔드포인트
    private static final String VALIDATE_TOKEN_URL = "http://10.0.2.2:8000/api/validate-token"; // 서버의 토큰 검증 엔드포인트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        autoLoginCheckbox = findViewById(R.id.autoLoginCheckbox);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signupButton);
        findEmailLink = findViewById(R.id.findEmailLink);
        findPasswordLink = findViewById(R.id.findPasswordLink);

        findEmailLink = findViewById(R.id.findEmailLink);
        findPasswordLink = findViewById(R.id.findPasswordLink);


        // 자동 로그인 설정 확인
        checkAutoLogin();

        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            } else {
                login(email, password);
            }
        });

        signUpButton.setOnClickListener(view -> {
            // SignUpActivity로 이동
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        findEmailLink.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, EmailFindActivity.class);
            startActivity(intent);
        });

        findPasswordLink.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, PasswordFindActivity.class);
            startActivity(intent);
        });

    }

    private void login(String email, String password) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        // JSON 객체 생성
        String json = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        RequestBody body = RequestBody.create(JSON, json);

        // 서버로 요청 전송
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    String token = extractTokenFromResponse(responseBody);

                    companyCode = extractCompanyCodeFromResponse(responseBody); // companyCode 추출

                    if (autoLoginCheckbox.isChecked()) {
                        // 자동 로그인 설정을 저장
                        saveToken(token);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        // 메인 화면으로 이동
                        Intent intent = new Intent(LoginActivity.this, AddbusinessActivity.class);
                        intent.putExtra("companyCode", companyCode); // companyCode를 Intent에 추가
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "로그인 실패: 이메일 또는 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putLong("companyCode", companyCode != null ? companyCode : -1);
        editor.putBoolean("auto_login", true);
        editor.apply();
    }

    private void checkAutoLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean autoLogin = sharedPreferences.getBoolean("auto_login", false);
        String token = sharedPreferences.getString("token", null);
        long storedCompanyCode = sharedPreferences.getLong("companyCode", -1);

        Log.d("checkAutoLogin", "Stored companyCode: " + storedCompanyCode);

        if (autoLogin && token != null && storedCompanyCode != -1) {
            validateToken(token, storedCompanyCode);
        } else {
            // 로그인 화면의 구성 요소를 활성화하거나 비활성화
            findViewById(R.id.emailInput).setVisibility(View.VISIBLE);
            findViewById(R.id.passwordInput).setVisibility(View.VISIBLE);
            findViewById(R.id.autoLoginCheckbox).setVisibility(View.VISIBLE);
            findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
            findViewById(R.id.signupButton).setVisibility(View.VISIBLE);
            findViewById(R.id.findEmailLink).setVisibility(View.VISIBLE);
        }
    }

    private void validateToken(String token, long companyCode) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(VALIDATE_TOKEN_URL)
                .header("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TokenValidation", "Token validation failed", e);
                // 토큰 검증 실패: 자동 로그인 해제
                runOnUiThread(() -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("token");
                    editor.putBoolean("auto_login", false);
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "자동 로그인 실패", Toast.LENGTH_SHORT).show();
                    // 로그인 화면으로 이동
                    findViewById(R.id.emailInput).setVisibility(View.VISIBLE);
                    findViewById(R.id.passwordInput).setVisibility(View.VISIBLE);
                    findViewById(R.id.autoLoginCheckbox).setVisibility(View.VISIBLE);
                    findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.signupButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.findEmailLink).setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseBody = response.body().string();
                Log.d("TokenValidation", "Response body: " + responseBody);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // 토큰 검증 성공: AddbusinessActivity로 이동
                        Intent intent = new Intent(LoginActivity.this, AddbusinessActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("companyCode", companyCode);
                        startActivity(intent);
                        finish();
                    } else {
                        // 토큰 검증 실패: 자동 로그인 해제
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("token");
                        editor.putBoolean("auto_login", false);
                        editor.apply();
                        Toast.makeText(LoginActivity.this, "자동 로그인 실패", Toast.LENGTH_SHORT).show();
                        // 로그인 화면의 구성 요소를 활성화
                        findViewById(R.id.emailInput).setVisibility(View.VISIBLE);
                        findViewById(R.id.passwordInput).setVisibility(View.VISIBLE);
                        findViewById(R.id.autoLoginCheckbox).setVisibility(View.VISIBLE);
                        findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
                        findViewById(R.id.signupButton).setVisibility(View.VISIBLE);
                        findViewById(R.id.findEmailLink).setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private String extractTokenFromResponse(String responseBody) {
        // JSON 파싱을 사용하여 토큰을 추출
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private long extractCompanyCodeFromResponse(String responseBody) {
        // JSON 파싱을 사용하여 companyCode를 추출
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getLong("companyCode");
        } catch (JSONException e) {
            e.printStackTrace();
            return -1; // 기본값으로 잘못된 값을 반환
        }
    }
}
