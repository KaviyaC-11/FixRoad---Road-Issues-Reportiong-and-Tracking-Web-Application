package com.fixroad.controller;

import com.fixroad.dto.IssueResponse;
import com.fixroad.dto.UpdateStatusRequest;
import com.fixroad.service.IssueService;

import com.fixroad.model.RoadCategory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;

import com.fixroad.dto.AssignRepairRequest;
import com.fixroad.dto.CommentRequest;
import com.fixroad.dto.CommentResponse;
import com.fixroad.dto.IssueDetailResponse;

import jakarta.validation.Valid;

import java.util.List;

import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    private final IssueService issueService;

    // Constructor Injection 
    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }


    // ================= CREATE ISSUE =================
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<IssueResponse> createIssue(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("place") String place,
            @RequestParam("roadCategory") RoadCategory roadCategory,
            @RequestParam("damageSeverity") String damageSeverity,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {

        IssueResponse response = issueService.createIssueWithImage(
                title,
                description,
                place,
                roadCategory,
                damageSeverity,
                latitude,
                longitude,
                image
        );

        return ResponseEntity.ok(response);
    }


    // ================= GET MY ISSUES =================
    @GetMapping("/my")
    public ResponseEntity<List<IssueResponse>> getMyIssues(Authentication authentication) {

        String email = authentication.getName();

        List<IssueResponse> issues =
                issueService.getIssuesByUserEmail(email);

        return ResponseEntity.ok(issues);
    }


    // ================= UPVOTE ISSUE =================
@PostMapping("/{id}/upvote")
public ResponseEntity<?> upvoteIssue(@PathVariable UUID id) {

    try {

        issueService.upvoteIssue(id);

        // Return updated vote count
        int updatedVoteCount = issueService.getVoteCount(id);

        return ResponseEntity.ok(Map.of(
                "message", "Upvoted successfully",
                "voteCount", updatedVoteCount
        ));

    } catch (IllegalStateException e) {

        return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
        );

    }
}


// ================= ADD COMMENT =================
@PostMapping("/{id}/comment")
public ResponseEntity<?> addComment(
        @PathVariable UUID id,
        @Valid @RequestBody CommentRequest request
) {

    CommentResponse response = issueService.addComment(id, request.getContent());

    return ResponseEntity.ok(response);
}



// ================= ADD IMAGE TO EXISTING ISSUE =================
@PostMapping(value = "/{id}/images", consumes = {"multipart/form-data"})
public ResponseEntity<?> addImageToIssue(
        @PathVariable UUID id,
        @RequestParam("image") MultipartFile image
) {

    String filePath = issueService.addImageToExistingIssue(id, image);

    return ResponseEntity.ok(
            Map.of("imageUrl", filePath)
    );
}


// ================= GET ISSUE DETAILS =================
@GetMapping("/{id}/details")
public ResponseEntity<IssueDetailResponse> getIssueDetails(@PathVariable UUID id) {

    IssueDetailResponse response = issueService.getIssueDetails(id);

    return ResponseEntity.ok(response);
}


// ================= Admin Dashboard =================
@GetMapping("/dashboard")
public Map<String, Object> getDashboardStats() {
    return issueService.getDashboardStats();
}

@GetMapping
public List<IssueResponse> getAllIssues() {

    return issueService.getAllIssues();

}


// ================= Assign repair work =================
@SuppressWarnings("null")
@PostMapping("/{id}/assign-repair")
public ResponseEntity<?> assignRepair(
        @PathVariable UUID id,
        @RequestBody AssignRepairRequest request) {

    issueService.assignRepairTeam(id, request);

    return ResponseEntity.ok("Repair team assigned successfully");
}

// ================= Status update =================
@SuppressWarnings("null")
@PatchMapping("/{id}/status")
public ResponseEntity<?> updateStatus(
        @PathVariable UUID id,
        @RequestBody UpdateStatusRequest request) {

    issueService.updateIssueStatus(id, request.getStatus());

    return ResponseEntity.ok("Status updated successfully");
}

}
