package org.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户薄弱点跟踪实体
 */
@Entity
@Table(name = "user_weakness")
public class UserWeakness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_position", length = 50)
    private String jobPosition;  // 岗位类型

    @Column(name = "question_type", length = 50)
    private String questionType;  // 题目类型：技术知识、项目经历、场景题、行为题

    @Column(name = "wrong_count")
    private Integer wrongCount = 0;  // 答错次数

    @Column(name = "total_count")
    private Integer totalCount = 0;  // 总答题次数

    @Column(name = "avg_score")
    private Double avgScore = 0.0;  // 平均得分

    @Column(name = "weakness_score")
    private Double weaknessScore = 0.0;  // 薄弱程度评分（越高越薄弱）

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // 计算薄弱程度：权重因子 = (1 - 平均分/100) * (答错次数+1) / (总次数+1)
        if (totalCount > 0) {
            double wrongRate = (double) wrongCount / totalCount;
            double scoreGap = (100 - avgScore) / 100;
            this.weaknessScore = wrongRate * 0.6 + scoreGap * 0.4;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getJobPosition() { return jobPosition; }
    public void setJobPosition(String jobPosition) { this.jobPosition = jobPosition; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public Integer getWrongCount() { return wrongCount; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public Double getAvgScore() { return avgScore; }
    public void setAvgScore(Double avgScore) { this.avgScore = avgScore; }

    public Double getWeaknessScore() { return weaknessScore; }
    public void setWeaknessScore(Double weaknessScore) { this.weaknessScore = weaknessScore; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}