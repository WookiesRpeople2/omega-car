package com.example.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.Car;
import com.example.repository.CarsRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CarsService {

    private final CarsRepository carsRepository;

    public CarsService(CarsRepository carsRepository) {
        this.carsRepository = carsRepository;
    }

    @Transactional
    public Car save(Car car) {
        return carsRepository.save(car);
    }

    @Transactional(readOnly = true)
    public Optional<Car> findById(UUID id) {
        return carsRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Car> findAll() {
        return carsRepository.findAll();
    }

    @Transactional
    public Car updateValues(UUID id, Car updatedCar) throws IllegalAccessException, EntityNotFoundException {
        if (!carsRepository.existsById(id)) {
            throw new EntityNotFoundException("Car not found");
        }
        return carsRepository.updateValues(id, updatedCar);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!carsRepository.existsById(id)) {
            throw new EntityNotFoundException("Car not found");
        }
        carsRepository.deleteById(id);
    }
}
