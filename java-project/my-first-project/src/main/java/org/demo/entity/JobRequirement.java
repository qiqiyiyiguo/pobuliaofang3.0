package org.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 岗位要求实体类
 * 存储各岗位的标准技能要求
 */
@Entity
@Table(name = "job_requirement")
public class JobRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_position", nullable = false, length = 50)
    private String jobPosition;  // 岗位：backend, frontend, python, test

    @Column(name = "position_name", length = 100)
    private String positionName;  // 岗位显示名称：Java后端工程师

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;  // 必备技能（JSON数组）

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    private String preferredSkills;  // 加分技能（JSON数组）

    @Column(name = "education_requirement", length = 100)
    private String educationRequirement;  // 学历要求

    @Column(name = "experience_requirement", length = 50)
    private String experienceRequirement;  // 经验要求

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;  // 岗位描述

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobPosition() { return jobPosition; }
    public void setJobPosition(String jobPosition) { this.jobPosition = jobPosition; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(String preferredSkills) { this.preferredSkills = preferredSkills; }

    public String getEducationRequirement() { return educationRequirement; }
    public void setEducationRequirement(String educationRequirement) { this.educationRequirement = educationRequirement; }

    public String getExperienceRequirement() { return experienceRequirement; }
    public void setExperienceRequirement(String experienceRequirement) { this.experienceRequirement = experienceRequirement; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}