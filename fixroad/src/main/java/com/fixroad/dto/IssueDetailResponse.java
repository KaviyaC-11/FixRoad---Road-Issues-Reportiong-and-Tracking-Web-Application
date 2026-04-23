package com.fixroad.dto;

import com.fixroad.model.IssueStatus;
import com.fixroad.model.RoadCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class IssueDetailResponse {

    private UUID id;
    private String title;
    private String description;
    private String place;
    private RoadCategory roadCategory;
    private String damageSeverity;
    private IssueStatus status;
    private Integer voteCount;
    private LocalDateTime createdAt;
    private String userName;
    private String priority;

    private List<String> images;
    private List<String> imageUrls;
    private List<CommentResponse> comments;

    private String repairTeamName;
    private String repairContactNumber;
    

    public IssueDetailResponse(
            UUID id,
            String title,
            String description,
            String place,
            String userName,
            String priority,
            RoadCategory roadCategory,
            String damageSeverity,
            IssueStatus status,
            Integer voteCount,
            LocalDateTime createdAt,
            List<String> images,
            List<String> imageUrls,
            List<CommentResponse> comments,
            String repairTeamName,
            String repairContactNumber
            )
            
            {
        this.id = id;
        this.title = title;
        this.userName = userName;
        this.priority = priority;
        this.description = description;
        this.place = place;
        this.roadCategory = roadCategory;
        this.damageSeverity = damageSeverity;
        this.status = status;
        this.voteCount = voteCount;
        this.createdAt = createdAt;
        this.images = images;
        this.imageUrls = imageUrls;
        this.comments = comments;
        this.repairTeamName = repairTeamName;
        this.repairContactNumber = repairContactNumber;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getuserName() {return userName; }
    public String getDescription() { return description; }
    public String getPlace() { return place; }
    public RoadCategory getRoadCategory() { return roadCategory; }
    public String getDamageSeverity() { return damageSeverity; }
    public IssueStatus getStatus() { return status; }
    public Integer getVoteCount() { return voteCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getImages() { return images; }
    public List<String> getImageUrls() {return imageUrls; }
    public List<CommentResponse> getComments() { return comments; }
    public String getPriority() {return priority; }
    public String getRepairTeamName() { return repairTeamName;}
    public String getRepairContactNumber() { return repairContactNumber;}

}