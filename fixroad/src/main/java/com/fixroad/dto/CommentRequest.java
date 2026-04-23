package com.fixroad.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

   
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}