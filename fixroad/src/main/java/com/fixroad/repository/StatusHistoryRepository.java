package com.fixroad.repository;

import com.fixroad.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {

    List<StatusHistory> findByIssue_IdOrderByChangedAtAsc(UUID issueId);

}
    
