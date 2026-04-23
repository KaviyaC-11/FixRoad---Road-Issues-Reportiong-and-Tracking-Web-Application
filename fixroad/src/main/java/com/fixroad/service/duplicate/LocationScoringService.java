package com.fixroad.service.duplicate;

import org.springframework.stereotype.Service;

@Service
public class LocationScoringService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double MAX_DISTANCE_METERS = 300.0;

    // ==============================
    // LOCATION SCORE CALCULATION
    // ==============================
    public double calculateLocationScore(
            double lat1, double lon1,
            double lat2, double lon2) {

        double distance = calculateDistanceMeters(lat1, lon1, lat2, lon2);

        if (distance > MAX_DISTANCE_METERS) {
            return 0.0;
        }

        return 1 - (distance / MAX_DISTANCE_METERS);
    }

    // ==============================
    // DISTANCE CALCULATION (HAVERSINE)
    // ==============================
    private double calculateDistanceMeters(
            double lat1, double lon1,
            double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double distanceKm = EARTH_RADIUS_KM * c;

        return distanceKm * 1000; // convert to meters
    }
}