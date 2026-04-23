package com.fixroad.repository;

import com.fixroad.model.Upvote;
import com.fixroad.model.Issue;
import com.fixroad.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UpvoteRepository extends JpaRepository<Upvote, UUID> {

    // Check if user already upvoted this issue
    boolean existsByIssueAndUser(Issue issue, User user);

    // Count total upvotes for an issue
    long countByIssue(Issue issue);
}