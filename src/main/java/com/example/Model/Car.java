package com.example.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
public class Car extends BaseModel {
    @Size(min=1, message = "{EMPTY_INPUT}")
    private String make;
    
    @Size(min=1, message = "{EMPTY_INPUT}")
    private String model;
    
    @Size(min=1, message = "{EMPTY_INPUT}")
    private String license_plate;
    
    @Column(columnDefinition = "boolean default false")
    private boolean car_validated;

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

    public String getLicense_plate() {
        return license_plate;
    }

    public void setLicense_plate(String license_plate) {
        this.license_plate = license_plate;
    }

    public boolean isCar_validated() {
        return car_validated;
    }

    public void setCar_validated(boolean car_validated) {
        this.car_validated = car_validated;
    }

}
