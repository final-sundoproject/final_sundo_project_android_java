package com.example.sundo_project_app.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.project.model.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Project> projectList;
    private List<Project> filteredProjectList; // 필터링된 리스트
    private OnItemClickListener onItemClickListener;

    public ProjectAdapter(List<Project> projectList, OnItemClickListener onItemClickListener) {
        this.projectList = new ArrayList<>(projectList);
        this.filteredProjectList = new ArrayList<>(projectList); // 초기에는 원본 데이터와 동일
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = filteredProjectList.get(position);
        holder.projectNameTextView.setText(project.getProjectName());

        // 날짜를 원하는 형식으로 변환
        String formattedDate = formatDateString(project.getRegistrationDate());
        holder.registrationDateTextView.setText("등록 일자: " + formattedDate);

        holder.projectCheckBox.setChecked(project.isChecked());

        // CheckBox의 클릭 이벤트를 설정합니다.
        holder.projectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            project.setChecked(isChecked);
        });

        // 전체 레이아웃 클릭 시 CheckBox의 상태 변경
        holder.itemView.setOnClickListener(v -> {
            holder.projectCheckBox.setChecked(!holder.projectCheckBox.isChecked());
        });

        // 항목 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(project);
            }
        });
    }

    private String formatDateString(String dateString) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy년 M월 d일 a h시 mm분", Locale.getDefault());
            Date date = originalFormat.parse(dateString);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;  // 변환에 실패하면 원래 문자열 반환
        }
    }

    @Override
    public int getItemCount() {
        return filteredProjectList.size();
    }

    public void filter(String text) {
        filteredProjectList.clear();
        if (text.isEmpty()) {
            filteredProjectList.addAll(projectList); // 검색어가 없을 때는 원본 리스트 전체를 보여줌
        } else {
            for (Project project : projectList) {
                if (project.getProjectName().toLowerCase().contains(text.toLowerCase())) {
                    filteredProjectList.add(project); // 검색어가 포함된 항목을 추가
                }
            }
        }
        notifyDataSetChanged(); // RecyclerView 업데이트
    }

    public void updateProjectList(List<Project> newProjectList) {
        this.projectList.clear();
        this.projectList.addAll(newProjectList);
        filter("");  // 전체 데이터를 반영하여 RecyclerView를 갱신
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectNameTextView;
        TextView registrationDateTextView;
        CheckBox projectCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectName);
            registrationDateTextView = itemView.findViewById(R.id.registrationDate);
            projectCheckBox = itemView.findViewById(R.id.projectCheckBox);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Project project);
    }
}
