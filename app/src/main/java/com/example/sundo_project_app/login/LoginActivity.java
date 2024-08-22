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

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.project.AddbusinessActivity;
import com.example.sundo_project_app.utill.UrlManager;

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

    private static final String LOGIN_URL = UrlManager.BASE_URL + "/api/companies/login";
    private static final String VALIDATE_TOKEN_URL = UrlManager.BASE_URL + "/api/validate-token";

    private String companyName;

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

        String json = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> onLoginFailure());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    String token = extractTokenFromResponse(responseBody);
                    companyName = extractCompanyNameFromResponse(responseBody);
                    long companyCode = extractCompanyCodeFromResponse(responseBody);

                    if (autoLoginCheckbox.isChecked()) {
                        saveToken(token, companyName);
                    }

                    runOnUiThread(() -> {
                        saveToken(token,companyName);
                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, AddbusinessActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("companyCode", companyCode);
                        intent.putExtra("companyName", companyName);

                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "로그인 실패: 이메일 또는 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void saveToken(String token, String companyName) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("companyName", companyName);
        editor.putBoolean("auto_login", autoLoginCheckbox.isChecked()); // 자동 로그인 체크박스 상태 저장
        editor.apply();
        Log.d("SaveToken", "Token saved: " + token);
        Log.d("SaveToken", "Company Name saved: " + companyName);
    }

    private void checkAutoLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean autoLogin = sharedPreferences.getBoolean("auto_login", false);
        String token = sharedPreferences.getString("token", null);
        long storedCompanyCode = sharedPreferences.getLong("companyCode", -1);

        if (autoLogin && token != null && storedCompanyCode != -1) {
            validateToken(token, storedCompanyCode);
        } else {
            findViewById(R.id.emailInput).setVisibility(View.VISIBLE);
            findViewById(R.id.passwordInput).setVisibility(View.VISIBLE);
            findViewById(R.id.autoLoginCheckbox).setVisibility(View.VISIBLE);
            findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
            findViewById(R.id.signupButton).setVisibility(View.VISIBLE);
            findViewById(R.id.findEmailLink).setVisibility(View.VISIBLE);
            autoLoginCheckbox.setChecked(autoLogin); // 자동 로그인 체크박스 상태 설정
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
                runOnUiThread(() -> onTokenValidationFailure());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, AddbusinessActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("companyCode", companyCode);
                        intent.putExtra("companyName", companyName);
                        startActivity(intent);
                        finish();
                    } else {
                        onTokenValidationFailure();
                    }
                });
            }
        });
    }

    private void onLoginFailure() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("companyName");
        editor.remove("companyCode");
        editor.putBoolean("auto_login", false); // 자동 로그인 상태 비활성화
        editor.apply();
        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
        findViewById(R.id.emailInput).setVisibility(View.VISIBLE);
        findViewById(R.id.passwordInput).setVisibility(View.VISIBLE);
        findViewById(R.id.autoLoginCheckbox).setVisibility(View.VISIBLE);
        findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
        findViewById(R.id.signupButton).setVisibility(View.VISIBLE);
        findViewById(R.id.findEmailLink).setVisibility(View.VISIBLE);
        autoLoginCheckbox.setChecked(false); // 체크박스 상태 초기화
    }

    private void onTokenValidationFailure() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("companyName");
        editor.remove("companyCode");
        editor.putBoolean("auto_login", false); // 자동 로그인 상태 비활성화
        editor.apply();
        Toast.makeText(LoginActivity.this, "자동 로그인 실패", Toast.LENGTH_SHORT).show();
        findViewById(R.id.emailInput).setVisibility(View.VISIBLE);
        findViewById(R.id.passwordInput).setVisibility(View.VISIBLE);
        findViewById(R.id.autoLoginCheckbox).setVisibility(View.VISIBLE);
        findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
        findViewById(R.id.signupButton).setVisibility(View.VISIBLE);
        findViewById(R.id.findEmailLink).setVisibility(View.VISIBLE);
        autoLoginCheckbox.setChecked(false); // 체크박스 상태 초기화
    }

    private String extractTokenFromResponse(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private long extractCompanyCodeFromResponse(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getLong("companyCode");
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private String extractCompanyNameFromResponse(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getString("companyName");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("LoginActivity", "Error extracting companyName from response", e);
            return null;
        }
    }
}
