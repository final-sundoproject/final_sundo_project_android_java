package com.example.sundo_project_app.utill;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sundo_project_app.R;

public abstract class toolBarActivity extends AppCompatActivity {

    protected String token;
    protected long companyCode;
    protected String companyName;
    protected TextView userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 호출 위치 변경
        retrieveLoginInfo();

        TextView userNameTextView = findViewById(R.id.userNameTextView);

        if (userNameTextView != null) {
            if (companyName != null && !companyName.isEmpty()) {
                userNameTextView.setText(companyName);
            } else {
                userNameTextView.setText("비로그인"); // 기본 값 설정
            }
        } else {
            Log.e("ToolBarActivity", "TextView with ID userNameTextView not found.");
        }
    }

    private void retrieveLoginInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        companyCode = sharedPreferences.getLong("companyCode", -1);
        companyName = sharedPreferences.getString("companyName", null);
        Log.d("ToolBarActivity", "Retrieved companyName: " + companyName);
    }
}
