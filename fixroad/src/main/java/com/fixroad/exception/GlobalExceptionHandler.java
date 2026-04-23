package com.fixroad.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ==================== MAX FILE SIZE EXCEPTION ====================
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSize(MaxUploadSizeExceededException ex) {

        return ResponseEntity
                .status(400)
                .body(Map.of("error", "File size exceeds maximum limit (Max 2MB)"));
    }

    // ==================== MULTIPART EXCEPTION ====================
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipart(MultipartException ex) {

        return ResponseEntity
                .status(400)
                .body(Map.of("error", "File size exceeds maximum limit (Max 2MB)"));
    }

    // ==================== GENERIC EXCEPTION ====================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        return ResponseEntity
                .status(400)
                .body(Map.of("error", ex.getMessage()));
    }

    // ==================== DUPLICATE ISSUE EXCEPTION ====================
    @ExceptionHandler(DuplicateIssueException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateIssue(
            DuplicateIssueException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("similarityScore", ex.getSimilarityScore());
        response.put("duplicate", true);
        response.put("issueId", ex.getMatchedIssueId());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}