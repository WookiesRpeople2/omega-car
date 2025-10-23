package com.example.dto;

import java.util.List;
import java.util.UUID;

import com.example.model.BookingStatus;

public class RideBookingDto {
    private UUID id;
    private UUID rideId;
    private UUID driverId;
    private Integer seatsBooked;
    private BookingStatus bookingStatus;
    private List<UUID> passengerIds;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRideId() { return rideId; }
    public void setRideId(UUID rideId) { this.rideId = rideId; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public Integer getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(Integer seatsBooked) { this.seatsBooked = seatsBooked; }
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
    public List<UUID> getPassengerIds() { return passengerIds; }
    public void setPassengerIds(List<UUID> passengerIds) { this.passengerIds = passengerIds; }
}



