package org.demo.entity;

import java.util.List;
import java.util.Map;

/**
 * 简历解析结果实体
 */
public class ResumeParseResult {
    private List<String> skills;           // 技能点列表
    private List<String> projects;         // 项目经验列表
    private String education;              // 学历信息
    private Integer experienceYears;       // 工作年限
    private List<String> certificates;     // 证书/奖项
    private Map<String, Integer> skillLevel; // 技能熟练度（可选）
    private String summary;                // 简历摘要
    private String rawResponse;            // AI原始响应

    // Getters and Setters
    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<String> certificates) {
        this.certificates = certificates;
    }

    public Map<String, Integer> getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Map<String, Integer> skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}