package com.fixroad.service.duplicate;

public interface SimilarityEngine {

    double calculateSimilarity(String text1, String text2);

    double levenshteinSimilarity(String s1, String s2);

}