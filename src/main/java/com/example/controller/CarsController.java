package com.example.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CarDto;
import com.example.model.Car;
import com.example.service.CarsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cars")
@Validated
@PreAuthorize("hasRole('Admin')")
public class CarsController extends BaseController<Car, CarDto> {

    private final CarsService carsService;

    public CarsController(CarsService carsService) {
        this.carsService = carsService;
    }

    @GetMapping
    public ResponseEntity<List<CarDto>> listCars() {
        List<CarDto> cars = carsService.findAll().stream()
            .map(car -> this.<Car, CarDto>toDto(car))
            .collect(Collectors.toList());
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarDto> getCar(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        return carsService.findById(uuid)
            .map(car -> this.<Car, CarDto>toDto(car))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CarDto> create(@Valid @RequestBody CarDto request) {
        Car saved = carsService.save(fromDtoPartial(request));
        return ResponseEntity.created(URI.create("/api/cars/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarDto> update(@PathVariable("id") String id, @Valid @RequestBody CarDto request) throws IllegalAccessException {
        Car updated = fromDtoPartial(request);
        UUID uuid = UUID.fromString(id);
        Car saved = carsService.updateValues(uuid, updated);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        carsService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }
}


