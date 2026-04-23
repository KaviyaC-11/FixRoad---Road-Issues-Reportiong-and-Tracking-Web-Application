package com.fixroad.repository;

import com.fixroad.model.Issue;
import com.fixroad.model.IssueImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;   // ✅ ADD THIS
import java.util.UUID;

public interface IssueImageRepository extends JpaRepository<IssueImage, UUID> {

    List<IssueImage> findByIssue(Issue issue);

}