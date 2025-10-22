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

    @Column(name = "pick_up")
    private String pickUp;

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

    
}
