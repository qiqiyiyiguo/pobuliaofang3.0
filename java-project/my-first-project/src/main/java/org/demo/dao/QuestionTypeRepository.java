package org.demo.dao;

import org.demo.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 题目类型数据访问层
 */
@Repository
public interface QuestionTypeRepository extends JpaRepository<QuestionType, Integer> {

    /**
     * 1. 查询所有类型，按sort排序（升序）
     */
    List<QuestionType> findAllByOrderBySortAsc();

    /**
     * 2. 查询所有类型，按sort排序（降序）
     */
    List<QuestionType> findAllByOrderBySortDesc();

    /**
     * 3. 根据类型名称查询
     */
    Optional<QuestionType> findByName(String name);

    /**
     * 4. 判断类型名称是否存在（用于新增时校验）
     */
    boolean existsByName(String name);

    /**
     * 5. 查询sort大于某个值的所有类型
     */
    List<QuestionType> findBySortGreaterThan(Integer sort);

    /**
     * 6. 查询sort小于某个值的所有类型
     */
    List<QuestionType> findBySortLessThan(Integer sort);

    /**
     * 7. 获取最大sort值（用于新增时自动计算排序）
     */
    @Query("SELECT MAX(q.sort) FROM QuestionType q")
    Integer findMaxSort();

    /**
     * 8. 获取所有类型的ID和名称映射（用于缓存）
     */
    @Query("SELECT new map(q.id as id, q.name as name) FROM QuestionType q ORDER BY q.sort")
    List<Object[]> findAllIdAndName();
}