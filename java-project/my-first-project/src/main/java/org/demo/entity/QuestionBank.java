package org.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 题库实体类
 * 对应数据库表：question_bank
 */
@Entity
@Table(name = "question_bank")
public class QuestionBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // 题目ID

    @Column(name = "type_id", nullable = false)
    private Integer typeId;  // 所属类型ID

    @Column(name = "name", nullable = false, length = 200)
    private String name;  // 题目名称

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;  // 题目内容

    // ========== 以下是补充字段（可选，根据需求添加）==========

    @Column(name = "job_position", length = 50)
    private String jobPosition;  // 适用岗位（如：Java后端、前端、算法）

    @Column(name = "difficulty")
    private Integer difficulty;  // 难度等级：1-简单，2-中等，3-困难

    @Column(name = "tags", length = 200)
    private String tags;  // 标签，用逗号分隔（如：多线程,并发,JVM）

    @Column(name = "reference_answer", columnDefinition = "TEXT")
    private String referenceAnswer;  // 参考答案

    @Column(name = "analysis", columnDefinition = "TEXT")
    private String analysis;  // 题目解析

    @Column(name = "view_count")
    private Integer viewCount = 0;  // 被查看次数

    @Column(name = "status")
    private Integer status = 1;  // 状态：0-禁用，1-启用

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // 更新时间

    // ========== 构造方法 ==========
    public QuestionBank() {
    }

    // ========== Getter和Setter方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public void setReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== 生命周期回调 ==========
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewCount == null) {
            viewCount = 0;
        }
        if (status == null) {
            status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "QuestionBank{" +
                "id=" + id +
                ", typeId=" + typeId +
                ", name='" + name + '\'' +
                ", jobPosition='" + jobPosition + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }
}