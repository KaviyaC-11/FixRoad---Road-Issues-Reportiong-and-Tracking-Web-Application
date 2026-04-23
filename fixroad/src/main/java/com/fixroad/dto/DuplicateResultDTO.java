package com.fixroad.dto;

import com.fixroad.model.Issue;

public class DuplicateResultDTO {

    private boolean duplicate;
    private double score;
    private Issue matchedIssue;

    public DuplicateResultDTO(boolean duplicate, double score, Issue matchedIssue) {
        this.duplicate = duplicate;
        this.score = score;
        this.matchedIssue = matchedIssue;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public double getScore() {
        return score;
    }

    public Issue getMatchedIssue() {
        return matchedIssue;
    }
}