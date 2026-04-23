package com.fixroad.dto;

import com.fixroad.model.IssueStatus;
import com.fixroad.model.RoadCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class IssueResponse {

    private UUID id;
    private String title;
    private String description;
    private String place;
    private RoadCategory placeCategory;
    private String damageSeverity;
    private Double latitude;
    private Double longitude;
    private Integer voteCount;
    private IssueStatus status;
    private LocalDateTime createdAt;
    private String userName;
    private UUID imageId;
    private String priority;
    private String repairTeamName;
    private String repairContactNumber;
    private List<CommentResponse> comments;
    

    // Constructors

    public IssueResponse() {
    }

    public IssueResponse(UUID id, String title, String description, String place,
                         RoadCategory placeCategory, String damageSeverity,
                         Double latitude, Double longitude, Integer voteCount,
                         IssueStatus status, LocalDateTime createdAt, String userName, UUID imageId,
                         String priority,String repairTeamName,String repairContactNumber) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.place = place;
        this.placeCategory = placeCategory;
        this.damageSeverity = damageSeverity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.voteCount = voteCount;
        this.status = status;
        this.createdAt = createdAt;
        this.userName = userName;
        this.imageId = imageId;
        this.priority = priority;
        this.repairTeamName = repairTeamName;
        this.repairContactNumber = repairContactNumber;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPlace() {
        return place;
    }

    public RoadCategory getPlaceCategory() {
        return placeCategory;
    }

    public String getDamageSeverity(){
        return damageSeverity;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getuserName(){
        return userName;
    }

    public UUID getimageId(){
        return imageId;
    }

    public String getPriority() {
        return priority;
    } 

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setPlaceCategory(RoadCategory placeCategory) {
        this.placeCategory = placeCategory;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRoadCategory(RoadCategory roadCategory) {
       
    }

    public void setDamageSeverity(String damageSeverity) {
    this.damageSeverity = damageSeverity;
    }

    public void setuserName(String userName){

    }

    public void setimageId(UUID imageId){

    }

    public void setPriority(String priority) {
        
    }

    public String getRepairTeamName() {
    return repairTeamName;
}

public void setRepairTeamName(String repairTeamName) {
    this.repairTeamName = repairTeamName;
}

public String getRepairContactNumber() {
    return repairContactNumber;
}

public void setRepairContactNumber(String repairContactNumber) {
    this.repairContactNumber = repairContactNumber;
}

public List<CommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }

}