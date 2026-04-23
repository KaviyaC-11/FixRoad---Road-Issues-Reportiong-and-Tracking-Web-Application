package com.fixroad.repository;

import com.fixroad.model.Comment;
import com.fixroad.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Fetch all comments for an issue (newest first)
    List<Comment> findByIssueOrderByCreatedAtDesc(Issue issue);
}