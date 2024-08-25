package com.example.sundo_project_app.login;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.utill.UrlManager;

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
    private EditText passwordConfirmInput; // 비밀번호 확인 입력 필드
    private EditText nameInput;
    private EditText addressInput;
    private EditText businessNumberInput1; // 사업자 등록 번호의 각 부분
    private EditText businessNumberInput2;
    private EditText businessNumberInput3;
    private Button signUpButton;

    private TextView emailErrorText; // 이메일 오류 메시지
    private TextView passwordGuidelineText;
    private TextView passwordErrorText; // 비밀번호 오류 메시지


    private static final String SIGNUP_URL = UrlManager.BASE_URL + "/api/companies/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordConfirmInput = findViewById(R.id.passwordConfirmInput);
        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);

        // 각 사업자 등록 번호 입력 필드
        businessNumberInput1 = findViewById(R.id.businessNumberInput1);
        businessNumberInput2 = findViewById(R.id.businessNumberInput2);
        businessNumberInput3 = findViewById(R.id.businessNumberInput3);
        signUpButton = findViewById(R.id.signUpButton);

        //에러메세지들
        emailErrorText = findViewById(R.id.emailErrorText);
        passwordGuidelineText = findViewById(R.id.passwordGuidelineText);
        passwordErrorText = findViewById(R.id.passwordErrorText);


        // 비밀번호 가이드라인 초기 설정
        passwordGuidelineText.setText("숫자, 영어, 특수문자 조합으로 8자 이상 입력하세요.");
        passwordGuidelineText.setTextColor(Color.GRAY);
        passwordGuidelineText.setVisibility(View.VISIBLE);

        // 이메일 입력 시 실시간으로 형식 검증
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString();

                if (TextUtils.isEmpty(email)) {
                    // 입력값이 없을 때 오류 메시지 숨기기
                    emailErrorText.setVisibility(View.GONE);
                } else if (!isValidEmail(email)) {
                    // 이메일 형식이 맞지 않을 때 오류 메시지 표시
                    emailErrorText.setText("이메일 형식에 맞게 입력하세요");
                    emailErrorText.setTextColor(Color.RED);
                    emailErrorText.setVisibility(View.VISIBLE);
                } else {
                    // 이메일 형식이 맞을 때 오류 메시지 숨기기
                    emailErrorText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 비밀번호 입력 시 가이드라인 및 오류 메시지 관리
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    // 비밀번호 입력칸이 비어있을 때 가이드라인 표시
                    passwordGuidelineText.setText("숫자, 영어, 특수문자 조합으로 8자 이상 입력하세요.");
                    passwordGuidelineText.setTextColor(Color.GRAY);
                    passwordGuidelineText.setVisibility(View.VISIBLE);
                } else {
                    // 입력 중일 때는 가이드라인 유지 (회색)
                    passwordGuidelineText.setVisibility(View.VISIBLE);
                    passwordGuidelineText.setTextColor(Color.GRAY);
                }
                // 오류 메시지 숨김
                passwordErrorText.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // 포커스를 벗어날 때 비밀번호 검증
        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // 포커스를 벗어났을 때
                String password = passwordInput.getText().toString();
                if (!isValidPassword(password)) {
                    // 비밀번호 형식이 맞지 않을 때 오류 메시지 표시
                    passwordGuidelineText.setText("비밀번호 형식을 확인하세요.");
                    passwordGuidelineText.setTextColor(Color.RED);
                } else {
                    // 올바른 비밀번호일 경우 가이드라인 숨김
                    passwordGuidelineText.setVisibility(View.GONE);
                }
            }
        });


        // 비밀번호 확인 입력 시 실시간 검증
        passwordConfirmInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = passwordInput.getText().toString();
                String confirmPassword = s.toString();

                if (!TextUtils.isEmpty(confirmPassword) && !password.equals(confirmPassword)) {
                    passwordErrorText.setText("비밀번호가 일치하지 않습니다");
                    passwordErrorText.setTextColor(Color.RED);
                    passwordErrorText.setVisibility(View.VISIBLE);
                } else {
                    passwordErrorText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String name = nameInput.getText().toString();
                String address = addressInput.getText().toString();
                // 각 사업자 등록 번호 부분의 값을 가져와서 조합
                String businessNumber1 = businessNumberInput1.getText().toString();
                String businessNumber2 = businessNumberInput2.getText().toString();
                String businessNumber3 = businessNumberInput3.getText().toString();
                String businessNumber = businessNumber1 + "-" + businessNumber2 + "-" + businessNumber3;

                if (email.isEmpty() || password.isEmpty() || name.isEmpty() || address.isEmpty() || businessNumber.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    signUp(email, password, name, address, businessNumber);
                }
            }
        });
    }

    // 비밀번호 조건 검증 메서드
    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[@#$%^&+=!]).+$");
    }

    // 이메일 형식 확인 메서드
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
