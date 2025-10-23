package com.example.repository;

import org.springframework.stereotype.Repository;

import com.example.model.RideBooking;

import java.util.UUID;

@Repository
public interface RideBookingRepository extends BaseRepository<RideBooking, UUID> {
    java.util.List<RideBooking> findByDriverIdOrderByCreatedAtDesc(UUID driverId);
}
