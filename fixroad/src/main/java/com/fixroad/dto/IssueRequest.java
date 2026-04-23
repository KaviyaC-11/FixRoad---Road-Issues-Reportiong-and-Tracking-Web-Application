package com.fixroad.dto;

import com.fixroad.model.RoadCategory;

public class IssueRequest {

    private String title;
    private String description;
    private String place;
    private RoadCategory roadCategory;
    private String damageSeverity;
    private Double latitude;
    private Double longitude;

    // Constructors

    public IssueRequest() {
    }

    public IssueRequest(String title, String description, String place,
                        RoadCategory roadCategory, String damageSeverity,
                        Double latitude, Double longitude) {
        this.title = title;
        this.description = description;
        this.place = place;
        this.roadCategory = roadCategory;
        this.damageSeverity = damageSeverity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public RoadCategory getRoadCategory() {
    return roadCategory;
}

public RoadCategory setRoadCategory(RoadCategory roadCategory) {
    return this.roadCategory = roadCategory;
}

 // Getter for damageSeverity
    public String getDamageSeverity() {
        return damageSeverity;
    }

    // Setter for damageSeverity
    public void setDamageSeverity(String damageSeverity) {
        this.damageSeverity = damageSeverity;
    }

}