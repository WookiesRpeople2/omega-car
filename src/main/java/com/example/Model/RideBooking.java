package com.example.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "ride_booking")
@Data
public class RideBooking extends BaseModel {
    @Column(name = "ride_id", nullable = false)
    private UUID rideId;

    @Column(name = "seats_booked", nullable = false)
    private int seatsBooked;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;

    @ElementCollection
    @CollectionTable(
        name = "ride_booking_passengers",
        joinColumns = @JoinColumn(name = "booking_id")
    )
    @Column(name = "passenger_id")
    private List<UUID> passenger_id;

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }
}
