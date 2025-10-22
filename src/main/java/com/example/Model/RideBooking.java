package com.example.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

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
    @Column(name = "passenger_id")
    private List<UUID> passenger_id;


    @Pattern(regexp="pending|confirmed|cancelled", message="{BAD_STATUS}")
    private String status;

    // Getters
    public UUID getRideId() {
        return rideId;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public List<UUID> getPassenger_id() {
        return passenger_id;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setRideId(UUID rideId) {
        this.rideId = rideId;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public void setPassenger_id(List<UUID> passenger_id) {
        this.passenger_id = passenger_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
