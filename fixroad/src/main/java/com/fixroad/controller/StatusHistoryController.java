package com.fixroad.controller;

import com.fixroad.model.StatusHistory;
import com.fixroad.service.StatusHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/status-history")
@CrossOrigin
public class StatusHistoryController {

    @Autowired
    private StatusHistoryService service;

    @GetMapping("/{issueId}")
    public List<StatusHistory> getHistory(@PathVariable UUID issueId) {

        return service.getHistory(issueId);

    }
}