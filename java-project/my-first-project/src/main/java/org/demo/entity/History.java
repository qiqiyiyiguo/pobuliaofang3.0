package org.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 历史记录实体类
 * 替代原有的InterviewRecord和InterviewReport
 * 一次问答就是一条独立记录，不需要会话概念
 */
@Entity
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // 记录ID

    /**
     * 会话ID
     * 标识一次完整的面试会话
     */
    @Column(name = "session_id", length = 50, nullable = false)
    private String sessionId;  // 添加这个字段！

    /**
     * 用户ID
     * 关联到user表的id
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 岗位类型
     * 如：Java、Web、Python、Test
     */
    @Column(name = "job_position", length = 50)
    private String jobPosition;

    /**
     * 面试风格
     * 友好 / 严格 / 温和
     */
    @Column(name = "style", length = 20)
    private String style;

    /**
     * 问题内容
     */
    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    /**
     * 用户回答内容
     */
    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    private String answer;

    /**
     * AI点评内容
     */
    @Column(name = "ai_reply", columnDefinition = "TEXT")
    private String aiReply;

    /**
     * 本题得分
     * 0-100分
     */
    @Column(name = "score")
    private Double score;

    /**
     * 题目类型
     * 技术知识 / 项目经历 / 场景题 / 行为题
     */
    @Column(name = "question_type", length = 50)
    private String questionType;

    /**
     * 题目难度
     * 1-简单，2-中等，3-困难
     */
    @Column(name = "difficulty")
    private Integer difficulty;

    /**
     * 回答时长（秒）
     */
    @Column(name = "answer_duration")
    private Integer answerDuration;

    /**
     * 创建时间（记录生成时间）
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 扩展字段（JSON格式，用于存储额外信息）
     * 如：AI的详细评价、情绪分析等
     */
    @Column(name = "extra_data", columnDefinition = "JSON")
    private String extraData;

    // ========== 构造方法 ==========

    public History() {
    }

    public History(String sessionId, Long userId, String question, String answer) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.question = question;
        this.answer = answer;
    }

    public History(String sessionId, Long userId, String question, String answer, String aiReply, Double score) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.aiReply = aiReply;
        this.score = score;
    }

    // ========== Getter和Setter方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAiReply() {
        return aiReply;
    }

    public void setAiReply(String aiReply) {
        this.aiReply = aiReply;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getAnswerDuration() {
        return answerDuration;
    }

    public void setAnswerDuration(Integer answerDuration) {
        this.answerDuration = answerDuration;
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

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
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
     * 获取格式化的创建时间
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return createdAt.format(formatter);
    }

    /**
     * 判断是否有评分
     */
    public boolean hasScore() {
        return score != null && score > 0;
    }

    /**
     * 判断是否有AI点评
     */
    public boolean hasAiReply() {
        return aiReply != null && !aiReply.isEmpty();
    }

    /**
     * 获取简化的历史记录（用于列表展示）
     */
    public Map<String, Object> toSimpleMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("question", question);
        map.put("answer", answer.length() > 50 ? answer.substring(0, 50) + "..." : answer);
        map.put("score", score);
        map.put("jobPosition", jobPosition);
        map.put("createdAt", getFormattedCreatedAt());
        return map;
    }

    /**
     * 获取完整的历史记录（用于详情展示）
     */
    public Map<String, Object> toDetailMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("sessionId", sessionId);
        map.put("userId", userId);
        map.put("jobPosition", jobPosition);
        map.put("style", style);
        map.put("question", question);
        map.put("answer", answer);
        map.put("aiReply", aiReply);
        map.put("score", score);
        map.put("questionType", questionType);
        map.put("difficulty", difficulty);
        map.put("answerDuration", answerDuration);
        map.put("createdAt", getFormattedCreatedAt());
        return map;
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", jobPosition='" + jobPosition + '\'' +
                ", questionType='" + questionType + '\'' +
                ", score=" + score +
                ", createdAt=" + createdAt +
                '}';
    }
}