package org.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 分析结果实体类
 * 存储每次面试的综合分析结果
 */
@Entity
@Table(name = "analysis_result")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // 分析结果ID

    /**
     * 用户ID
     * 关联到user表的id
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 会话ID
     * 关联到一次完整的面试会话
     */
    @Column(name = "session_id", length = 50)
    private String sessionId;

    /**
     * 报告标题
     * 如：第一次面试、第二次面试
     */
    @Column(name = "title", length = 100)
    private String title;

    /**
     * 岗位类型
     */
    @Column(name = "job_position", length = 50)
    private String jobPosition;

    /**
     * 面试风格
     */
    @Column(name = "interview_style", length = 20)
    private String interviewStyle;

    // ==================== 各维度总分 ====================

    /**
     * 综合总分
     */
    @Column(name = "total_score")
    private Double totalScore;

    /**
     * 技术正确性平均分
     */
    @Column(name = "correctness_avg")
    private Double correctnessAvg;

    /**
     * 知识深度平均分
     */
    @Column(name = "depth_avg")
    private Double depthAvg;

    /**
     * 逻辑严谨性平均分
     */
    @Column(name = "logic_avg")
    private Double logicAvg;

    /**
     * 语速平均分
     */
    @Column(name = "speed_avg")
    private Double speedAvg;

    /**
     * 自信度平均分
     */
    @Column(name = "confidence_avg")
    private Double confidenceAvg;

    /**
     * 情绪倾向统计
     * 记录积极/中性/消极的次数
     */
    @Column(name = "sentiment_stats", length = 100)
    private String sentimentStats;

    // ==================== 详细数据（JSON格式）====================

    /**
     * 各维度详细得分（JSON）
     * 包含每道题的详细分析
     */
    @Column(name = "dimension_details", columnDefinition = "JSON")
    private String dimensionDetails;

    /**
     * 问答列表（JSON）
     * 包含所有问题和回答
     */
    @Column(name = "qa_list", columnDefinition = "JSON")
    private String qaList;

    // ==================== 分析结论 ====================

    /**
     * 亮点分析（JSON数组）
     */
    @Column(name = "strengths", columnDefinition = "JSON")
    private String strengths;

    /**
     * 不足分析（JSON数组）
     */
    @Column(name = "weaknesses", columnDefinition = "JSON")
    private String weaknesses;

    /**
     * 改进建议（JSON数组）
     */
    @Column(name = "improvements", columnDefinition = "JSON")
    private String improvements;

    /**
     * 总结评语
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // ==================== 时间信息 ====================

    /**
     * 面试开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 面试结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 报告生成时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== 构造方法 ==========

    public AnalysisResult() {
    }

    public AnalysisResult(Long userId, String sessionId, String jobPosition) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.jobPosition = jobPosition;
    }

    // ========== Getter和Setter方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public String getInterviewStyle() {
        return interviewStyle;
    }

    public void setInterviewStyle(String interviewStyle) {
        this.interviewStyle = interviewStyle;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Double getCorrectnessAvg() {
        return correctnessAvg;
    }

    public void setCorrectnessAvg(Double correctnessAvg) {
        this.correctnessAvg = correctnessAvg;
    }

    public Double getDepthAvg() {
        return depthAvg;
    }

    public void setDepthAvg(Double depthAvg) {
        this.depthAvg = depthAvg;
    }

    public Double getLogicAvg() {
        return logicAvg;
    }

    public void setLogicAvg(Double logicAvg) {
        this.logicAvg = logicAvg;
    }

    public Double getSpeedAvg() {
        return speedAvg;
    }

    public void setSpeedAvg(Double speedAvg) {
        this.speedAvg = speedAvg;
    }

    public Double getConfidenceAvg() {
        return confidenceAvg;
    }

    public void setConfidenceAvg(Double confidenceAvg) {
        this.confidenceAvg = confidenceAvg;
    }

    public String getSentimentStats() {
        return sentimentStats;
    }

    public void setSentimentStats(String sentimentStats) {
        this.sentimentStats = sentimentStats;
    }

    public String getDimensionDetails() {
        return dimensionDetails;
    }

    public void setDimensionDetails(String dimensionDetails) {
        this.dimensionDetails = dimensionDetails;
    }

    public String getQaList() {
        return qaList;
    }

    public void setQaList(String qaList) {
        this.qaList = qaList;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getImprovements() {
        return improvements;
    }

    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== 辅助方法 ==========

    /**
     * 获取格式化的开始时间
     */
    public String getFormattedStartTime() {
        if (startTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return startTime.format(formatter);
    }

    /**
     * 获取格式化的结束时间
     */
    public String getFormattedEndTime() {
        if (endTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return endTime.format(formatter);
    }

    /**
     * 获取面试时长（分钟）
     */
    public long getDurationMinutes() {
        if (startTime == null) return 0;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMinutes();
    }

    /**
     * 计算综合总分（各维度加权平均）
     */
    public void calculateTotalScore() {
        double sum = 0;
        int count = 0;

        if (correctnessAvg != null) { sum += correctnessAvg; count++; }
        if (depthAvg != null) { sum += depthAvg; count++; }
        if (logicAvg != null) { sum += logicAvg; count++; }
        if (speedAvg != null) { sum += speedAvg; count++; }
        if (confidenceAvg != null) { sum += confidenceAvg; count++; }

        if (count > 0) {
            this.totalScore = Math.round((sum / count) * 100) / 100.0;
        }
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "id=" + id +
                ", userId=" + userId +
                ", sessionId='" + sessionId + '\'' +
                ", jobPosition='" + jobPosition + '\'' +
                ", totalScore=" + totalScore +
                ", createdAt=" + createdAt +
                '}';
    }
}