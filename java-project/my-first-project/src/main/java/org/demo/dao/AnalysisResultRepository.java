package org.demo.dao;

import org.demo.entity.AnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 分析结果数据访问层
 * 提供综合报告的CRUD和查询功能
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    /**
     * 按用户ID查询所有报告（按时间倒序）
     */
    List<AnalysisResult> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 按用户ID分页查询报告（按时间倒序）
     */
    Page<AnalysisResult> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 按会话ID查询报告
     */
    Optional<AnalysisResult> findBySessionId(String sessionId);

    /**
     * 按用户ID和岗位查询报告
     */
    List<AnalysisResult> findByUserIdAndJobPositionOrderByCreatedAtDesc(Long userId, String jobPosition);

    /**
     * 查询用户在某时间范围内的报告
     */
    List<AnalysisResult> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户的报告总数
     */
    long countByUserId(Long userId);

    /**
     * 统计用户在各岗位的报告数
     */
    @Query("SELECT a.jobPosition as jobPosition, COUNT(a) as count " +
            "FROM AnalysisResult a WHERE a.userId = :userId AND a.jobPosition IS NOT NULL " +
            "GROUP BY a.jobPosition")
    List<Object[]> countByUserIdAndJobPosition(@Param("userId") Long userId);

    /**
     * 获取用户的平均总分
     */
    @Query("SELECT AVG(a.totalScore) FROM AnalysisResult a WHERE a.userId = :userId AND a.totalScore IS NOT NULL")
    Double getAverageTotalScoreByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的最新报告
     */
    Optional<AnalysisResult> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 删除用户的报告
     */
    void deleteByUserId(Long userId);

    /**
     * 检查会话是否已生成报告
     */
    boolean existsBySessionId(String sessionId);
}
