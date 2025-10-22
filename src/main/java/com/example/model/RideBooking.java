package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;


import java.util.UUID;
import java.util.List;


@Entity
@Table(name = "ride_booking")
public class RideBooking extends BaseModel {

    @Column(name = "ride_id", nullable = false)
    private UUID rideId;

    @Column(name = "seats_booked", nullable = false)
    private int seatsBooked;


    @ElementCollection
    @CollectionTable(
        name = "ride_booking_passengers",
        joinColumns = @JoinColumn(name = "booking_id")
    )
    @Column(name = "passenger_ids")
    private List<UUID> passengerIds;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    public UUID getRideId() {
        return rideId;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public List<UUID> getPassengerIds() {
        return passengerIds;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setRideId(UUID rideId) {
        this.rideId = rideId;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public void setPassengerIds(List<UUID> passengerIds) {
        this.passengerIds = passengerIds;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
