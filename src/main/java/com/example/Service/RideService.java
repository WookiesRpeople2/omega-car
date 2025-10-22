package com.example.Service;

import com.example.Configuration.SafeExecutor;
import com.example.Model.Ride;
import com.example.Repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RideService extends SafeExecutor {
    private final RideRepository rideRepository;

    RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public Ride createRide(Ride ride) {
        return executeSafely(rideRepository::save, ride);
    }

    public Optional<Ride> getRide(String id) {
        UUID uuid = UUID.fromString(id);
        return executeSafely(rideRepository::findById, uuid);
    }

    public List<Ride> getAllRides() {
        return executeSafely(rideRepository::findAll);
    }

    public Ride updateRide(String id, Ride updatedRide) {
        UUID uuid = UUID.fromString(id);
        return executeSafely(rideRepository::updateValues, uuid, updatedRide);
    }

    public void deleteRide(String id) {
        UUID uuid = UUID.fromString(id);
        executeSafely(rideRepository::deleteAndReturn, uuid);
    }
}
