package com.example.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Configuration.SafeExecutor;
import com.example.Model.RideBooking;
import com.example.Repository.RideBookingRepository;

@Service
public class RideBookingService extends SafeExecutor {
    
    @Autowired
    private RideBookingRepository rideBookingRepository;

    public RideBooking createBooking(RideBooking booking) {
        booking.setStatus("pending");
        return executeSafely(rideBookingRepository::save, booking);
    }

    public Optional<RideBooking> getBooking(String id) {
        UUID uuid = UUID.fromString(id);
        return executeSafely(rideBookingRepository::findById, uuid);
    }

    public List<RideBooking> getAllBookings() {
        return executeSafely(rideBookingRepository::findAll);
    }

    public RideBooking updateBooking(String id, RideBooking updatedBooking) {
        UUID uuid = UUID.fromString(id);
        return executeSafely(rideBookingRepository::updateValues, uuid, updatedBooking);
    }

    public void deleteBooking(String id) {
        UUID uuid = UUID.fromString(id);
        executeSafely(rideBookingRepository::deleteAndReturn, uuid);
    }
    

}
