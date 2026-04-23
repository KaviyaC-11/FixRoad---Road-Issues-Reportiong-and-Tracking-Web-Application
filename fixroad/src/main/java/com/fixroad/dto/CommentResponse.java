package com.fixroad.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private String username;
    private String content;
    private LocalDateTime createdAt;

    public CommentResponse(String username, String content, LocalDateTime createdAt) {
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}