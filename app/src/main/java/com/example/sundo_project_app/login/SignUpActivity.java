package com.example.sundo_project_app.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sundo_project_app.R;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private EditText nameInput;
    private EditText addressInput;
    private EditText businessNumberInput;
    private Button signUpButton;

    private static final String SIGNUP_URL = "http://172.30.1.94:8000/api/companies/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        businessNumberInput = findViewById(R.id.businessNumberInput);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String name = nameInput.getText().toString();
                String address = addressInput.getText().toString();
                String businessNumber = businessNumberInput.getText().toString();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty() || address.isEmpty() || businessNumber.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    signUp(email, password, name, address, businessNumber);
                }
            }
        });
    }

    private void signUp(String email, String password, String name, String address, String businessNumber) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String json = "{\"companyEmail\":\"" + email + "\", \"password\":\"" + password + "\", \"companyName\":\"" + name + "\", \"companyAddress\":\"" + address + "\", \"businessNumber\":\"" + businessNumber + "\"}";
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(SIGNUP_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                        finish(); // 회원가입 후 화면 종료
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "회원가입 실패: 다시 시도하세요", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
