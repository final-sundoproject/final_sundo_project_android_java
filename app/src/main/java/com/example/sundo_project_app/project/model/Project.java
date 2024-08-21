package com.example.sundo_project_app.project.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Project implements Serializable {

    private Map<String, String> additionalAttributes;

    // 프로젝트의 고유 ID
    private Long projectId;

    // 프로젝트의 이름
    private String projectName;

    // 회사 코드
    private Long companyCode;

    // 프로젝트 등록 날짜
    private String registrationDate;

    // 프로젝트 선택 상태를 관리하기 위한 필드
    private boolean isChecked;

    // 기본 생성자
    public Project() {}

    // 프로젝트 이름, 회사 코드, 등록 날짜를 사용하는 생성자
    public Project(String projectName, Long companyCode, String registrationDate) {
        this.projectName = projectName;
        this.companyCode = companyCode;
        this.registrationDate = registrationDate;
    }

    // 모든 필드를 사용하는 생성자
    public Project(Long projectId, String projectName, Long companyCode, String registrationDate) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.companyCode = companyCode;
        this.registrationDate = registrationDate;
    }

    // Getter 및 Setter 메서드

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(Long companyCode) {
        this.companyCode = companyCode;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", companyCode=" + companyCode +
                ", registrationDate='" + registrationDate + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }

    public void put(String key, String value) {
        additionalAttributes.put(key, value);
    }

    public String getAttribute(String key) {
        return additionalAttributes.get(key);
    }
}
