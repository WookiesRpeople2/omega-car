package com.example.service;

import com.example.model.Ride;
import com.example.repository.RideRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RideService {
    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional
    public Ride createRide(Ride ride) {
        Double computed = computePriceForRide(ride);
        if (computed != null) {
            ride.setPrice(computed);
        }
        return rideRepository.save(ride);
    }

    @Transactional(readOnly = true)
    public Optional<Ride> getRide(UUID id) {
        return rideRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Transactional
    public Ride updateRide(UUID id, Ride updatedRide) throws IllegalAccessException, EntityNotFoundException {
        if (!rideRepository.existsById(id)) {
            throw new EntityNotFoundException("Ride not found");
        }
        Ride saved = rideRepository.updateValues(id, updatedRide);
        Double computed = computePriceForRide(saved);
        if (computed != null && Double.compare(saved.getPrice(), computed) != 0) {
            saved.setPrice(computed);
            saved = rideRepository.save(saved);
        }
        return saved;
    }

    @Transactional
    public void deleteRide(UUID id) {
        if (!rideRepository.existsById(id)) {
            throw new EntityNotFoundException("Ride not found");
        }
        rideRepository.deleteById(id);
    }

    private static Double computePriceForRide(Ride ride) {
        if (ride == null) return null;
        Double plat = ride.getPickupLat();
        Double plon = ride.getPickupLon();
        Double dlat = ride.getDropoffLat();
        Double dlon = ride.getDropoffLon();
        if (plat != null && plon != null && dlat != null && dlon != null) {
            double km = haversineKm(plat, plon, dlat, dlon);
            return estimatePrice(km);
        }
        
        String pick = ride.getPickUp();
        String drop = ride.getDropOff();
        if (pick == null || drop == null) return null;
        try {
            String[] p = pick.split(",");
            String[] d = drop.split(",");
            if (p.length < 2 || d.length < 2) return null;
            double fplat = Double.parseDouble(p[0].trim());
            double fplon = Double.parseDouble(p[1].trim());
            double fdlat = Double.parseDouble(d[0].trim());
            double fdlon = Double.parseDouble(d[1].trim());
            double km = haversineKm(fplat, fplon, fdlat, fdlon);
            return estimatePrice(km);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double estimatePrice(double km) {
        double base = 2.5; // base fare
        double perKm = 1.2; // per km rate
        double price = base + perKm * km;
        return Math.round(price * 100.0) / 100.0;
    }
}
