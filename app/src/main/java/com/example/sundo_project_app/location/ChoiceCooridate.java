package com.example.sundo_project_app.location;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.project.model.Project;

import java.io.Serializable;

public class ChoiceCooridate extends DialogFragment {

    private static final String ARG_PROJECT_ID = "project_id";


    public static ChoiceCooridate newInstance(String projectId, Project currentProject, String registerName) {
        ChoiceCooridate fragment = new ChoiceCooridate();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putSerializable("currentProject", currentProject);
        args.putString("registerName", registerName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choice_coordinate, container, false);

        // arguments에서 projectId를 가져옵니다.
        Bundle args = getArguments();
        if (args != null) {
            String projectId = args.getString(ARG_PROJECT_ID);

            Project currentProject = (Project) getArguments().getSerializable("currentProject");
            String registerName = getArguments().getString("registerName");


            // 'DD 선택' 버튼 클릭 리스너
            Button btnDdFormat = view.findViewById(R.id.btn_dd_format);
            btnDdFormat.setOnClickListener(v -> {
                dismiss(); // 모달을 닫고
                Intent intent = new Intent(getActivity(), DdActivity.class);
                intent.putExtra("project_id", projectId); // projectId 추가
                intent.putExtra("currentProject", currentProject); // projectId 추가
                intent.putExtra("registerName", registerName); // projectId 추가

                Log.d("projectId",projectId);
                startActivity(intent); // DdActivity 시작
            });

            // 'DMS' 버튼 클릭 리스너
            Button btnDmsFormat = view.findViewById(R.id.btn_dms_format);
            btnDmsFormat.setOnClickListener(v -> {
                dismiss(); // 모달을 닫고
                Intent intent = new Intent(getActivity(), DmsActivity.class);
                intent.putExtra("project_id", projectId); // projectId 추가

                intent.putExtra("currentProject", currentProject); // projectId 추가
                intent.putExtra("registerName", registerName); // projectId 추가
                Log.d("projectId",projectId);
                startActivity(intent); // DmsActivity 시작
            });
        }

        // 'x' 버튼 클릭 리스너 추가
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());


        return view;
    }
}