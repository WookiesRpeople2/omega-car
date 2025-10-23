package com.example.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;


@Entity
public class Ride extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Car car;

    @Column(name = "pickup_name")
    private String pickupName;

    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Column(name = "pickup_lon")
    private Double pickupLon;

    @Column(name = "pick_up")
    private String pickUp;

    @Column(name = "dropoff_name")
    private String dropoffName;

    @Column(name = "dropoff_lat")
    private Double dropoffLat;

    @Column(name = "dropoff_lon")
    private Double dropoffLon;

    @Column(name = "drop_off")
    private String dropOff;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private double price;

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getPickUp() {
        return pickUp;
    }

    public void setPickUp(String pickUp) {
        this.pickUp = pickUp;
    }

    public String getDropOff() {
        return dropOff;
    }

    public void setDropOff(String dropOff) {
        this.dropOff = dropOff;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPickupName() {
        return pickupName;
    }

    public void setPickupName(String pickupName) {
        this.pickupName = pickupName;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLon() {
        return pickupLon;
    }

    public void setPickupLon(Double pickupLon) {
        this.pickupLon = pickupLon;
    }

    public String getDropoffName() {
        return dropoffName;
    }

    public void setDropoffName(String dropoffName) {
        this.dropoffName = dropoffName;
    }

    public Double getDropoffLat() {
        return dropoffLat;
    }

    public void setDropoffLat(Double dropoffLat) {
        this.dropoffLat = dropoffLat;
    }

    public Double getDropoffLon() {
        return dropoffLon;
    }

    public void setDropoffLon(Double dropoffLon) {
        this.dropoffLon = dropoffLon;
    }

    
}
