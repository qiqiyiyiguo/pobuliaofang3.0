package org.demo.dao;

import org.demo.entity.UserWeakness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWeaknessRepository extends JpaRepository<UserWeakness, Long> {

    Optional<UserWeakness> findByUserIdAndJobPositionAndQuestionType(
            Long userId, String jobPosition, String questionType);

    List<UserWeakness> findByUserIdAndJobPositionOrderByWeaknessScoreDesc(
            Long userId, String jobPosition);

    @Modifying
    @Transactional
    @Query("UPDATE UserWeakness w SET w.wrongCount = w.wrongCount + 1, w.totalCount = w.totalCount + 1 WHERE w.id = :id")
    void incrementWrongCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE UserWeakness w SET w.totalCount = w.totalCount + 1 WHERE w.id = :id")
    void incrementTotalCount(@Param("id") Long id);
}