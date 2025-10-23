package com.example.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CarDto;
import com.example.model.Car;
import com.example.service.CarsService;
import com.example.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cars")
@Validated
public class CarsController extends BaseController<Car, CarDto> {

    private final CarsService carsService;
    private final UserRepository users;

    public CarsController(CarsService carsService, UserRepository users) {
        this.carsService = carsService;
        this.users = users;
    }

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<CarDto>> listCars() {
        List<CarDto> cars = carsService.findAll().stream()
            .map(car -> this.<Car, CarDto>toDto(car))
            .collect(Collectors.toList());
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<CarDto> getCar(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        return carsService.findById(uuid)
            .map(car -> this.<Car, CarDto>toDto(car))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<CarDto> create(@Valid @RequestBody CarDto request) {
        Car saved = carsService.save(fromDtoPartial(request));
        return ResponseEntity.created(URI.create("/api/cars/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<CarDto> update(@PathVariable("id") String id, @Valid @RequestBody CarDto request) throws IllegalAccessException {
        Car updated = fromDtoPartial(request);
        UUID uuid = UUID.fromString(id);
        Car saved = carsService.updateValues(uuid, updated);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        carsService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<CarDto> validateCar(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        Car car = carsService.findById(uuid).orElse(null);
        if (car == null) return ResponseEntity.notFound().build();
        car.setCarValidated(true);
        Car saved = carsService.save(car);
        return ResponseEntity.ok(toDto(saved));
    }

    // Driver self-service endpoints
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('Driver','Admin')")
    public ResponseEntity<List<CarDto>> myCars() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        return users.findByEmail(auth.getName())
            .map(u -> carsService.findAll().stream()
                .filter(c -> u.getId().equals(c.getOwnerId()))
                .map(c -> this.<Car, CarDto>toDto(c))
                .collect(Collectors.toList()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/my")
    @PreAuthorize("hasAnyRole('Driver','Admin')")
    public ResponseEntity<CarDto> createMyCar(@Valid @RequestBody CarDto request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        return users.findByEmail(auth.getName())
            .map(u -> {
                Car car = fromDtoPartial(request);
                car.setOwnerId(u.getId());
                Car saved = carsService.save(car);
                CarDto dto = this.<Car, CarDto>toDto(saved);
                return ResponseEntity.created(URI.create("/api/cars/" + dto.getId())).body(dto);
            })
            .orElseGet(() -> ResponseEntity.status(401).<CarDto>build());
    }
}


