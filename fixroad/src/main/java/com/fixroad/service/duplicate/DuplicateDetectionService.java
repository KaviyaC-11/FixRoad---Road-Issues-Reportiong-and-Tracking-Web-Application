package com.fixroad.service.duplicate;

import com.fixroad.dto.DuplicateResultDTO;
import com.fixroad.model.Issue;
import com.fixroad.repository.IssueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DuplicateDetectionService {

    private final IssueRepository issueRepository;
    private final SimilarityEngine similarityEngine;
    private final LocationScoringService locationScoringService;

    private static final double DUPLICATE_THRESHOLD = 0.75;

    // ==============================
    // CONSTRUCTOR
    // ==============================
    public DuplicateDetectionService(
            IssueRepository issueRepository,
            SimilarityEngine similarityEngine,
            LocationScoringService locationScoringService) {

        this.issueRepository = issueRepository;
        this.similarityEngine = similarityEngine;
        this.locationScoringService = locationScoringService;
    }

    // ==============================
    // DUPLICATE CHECK LOGIC
    // ==============================
    public DuplicateResultDTO checkDuplicate(
            String title,
            String description,
            double latitude,
            double longitude) {

        // ---------- BOUNDING BOX CALCULATION ----------

        double latRange = 1 / 111.0;
        double lonRange = 1 / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latRange;
        double maxLat = latitude + latRange;
        double minLon = longitude - lonRange;
        double maxLon = longitude + lonRange;

        List<Issue> issues = issueRepository.findIssuesInBoundingBox(
                minLat, maxLat, minLon, maxLon);

        double highestScore = 0.0;
        Issue bestMatch = null;

        // ---------- ITERATE EXISTING ISSUES ----------
        for (Issue existing : issues) {

            // ---------- TEXT NORMALIZATION ----------
            String normalizedTitle = normalizeText(title);
            String normalizedExistingTitle = normalizeText(existing.getTitle());

            // ---------- COSINE SIMILARITY ----------
            double cosineScore = similarityEngine.calculateSimilarity(
                    normalizedTitle,
                    normalizedExistingTitle);

            // ---------- FUZZY (LEVENSHTEIN) SIMILARITY ----------
            double fuzzyScore = similarityEngine.levenshteinSimilarity(
                    normalizedTitle,
                    normalizedExistingTitle);

            // ---------- TITLE SCORE COMBINATION ----------
            double titleScore = (0.7 * cosineScore) + (0.3 * fuzzyScore);

            // ---------- LOCATION SCORE ----------
            double locationScore = locationScoringService.calculateLocationScore(
                    latitude, longitude,
                    existing.getLatitude(), existing.getLongitude());

            double finalScore;

            // ---------- DESCRIPTION CHECK ----------
            boolean hasDescription =
                    description != null && !description.trim().isEmpty() &&
                    existing.getDescription() != null &&
                    !existing.getDescription().trim().isEmpty();

            // ---------- FINAL SCORE WITH DESCRIPTION ----------
            if (hasDescription) {

                String normalizedDescription = normalizeText(description);
                String normalizedExistingDescription =
                        normalizeText(existing.getDescription());

                double descriptionScore = similarityEngine.calculateSimilarity(
                        normalizedDescription,
                        normalizedExistingDescription);

                finalScore =
                        (0.40 * titleScore) +
                        (0.20 * descriptionScore) +
                        (0.40 * locationScore);

            } else {

                // ---------- FINAL SCORE WITHOUT DESCRIPTION ----------
                finalScore =
                        (0.60 * titleScore) +
                        (0.40 * locationScore);
            }

            // ---------- TRACK BEST MATCH ----------
            if (finalScore > highestScore) {
                highestScore = finalScore;
                bestMatch = existing;
            }
        }

        // ---------- DUPLICATE DECISION ----------
        boolean isDuplicate = highestScore >= DUPLICATE_THRESHOLD;

        return new DuplicateResultDTO(isDuplicate, highestScore, bestMatch);
    }

    // ==============================
    // TEXT NORMALIZATION
    // ==============================
    private String normalizeText(String text) {

        if (text == null) return "";

        text = text.toLowerCase();

        text = text.replaceAll("[^a-z0-9 ]", "");

        text = text.replaceAll("\\s+", " ").trim();

        text = text.replace("bus stand", "bus stop");
        text = text.replace("bus station", "bus stop");
        text = text.replace("main road", "road");

        return text;
    }
}