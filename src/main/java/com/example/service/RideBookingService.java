package com.example.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.BookingStatus;
import com.example.model.RideBooking;
import com.example.model.NotificationType;
import com.example.repository.RideBookingRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RideBookingService {
    
    private final RideBookingRepository rideBookingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    public RideBookingService(RideBookingRepository rideBookingRepository, ApplicationEventPublisher eventPublisher, NotificationService notificationService) {
        this.rideBookingRepository = rideBookingRepository;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    @Transactional
    public RideBooking createBooking(RideBooking booking) {
        booking.setStatus(BookingStatus.PENDING);
        RideBooking saved = rideBookingRepository.save(booking);
        if (saved.getDriverId() != null) {
            eventPublisher.publishEvent(new RideBookedEvent(saved.getDriverId(), saved.getId()));
            notificationService.create(
                saved.getDriverId(),
                NotificationType.RIDE_BOOKED,
                "New ride booking",
                "A passenger created a booking and assigned you as driver",
                saved.getId()
            );
        }
        return saved;
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
        RideBooking saved = rideBookingRepository.updateValues(id, updatedBooking);
        if (saved.getDriverId() != null) {
            if (saved.getStatus() == BookingStatus.PENDING) {
                eventPublisher.publishEvent(new RideBookedEvent(saved.getDriverId(), saved.getId()));
            } else if (saved.getStatus() == BookingStatus.CONFIRMED) {
                // Notify passengers of acceptance
                if (saved.getPassengerIds() != null) {
                    for (var pid : saved.getPassengerIds()) {
                        notificationService.create(pid, NotificationType.RIDE_ACCEPTED, "Ride accepted", "Your ride booking was accepted", saved.getId());
                    }
                }
            } else if (saved.getStatus() == BookingStatus.CANCELLED) {
                // Notify passengers of decline
                if (saved.getPassengerIds() != null) {
                    for (var pid : saved.getPassengerIds()) {
                        notificationService.create(pid, NotificationType.RIDE_DECLINED, "Ride declined", "Your ride booking was declined", saved.getId());
                    }
                }
            }
        }
        return saved;
    }

    @Transactional
    public void deleteBooking(UUID id) {
        if (!rideBookingRepository.existsById(id)) {
            throw new EntityNotFoundException("RideBooking not found");
        }
        rideBookingRepository.deleteById(id);
    }
}
