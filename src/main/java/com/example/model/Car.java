package com.example.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;


@Entity
public class Car extends BaseModel {
    @Column(name = "owner_id")
    private java.util.UUID ownerId;
    
    @Size(min=1, message = "{EMPTY_INPUT}")
    private String make;
    
    @Size(min=1, message = "{EMPTY_INPUT}")
    private String model;
    
    @Size(min=1, message = "{EMPTY_INPUT}")
    @Column(name = "license_plate")
    private String licensePlate;
    
    @Column(name = "car_validated", columnDefinition = "boolean default false")
    private boolean carValidated;

    public java.util.UUID getOwnerId() {
    return ownerId;
    }

    public void setOwnerId(java.util.UUID ownerId) {
        this.ownerId = ownerId;
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

    public boolean isCarValidated() {
        return carValidated;
    }

    public void setCarValidated(boolean carValidated) {
        this.carValidated = carValidated;
    }

}
