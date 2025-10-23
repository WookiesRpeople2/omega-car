package com.example.service;

import java.util.UUID;

public class RideBookedEvent {
    private final UUID driverId;
    private final UUID bookingId;

    public RideBookedEvent(UUID driverId, UUID bookingId) {
        this.driverId = driverId;
        this.bookingId = bookingId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public UUID getBookingId() {
        return bookingId;
    }
}


