package com.example.Service;

import com.example.Model.Ride;
import com.example.Repository.RideRepository;
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
        return rideRepository.updateValues(id, updatedRide);
    }

    @Transactional
    public void deleteRide(UUID id) {
        if (!rideRepository.existsById(id)) {
            throw new EntityNotFoundException("Ride not found");
        }
        rideRepository.deleteById(id);
    }
}
