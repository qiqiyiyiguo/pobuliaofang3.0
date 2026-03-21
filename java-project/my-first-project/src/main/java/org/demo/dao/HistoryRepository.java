package org.demo.dao;

import org.demo.entity.History;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 历史记录数据访问层
 * 提供问答记录的CRUD和统计分析功能
 */
@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    // ==================== 基础查询 ====================

    /**
     * 1. 按用户ID查询所有记录（按时间倒序）- 不分页
     */
    List<History> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 2. 按用户ID查询所有记录（按时间倒序）- 支持分页
     */
    List<History> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 3. 按会话ID查询所有记录（按时间升序）- 用于报告生成
     * 注意：实体类中字段名为 createdAt，所以方法名要用 CreatedAt
     */
    List<History> findBySessionIdOrderByCreatedAtAsc(String sessionId);  // 修改这里！

    /**
     * 4. 按用户ID和岗位类型查询
     */
    List<History> findByUserIdAndJobPositionOrderByCreatedAtDesc(Long userId, String jobPosition, Pageable pageable);

    /**
     * 5. 统计用户的总对话次数
     */
    long countByUserId(Long userId);

    // ==================== 分数趋势查询 ====================

    /**
     * 6. 获取用户的分数趋势数据（用于折线图）
     */
    @Query("SELECT new map(h.createdAt as createdAt, h.score as score) " +
            "FROM History h WHERE h.userId = :userId AND h.score IS NOT NULL " +
            "ORDER BY h.createdAt DESC")
    List<Map<String, Object>> findScoreTrendByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 7. 获取用户在某时间范围内的分数趋势
     */
    @Query("SELECT new map(h.createdAt as createdAt, h.score as score) " +
            "FROM History h WHERE h.userId = :userId AND h.score IS NOT NULL " +
            "AND h.createdAt BETWEEN :startTime AND :endTime " +
            "ORDER BY h.createdAt ASC")
    List<Map<String, Object>> findScoreTrendByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ==================== 统计查询 ====================

    /**
     * 8. 统计用户在不同岗位的对话次数
     */
    @Query("SELECT h.jobPosition as jobPosition, COUNT(h) as count " +
            "FROM History h WHERE h.userId = :userId AND h.jobPosition IS NOT NULL " +
            "GROUP BY h.jobPosition")
    List<Map<String, Object>> countByUserIdAndJobPosition(@Param("userId") Long userId);

    /**
     * 9. 获取用户的平均分
     */
    @Query("SELECT AVG(h.score) FROM History h WHERE h.userId = :userId AND h.score IS NOT NULL")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    /**
     * 10. 获取用户的最高分记录
     */
    History findTopByUserIdAndScoreIsNotNullOrderByScoreDesc(Long userId);

    /**
     * 11. 获取用户的最低分记录
     */
    History findTopByUserIdAndScoreIsNotNullOrderByScoreAsc(Long userId);

    // ==================== 搜索查询 ====================

    /**
     * 12. 按关键词搜索用户的历史记录
     */
    @Query("SELECT h FROM History h WHERE h.userId = :userId " +
            "AND (h.question LIKE %:keyword% OR h.answer LIKE %:keyword% OR h.aiReply LIKE %:keyword%) " +
            "ORDER BY h.createdAt DESC")
    List<History> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    // ==================== 删除操作 ====================

    /**
     * 13. 删除用户的所有历史记录
     */
    void deleteByUserId(Long userId);
}