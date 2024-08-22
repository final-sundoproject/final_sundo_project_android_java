package com.example.sundo_project_app.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.location.MapActivity;
import com.example.sundo_project_app.project.api.ProjectApi;
import com.example.sundo_project_app.project.model.Project;
import com.example.sundo_project_app.utill.UrlManager;
import com.example.sundo_project_app.utill.toolBarActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddbusinessActivity extends toolBarActivity {

    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList = new ArrayList<>();
    private ProjectApi apiService;
    private Long companyCode;  // 회사 코드, 필요에 따라 변경하거나 동적으로 설정
    private String token;
    private TextView noResultsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_business);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        userNameTextView = findViewById(R.id.userNameTextView);

        if (userNameTextView != null) {
            userNameTextView.setText(companyName != null ? companyName : "No Company");
        } else {
            Log.e("MapActivity", "TextView with ID userNameTextView not found.");
        }


        // Intent에서 데이터 가져오기
        Intent intent = getIntent();
        companyCode = intent.getLongExtra("companyCode", -1); // 기본값 -1로 설정
        token = intent.getStringExtra("token");
        companyName = intent.getStringExtra("companyName");

        // 데이터 확인
        Log.d("AddbusinessActivity", "Received companyCode: " + companyCode);
        Log.d("AddbusinessActivity", "Received token: " + (token != null ? token : "null"));
        Log.d("AddbusinessActivity", "Received companyName: " + (companyName != null ? companyName : "null"));
        
        if (companyCode == -1) {
            Log.d("companyCode: {}, ", String.valueOf(companyCode));
            Log.d("token: {}, ", String.valueOf(token));
            Toast.makeText(this, "유효하지 않은 회사 코드입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return; // 데이터가 유효하지 않을 경우 Activity 종료
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noResultsTextView = findViewById(R.id.noResultsTextView);

        projectAdapter = new ProjectAdapter(projectList, this::onProjectItemClick);
        recyclerView.setAdapter(projectAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UrlManager.BASE_URL) // 서버의 기본 URL (에뮬레이터에서는 localhost가 10.0.2.2로 매핑됨)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ProjectApi.class);

        loadProjects();

        Button addProjectButton = findViewById(R.id.addProjectButton);
        Button deleteProjectButton = findViewById(R.id.deleteProjectButton);

        addProjectButton.setOnClickListener(v -> showAddProjectDialog());
        deleteProjectButton.setOnClickListener(v -> deleteSelectedProjects());

        // 검색 기능 추가
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                projectAdapter.filter(s.toString()); // 텍스트 변경 시 필터링 수행
                updateNoResultsTextView();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    private void updateNoResultsTextView() {
        if (projectAdapter.getItemCount() == 0) {
            noResultsTextView.setVisibility(View.VISIBLE); // 결과가 없으면 보이게
        } else {
            noResultsTextView.setVisibility(View.GONE); // 결과가 있으면 숨기기
        }
    }

    private void onProjectItemClick(Project projects) {
        Intent mapIntent = new Intent(AddbusinessActivity.this, MapActivity.class);
        mapIntent.putExtra("project", projects);
        mapIntent.putExtra("token", token);
        startActivity(mapIntent);
    }

    private void loadProjects() {
        apiService.getProjects(companyCode).enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.clear();
                    projectList.addAll(response.body());
                    projectAdapter.updateProjectList(projectList);  // 초기 데이터 로드 시 전체 리스트 표시
                } else {
                    Toast.makeText(AddbusinessActivity.this, "No projects found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                Toast.makeText(AddbusinessActivity.this, "Failed to load projects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.modal_dialog, null);
        builder.setView(dialogView);

        EditText projectNameInput = dialogView.findViewById(R.id.projectNameInput);

        builder.setPositiveButton("추가", (dialog, which) -> {
            String projectName = projectNameInput.getText().toString();
            if (!projectName.isEmpty()) {
                addNewProject(projectName);
            } else {
                Toast.makeText(AddbusinessActivity.this, "사업명을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addNewProject(String projectName) {
        Project newProject = new Project(projectName, companyCode, "");  // 등록일자는 서버에서 설정
        apiService.createProject(newProject).enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.add(response.body());
                    projectAdapter.updateProjectList(projectList);
                    Toast.makeText(AddbusinessActivity.this, "사업이 추가되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddbusinessActivity.this, "Failed to add project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {
                Toast.makeText(AddbusinessActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSelectedProjects() {
        List<Long> selectedProjectIds = new ArrayList<>();
        for (Project project : projectList) {
            if (project.isChecked()) {
                selectedProjectIds.add(project.getProjectId());
            }
        }

        if (!selectedProjectIds.isEmpty()) {
            for (Long projectId : selectedProjectIds) {
                deleteProject(projectId);
            }
        } else {
            Toast.makeText(this, "선택된 사업이 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProject(Long projectId) {
        apiService.deleteProject(projectId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    projectList.removeIf(project -> project.getProjectId().equals(projectId));
                    projectAdapter.updateProjectList(projectList);
                    Toast.makeText(AddbusinessActivity.this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddbusinessActivity.this, "Failed to delete project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddbusinessActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
