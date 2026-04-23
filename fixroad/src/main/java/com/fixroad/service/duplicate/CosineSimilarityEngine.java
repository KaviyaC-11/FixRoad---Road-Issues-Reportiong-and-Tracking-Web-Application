package com.fixroad.service.duplicate;
import org.springframework.stereotype.Service;

@Service
public class CosineSimilarityEngine implements SimilarityEngine {

    // ==============================
    // COSINE SIMILARITY CALCULATION
    // ==============================
    @Override
    public double calculateSimilarity(String text1, String text2) {

        
        if (text1 == null || text2 == null)
            return 0.0;

        text1 = text1.toLowerCase();
        text2 = text2.toLowerCase();

        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");

        java.util.Map<String, Integer> freq1 = new java.util.HashMap<>();
        java.util.Map<String, Integer> freq2 = new java.util.HashMap<>();

        for (String word : words1) {
            freq1.put(word, freq1.getOrDefault(word, 0) + 1);
        }

        for (String word : words2) {
            freq2.put(word, freq2.getOrDefault(word, 0) + 1);
        }

        java.util.Set<String> allWords = new java.util.HashSet<>();
        allWords.addAll(freq1.keySet());
        allWords.addAll(freq2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {

            int v1 = freq1.getOrDefault(word, 0);
            int v2 = freq2.getOrDefault(word, 0);

            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0 || norm2 == 0)
            return 0.0;

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // =====================================
    // LEVENSHTEIN SIMILARITY CALCULATION
    // =====================================
    @Override
    public double levenshteinSimilarity(String s1, String s2) {

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        if (maxLength == 0)
            return 1.0;

        return 1.0 - ((double) distance / maxLength);
    }

    // =====================================
    // LEVENSHTEIN DISTANCE (EDIT DISTANCE)
    // =====================================
    private int levenshteinDistance(String s1, String s2) {

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++)
            dp[i][0] = i;

        for (int j = 0; j <= s2.length(); j++)
            dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {

                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,     
                                dp[i][j - 1] + 1       
                        ),
                        dp[i - 1][j - 1] + cost     
                );
            }
        }
        return dp[s1.length()][s2.length()];
    }
}