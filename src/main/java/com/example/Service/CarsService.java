package com.example.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.Attributes.Name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Configuration.SafeExecutor;
import com.example.Model.Car;
import com.example.Repository.CarsRepository;


@Service
public class CarsService extends SafeExecutor{
    @Autowired
    private CarsRepository carsRepository;

    public Car createCars(Car car) {
        return executeSafely(carsRepository::save, car);
    }

    public Optional<Car> getCar(String id){
        UUID uuid = UUID.fromString(id);
        return executeSafely(carsRepository::findById, uuid);
    }

    public List<Car> getAllCars(){
        return executeSafely(carsRepository::findAll);
    }

    public Car updateCar(String id, Car updatedCar){
        UUID uuid = UUID.fromString(id);
        return executeSafely(carsRepository::updateValues, uuid, updatedCar);
    }

    public void deleteCar(String id){
       UUID uuid = UUID.fromString(id);
       executeSafely(carsRepository::deleteAndReturn, uuid);
    }
}

