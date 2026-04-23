package com.fixroad.exception;

import java.util.UUID;

public class DuplicateIssueException extends RuntimeException {

    private final double similarityScore;
    private final UUID matchedIssueId;

    public DuplicateIssueException(String message,
                                   double similarityScore,
                                   UUID matchedIssueId) {
        super(message);
        this.similarityScore = similarityScore;
        this.matchedIssueId = matchedIssueId;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public UUID getMatchedIssueId() {
        return matchedIssueId;
    }
}