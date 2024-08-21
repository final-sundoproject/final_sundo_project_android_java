package com.example.sundo_project_app.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sundo_project_app.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;

import java.io.IOException;

public class PasswordFindActivity extends AppCompatActivity {

    private EditText companyEmailInput;
    private EditText companyNameInput;
    private EditText companyAddressInput;
    private EditText businessNumberInput;
    private Button passwordFindButton;
    private TextView passwordCheckTextView;

    private static final String PASSWORD_FIND_URL = "http://10.0.2.2:8000/api/companies/find-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_find);

        companyEmailInput = findViewById(R.id.companyEmailInput);
        companyNameInput = findViewById(R.id.companyNameInput);
        companyAddressInput = findViewById(R.id.companyAddressInput);
        businessNumberInput = findViewById(R.id.businessNumberInput);
        passwordFindButton = findViewById(R.id.passwordFindButton);
        passwordCheckTextView = findViewById(R.id.passwordCheckTextView);

        passwordFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = companyEmailInput.getText().toString();
                String name = companyNameInput.getText().toString();
                String address = companyAddressInput.getText().toString();
                String businessNumber = businessNumberInput.getText().toString();

                if (email.isEmpty() || name.isEmpty() || address.isEmpty() || businessNumber.isEmpty()) {
                    Toast.makeText(PasswordFindActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    findPassword(email, name, address, businessNumber);
                }
            }
        });
    }

    private void findPassword(String email, String name, String address, String businessNumber) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(PASSWORD_FIND_URL).newBuilder()
                .addQueryParameter("companyEmail", email)
                .addQueryParameter("companyName", name)
                .addQueryParameter("companyAddress", address)
                .addQueryParameter("businessNumber", businessNumber)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PasswordFindActivity.this, "비밀번호 찾기 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        // 비밀번호 찾기 성공 시 PasswordResetActivity로 이동
                        Intent intent = new Intent(PasswordFindActivity.this, PasswordResetActivity.class);
                        intent.putExtra("companyEmail", email); // 이메일 정보를 다음 액티비티에 전달
                        startActivity(intent);
                        finish(); // 현재 액티비티 종료
                    });
                } else {
                    runOnUiThread(() -> {
                        passwordCheckTextView.setText("비밀번호 찾기 실패: 입력한 정보를 확인하세요.");
                        passwordCheckTextView.setTextColor(ContextCompat.getColor(PasswordFindActivity.this, R.color.gray)); // 실패 메시지 색상 변경
                    });
                }
            }
        });
    }
}
