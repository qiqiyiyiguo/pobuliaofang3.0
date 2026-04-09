package org.demo.dao;

import org.demo.entity.JobRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRequirementRepository extends JpaRepository<JobRequirement, Long> {
    Optional<JobRequirement> findByJobPosition(String jobPosition);
}