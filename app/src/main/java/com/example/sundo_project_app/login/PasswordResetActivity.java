package com.example.sundo_project_app.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sundo_project_app.R;
import com.example.sundo_project_app.utill.UrlManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PasswordResetActivity extends AppCompatActivity {

    private EditText passwordResetInput;
    private EditText passwordResetInputCheck;
    private Button passwordResetButton;

    private static final String RESET_PASSWORD_URL = UrlManager.BASE_URL +"/api/companies/reset-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        passwordResetInput = findViewById(R.id.passwordResetInput);
        passwordResetInputCheck = findViewById(R.id.passwordResetInputCheck);
        passwordResetButton = findViewById(R.id.passwordFindButton);

        // 이전 액티비티로부터 companyEmail을 전달받음
        Intent intent = getIntent();
        String companyEmail = intent.getStringExtra("companyEmail");

        // companyEmail 로그 출력
        Log.d("PasswordResetActivity", "Received companyEmail: " + companyEmail);

        passwordResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = passwordResetInput.getText().toString();
                String newPasswordCheck = passwordResetInputCheck.getText().toString();

                if (newPassword.isEmpty() || newPasswordCheck.isEmpty()) {
                    Toast.makeText(PasswordResetActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(newPasswordCheck)) {
                    Toast.makeText(PasswordResetActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                } else {
                    resetPassword(companyEmail, newPassword);
                }
            }
        });
    }

    private void resetPassword(String companyEmail, String newPassword) {
        Log.d("PasswordReset", "Resetting password for email: " + companyEmail); // 이메일 로그 확인

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("companyEmail", companyEmail);
            jsonObject.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url(RESET_PASSWORD_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PasswordResetActivity.this, "비밀번호 재설정 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("PasswordReset", "Response code: " + response.code());
                Log.d("PasswordReset", "Response body: " + responseBody);

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PasswordResetActivity.this, "비밀번호 재설정 성공", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(PasswordResetActivity.this, "비밀번호 재설정 실패: " + responseBody, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
