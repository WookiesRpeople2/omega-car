package com.example.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.BookingStatus;
import com.example.model.RideBooking;
import com.example.repository.RideBookingRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RideBookingService {
    
    private final RideBookingRepository rideBookingRepository;

    public RideBookingService(RideBookingRepository rideBookingRepository) {
        this.rideBookingRepository = rideBookingRepository;
    }

    @Transactional
    public RideBooking createBooking(RideBooking booking) {
        booking.setStatus(BookingStatus.PENDING);
        return rideBookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Optional<RideBooking> getBooking(UUID id) {
        return rideBookingRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<RideBooking> getAllBookings() {
        return rideBookingRepository.findAll();
    }

    @Transactional
    public RideBooking updateBooking(UUID id, RideBooking updatedBooking) throws IllegalAccessException {
        if (!rideBookingRepository.existsById(id)) {
            throw new EntityNotFoundException("RideBooking not found");
        }
        return rideBookingRepository.updateValues(id, updatedBooking);
    }

    @Transactional
    public void deleteBooking(UUID id) {
        if (!rideBookingRepository.existsById(id)) {
            throw new EntityNotFoundException("RideBooking not found");
        }
        rideBookingRepository.deleteById(id);
    }
}
