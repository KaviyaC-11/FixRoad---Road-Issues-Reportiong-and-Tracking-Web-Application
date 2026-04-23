package com.fixroad.dto;

import com.fixroad.model.IssueStatus;

public class UpdateStatusRequest {

    private IssueStatus status;

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }
}