package com.fixroad.service.priority;

import com.fixroad.model.Issue;
import com.fixroad.model.RoadCategory;
import org.springframework.stereotype.Service;

@Service
public class PriorityScoringService {

    public double calculatePriorityScore(Issue issue){

        double damageScore = getDamageScore(issue.getDamageSeverity());
        double roadScore = getRoadScore(issue.getRoadCategory());
        double voteScore = getVoteScore(issue.getVoteCount());

        return (0.50 * damageScore) +
               (0.30 * roadScore) +
               (0.20 * voteScore);
    }

    public String getPriorityLevel(double score){

    if(score >= 3.5){
        return "CRITICAL";
    }

    if(score >= 2.5){
        return "HIGH";
    }

    if(score >= 1.5){
        return "MEDIUM";
    }

    return "LOW";
}


    private double getDamageScore(String severity){

        if(severity == null) return 1;

        switch(severity.toUpperCase()){
            case "LOW": return 1;
            case "MEDIUM": return 2;
            case "HIGH": return 3;
            case "CRITICAL": return 4;
            default: return 1;
        }

    }


    private double getRoadScore(RoadCategory category){

        if(category == null) return 1;

        switch(category){
            case HIGHWAY: return 4;
            case MAIN_ROAD: return 3;
            case STREET: return 2;
            case SCHOOL_ZONE: return 1;
            default: return 1;
        }

    }


    private double getVoteScore(int votes){

        if(votes >= 30) return 4;
        if(votes >= 15) return 3;
        if(votes >= 5) return 2;

        return 1;
    }

}