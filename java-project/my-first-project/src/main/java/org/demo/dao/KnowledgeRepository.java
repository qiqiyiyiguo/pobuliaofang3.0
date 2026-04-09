package org.demo.dao;

import org.demo.entity.Knowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RAG知识库数据访问层
 * 提供知识点的CRUD和向量检索功能
 */
@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {

    // ==================== 基础查询 ====================

    /**
     * 1. 按岗位类型查询（不分页）
     */
    List<Knowledge> findByJobPosition(String jobPosition);

    /**
     * 2. 按岗位类型查询（分页）- 返回Page对象
     */
    Page<Knowledge> findByJobPosition(String jobPosition, Pageable pageable);

    /**
     * 3. 按标签模糊查询（包含某个标签）
     */
    @Query("SELECT k FROM Knowledge k WHERE k.tags LIKE %:tag%")
    List<Knowledge> findByTagContaining(@Param("tag") String tag);

    /**
     * 4. 按知识点类型查询（不分页）
     */
    List<Knowledge> findByKnowledgeType(String knowledgeType);

    /**
     * 5. 按知识点类型查询（分页）
     */
    Page<Knowledge> findByKnowledgeType(String knowledgeType, Pageable pageable);

    /**
     * 6. 按岗位和知识类型分页查询
     */
    Page<Knowledge> findByJobPositionAndKnowledgeType(
            @Param("jobPosition") String jobPosition,
            @Param("knowledgeType") String knowledgeType,
            Pageable pageable);

    /**
     * 7. 按重要程度查询
     */
    List<Knowledge> findByImportanceGreaterThanEqual(Integer importance);

    /**
     * 8. 按关联题目ID查询
     */
    Knowledge findByQuestionId(Long questionId);

    /**
     * 9. 统计某个岗位的知识点数量
     */
    long countByJobPosition(String jobPosition);

    /**
     * 10. 统计某个标签的知识点数量
     */
    @Query("SELECT COUNT(k) FROM Knowledge k WHERE k.tags LIKE %:tag%")
    long countByTag(@Param("tag") String tag);

    // ==================== 向量检索相关 ====================

    /**
     * 11. 获取所有启用的知识点（用于批量向量检索）
     */
    @Query("SELECT k FROM Knowledge k WHERE k.status = 1")
    List<Knowledge> findAllActive();

    /**
     * 12. 获取指定岗位的启用的知识点
     */
    @Query("SELECT k FROM Knowledge k WHERE k.status = 1 AND k.jobPosition = :jobPosition")
    List<Knowledge> findActiveByJobPosition(@Param("jobPosition") String jobPosition);

    /**
     * 13. 按重要程度获取需要优先检索的知识点
     */
    @Query("SELECT k FROM Knowledge k WHERE k.status = 1 AND k.importance >= :minImportance")
    List<Knowledge> findImportantKnowledge(@Param("minImportance") Integer minImportance);

    // ==================== 高级检索 ====================

    /**
     * 14. 组合条件查询
     */
    @Query("SELECT k FROM Knowledge k WHERE " +
            "(:jobPosition IS NULL OR k.jobPosition = :jobPosition) AND " +
            "(:knowledgeType IS NULL OR k.knowledgeType = :knowledgeType) AND " +
            "(:minImportance IS NULL OR k.importance >= :minImportance) AND " +
            "k.status = 1 " +
            "ORDER BY k.importance DESC, k.usageCount DESC")
    List<Knowledge> searchKnowledge(
            @Param("jobPosition") String jobPosition,
            @Param("knowledgeType") String knowledgeType,
            @Param("minImportance") Integer minImportance,
            Pageable pageable);

    /**
     * 15. 按标题或内容关键词搜索
     */
    @Query("SELECT k FROM Knowledge k WHERE " +
            "k.status = 1 AND " +
            "(k.title LIKE %:keyword% OR k.content LIKE %:keyword%) " +
            "ORDER BY k.importance DESC")
    List<Knowledge> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 16. 获取热门知识点（按使用次数排序）
     */
    @Query("SELECT k FROM Knowledge k WHERE k.status = 1 ORDER BY k.usageCount DESC")
    List<Knowledge> findHotKnowledge(Pageable pageable);

    /**
     * 17. 获取最新添加的知识点
     */
    @Query("SELECT k FROM Knowledge k WHERE k.status = 1 ORDER BY k.createdAt DESC")
    List<Knowledge> findLatestKnowledge(Pageable pageable);

    // ==================== 更新统计 ====================

    /**
     * 18. 增加知识点使用次数
     */
    @Modifying
    @Transactional
    @Query("UPDATE Knowledge k SET k.usageCount = k.usageCount + 1 WHERE k.id = :id")
    void incrementUsageCount(@Param("id") Long id);

    /**
     * 19. 批量增加使用次数
     */
    @Modifying
    @Transactional
    @Query("UPDATE Knowledge k SET k.usageCount = k.usageCount + 1 WHERE k.id IN :ids")
    void batchIncrementUsageCount(@Param("ids") List<Long> ids);

    // ==================== 向量相似度检索 ====================

    /**
     * 20. 基于向量的相似度检索
     */
    @Query(value = "SELECT * FROM knowledge k WHERE k.status = 1 " +
            "ORDER BY cosine_similarity(k.embedding, :queryVector) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Knowledge> findSimilarByVector(
            @Param("queryVector") String queryVectorJson,
            @Param("limit") int limit);

    /**
     * 21. 按岗位筛选后的向量相似度检索
     */
    @Query(value = "SELECT * FROM knowledge k WHERE k.status = 1 " +
            "AND k.job_position = :jobPosition " +
            "ORDER BY cosine_similarity(k.embedding, :queryVector) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Knowledge> findSimilarByVectorAndJobPosition(
            @Param("queryVector") String queryVectorJson,
            @Param("jobPosition") String jobPosition,
            @Param("limit") int limit);

    /**
     * 22. 带重要性加权的向量检索
     */
    @Query(value = "SELECT * FROM knowledge k WHERE k.status = 1 " +
            "ORDER BY (cosine_similarity(k.embedding, :queryVector) * " +
            "         (k.importance / 5.0)) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Knowledge> findSimilarWithImportanceWeight(
            @Param("queryVector") String queryVectorJson,
            @Param("limit") int limit);

    // ==================== 统计分析 ====================

    /**
     * 23. 统计各岗位的知识点数量
     */
    @Query("SELECT k.jobPosition as jobPosition, COUNT(k) as count " +
            "FROM Knowledge k " +
            "GROUP BY k.jobPosition " +
            "ORDER BY COUNT(k) DESC")
    List<Map<String, Object>> countByJobPosition();

    /**
     * 24. 统计各类型的知识点数量
     */
    @Query("SELECT k.knowledgeType as type, COUNT(k) as count " +
            "FROM Knowledge k " +
            "WHERE k.knowledgeType IS NOT NULL " +
            "GROUP BY k.knowledgeType " +
            "ORDER BY COUNT(k) DESC")
    List<Map<String, Object>> countByKnowledgeType();

    /**
     * 25. 获取常用的标签云数据
     */
    @Query(value = "SELECT tags, COUNT(*) as count FROM knowledge " +
            "WHERE tags IS NOT NULL AND tags != '' " +
            "GROUP BY tags ORDER BY COUNT(*) DESC LIMIT 50",
            nativeQuery = true)
    List<Map<String, Object>> getTagCloud();

    // ==================== 维护方法 ====================

    /**
     * 26. 删除过时的知识点（按创建时间）
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Knowledge k WHERE k.createdAt < :date AND k.usageCount < :minUsage")
    void deleteOutdated(@Param("date") LocalDateTime date, @Param("minUsage") int minUsage);

    /**
     * 27. 批量更新知识点状态
     */
    @Modifying
    @Transactional
    @Query("UPDATE Knowledge k SET k.status = :status WHERE k.id IN :ids")
    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
}