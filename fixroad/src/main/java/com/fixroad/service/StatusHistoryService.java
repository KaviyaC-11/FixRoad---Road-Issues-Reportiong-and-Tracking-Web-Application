package com.fixroad.service;

import com.fixroad.model.Issue;
import com.fixroad.model.StatusHistory;
import com.fixroad.model.User;
import com.fixroad.repository.StatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StatusHistoryService {

    @Autowired
    private StatusHistoryRepository repository;

    // ==================== SAVE STATUS HISTORY ====================
    public void saveHistory(
            Issue issue,
            String oldStatus,
            String newStatus,
            User changedBy) {

        StatusHistory history = new StatusHistory();

        history.setIssue(issue);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());

        repository.save(history);
    }

    // ==================== GET ISSUE HISTORY ====================
    public List<StatusHistory> getHistory(UUID issueId) {

        return repository.findByIssue_IdOrderByChangedAtAsc(issueId);
    }
}