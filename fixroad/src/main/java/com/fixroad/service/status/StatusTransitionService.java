package com.fixroad.service.status;

import org.springframework.stereotype.Service;

import com.fixroad.model.IssueStatus;

@Service
public class StatusTransitionService {

    public boolean isValidStatusTransition(IssueStatus current, IssueStatus next) {

        if (current == null) return true;

        switch (current) {
            case REPORTED:
                return next == IssueStatus.IN_PROGRESS;

            case IN_PROGRESS:
                return next == IssueStatus.RESOLVED;

            case RESOLVED:
                return false;

            default:
                return false;
        }
    }
}