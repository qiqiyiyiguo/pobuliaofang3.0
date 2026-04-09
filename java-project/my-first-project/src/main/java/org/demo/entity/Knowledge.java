package org.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG知识库实体类
 * 存储向量化的知识点，用于相似性检索
 */
@Entity
@Table(name = "knowledge")
public class Knowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // 知识条目ID

    /**
     * 关联的题目ID（可选）
     * 如果这条知识来自题库，则关联对应的题目
     */
    @Column(name = "question_id")
    private Long questionId;

    /**
     * 岗位类型
     * 如：Java后端、前端、算法
     */
    @Column(name = "job_position", length = 50)
    private String jobPosition;

    /**
     * 知识点标签
     * 用逗号分隔，如："多线程,并发,JVM,内存模型"
     */
    @Column(name = "tags", length = 200)
    private String tags;

    /**
     * 知识点标题
     * 简要描述这条知识的内容
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * 知识点内容（markdown格式）
     * 存储完整的知识文本，支持markdown渲染
     */
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 向量化字段（核心）
     * 存储文本的embedding向量，用于相似度计算
     * MySQL没有原生向量类型，这里用JSON格式存储浮点数数组
     * 如：[0.123, -0.456, 0.789, ...]
     */
    @Column(name = "embedding", columnDefinition = "JSON")
    private String embedding;  // JSON格式存储向量

    /**
     * 向量维度
     * 通义千问embedding接口返回的向量维度
     * text-embedding-v1: 768维
     * text-embedding-v2: 1536维
     */
    @Column(name = "embedding_dim")
    private Integer embeddingDim;

    /**
     * 知识点来源
     * 如：题库、人工录入、网络资料等
     */
    @Column(name = "source", length = 50)
    private String source;

    /**
     * 知识点类型
     * 如：概念、原理、代码示例、面试题等
     */
    @Column(name = "knowledge_type", length = 50)
    private String knowledgeType;

    /**
     * 相关链接
     */
    @Column(name = "reference_url", length = 500)
    private String referenceUrl;

    /**
     * 重要程度
     * 1-5，5最重要
     */
    @Column(name = "importance")
    private Integer importance = 3;

    /**
     * 使用次数
     * 统计这条知识被检索/使用的次数
     */
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    /**
     * 状态
     * 0-禁用，1-启用
     */
    @Column(name = "status")
    private Integer status = 1;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== 构造方法 ==========

    public Knowledge() {
    }

    public Knowledge(String jobPosition, String title, String content) {
        this.jobPosition = jobPosition;
        this.title = title;
        this.content = content;
    }

    // ========== Getter和Setter方法 ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public Integer getEmbeddingDim() {
        return embeddingDim;
    }

    public void setEmbeddingDim(Integer embeddingDim) {
        this.embeddingDim = embeddingDim;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getKnowledgeType() {
        return knowledgeType;
    }

    public void setKnowledgeType(String knowledgeType) {
        this.knowledgeType = knowledgeType;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }

    public Integer getImportance() {
        return importance;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
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
        if (usageCount == null) {
            usageCount = 0;
        }
        if (importance == null) {
            importance = 3;
        }
        if (status == null) {
            status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== 辅助方法 ==========

    /**
     * 将List<Float>类型的向量转换为JSON字符串
     */
    public void setEmbeddingFromList(List<Float> vector) {
        if (vector == null) {
            this.embedding = null;
            this.embeddingDim = null;
            return;
        }
        // 手动构建JSON数组字符串
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(vector.get(i));
        }
        sb.append("]");
        this.embedding = sb.toString();
        this.embeddingDim = vector.size();
    }

    /**
     * 将JSON字符串转换为List<Float>向量
     */
    public List<Float> getEmbeddingAsList() {
        if (this.embedding == null || this.embedding.isEmpty()) {
            return null;
        }
        // 这里简化处理，实际需要使用JSON解析库
        // 建议在Service层使用Jackson或Gson解析
        return null; // 实际使用时需要实现解析逻辑
    }

    @Override
    public String toString() {
        return "Knowledge{" +
                "id=" + id +
                ", jobPosition='" + jobPosition + '\'' +
                ", title='" + title + '\'' +
                ", tags='" + tags + '\'' +
                ", embeddingDim=" + embeddingDim +
                ", importance=" + importance +
                '}';
    }
}