package com.fixroad.repository;

import com.fixroad.model.Issue;
import com.fixroad.model.IssueStatus;
import com.fixroad.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {

@Query("""
    SELECT i FROM Issue i
    WHERE i.latitude BETWEEN :minLat AND :maxLat
    AND i.longitude BETWEEN :minLon AND :maxLon
""")
List<Issue> findIssuesInBoundingBox(
        @Param("minLat") double minLat,
        @Param("maxLat") double maxLat,
        @Param("minLon") double minLon,
        @Param("maxLon") double maxLon);

    // Get all issues by status
    List<Issue> findByStatus(IssueStatus status);

    // Get all issues assigned to a specific user
    List<Issue> findByAssignedToId(UUID userId);

    // Get all issues ordered by newest first
    List<Issue> findAllByOrderByCreatedAtDesc();

    List<Issue> findByUser(User user);

    long countByStatus(IssueStatus status);

    List<Issue> findTop5ByOrderByCreatedAtDesc();

}