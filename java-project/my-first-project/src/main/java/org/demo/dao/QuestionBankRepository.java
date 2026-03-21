package org.demo.dao;

import org.demo.entity.QuestionBank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 题库数据访问层
 */
@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {

    // ==================== 新增：按模块和类别查询 ====================

    /**
     * 根据技术模块和类别分页查询题目
     * @param module 技术模块（Java/Web/Python/Test）
     * @param typeId 题目类型ID（可选）
     */
    @Query("SELECT q FROM QuestionBank q WHERE " +
            "q.jobPosition = :module AND " +
            "(:typeId IS NULL OR q.typeId = :typeId)")
    Page<QuestionBank> findByModuleAndCategory(
            @Param("module") String module,
            @Param("typeId") Integer typeId,
            Pageable pageable);

    /**
     * 根据技术模块查询所有题目（不分页）
     */
    List<QuestionBank> findByJobPosition(String jobPosition);

    /**
     * 统计某模块下的题目数量
     */
    long countByJobPosition(String jobPosition);

    /**
     * 统计某模块下某类别的题目数量
     */
    @Query("SELECT COUNT(q) FROM QuestionBank q WHERE " +
            "q.jobPosition = :module AND q.typeId = :typeId")
    long countByModuleAndType(@Param("module") String module, @Param("typeId") Integer typeId);

    // ==================== 原有的方法 ====================

    /**
     * 根据题目类型ID查询（分页）
     */
    Page<QuestionBank> findByTypeId(Integer typeId, Pageable pageable);

    /**
     * 根据题目名称关键词查询
     */
    Page<QuestionBank> findByNameContaining(String keyword, Pageable pageable);

    /**
     * 根据题目内容关键词查询
     */
    Page<QuestionBank> findByDescriptionContaining(String keyword, Pageable pageable);

    /**
     * 组合查询：类型ID + 名称关键词
     */
    Page<QuestionBank> findByTypeIdAndNameContaining(Integer typeId, String keyword, Pageable pageable);

    /**
     * 复杂查询：同时搜索名称和内容
     */
    @Query("SELECT q FROM QuestionBank q WHERE " +
            "(:typeId IS NULL OR q.typeId = :typeId) AND " +
            "(:keyword IS NULL OR q.name LIKE %:keyword% OR q.description LIKE %:keyword%)")
    Page<QuestionBank> searchQuestions(
            @Param("typeId") Integer typeId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 统计某个类型下的题目数量
     */
    long countByTypeId(Integer typeId);

    /**
     * 检查某个类型下是否有题目
     */
    boolean existsByTypeId(Integer typeId);
}