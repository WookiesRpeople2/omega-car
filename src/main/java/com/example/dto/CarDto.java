package com.example.dto;

import java.util.UUID;

public class CarDto {
    private UUID id;
    private String make;
    private String model;
    private String licensePlate;
    private UUID ownerId;
    private boolean carValidated;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isCarValidated() {
        return carValidated;
    }

    public void setCarValidated(boolean carValidated) {
        this.carValidated = carValidated;
    }
}


