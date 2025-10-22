package com.example.Repository;

import org.springframework.stereotype.Repository;

import com.example.Model.RideBooking;

import java.util.UUID;
import java.util.List;

@Repository
public interface RideBookingRepository extends BaseRepository<RideBooking, UUID> {
}
