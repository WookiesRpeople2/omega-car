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

import com.example.dto.RideDto;
import com.example.model.Ride;
import com.example.service.RideService;
import com.example.service.CarsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rides")
@Validated
@PreAuthorize("hasAnyRole('User','Driver','Admin')")
public class RideController extends BaseController<Ride, RideDto> {

    private final RideService rideService;
    private final CarsService carsService;

    public RideController(RideService rideService, CarsService carsService) {
        this.rideService = rideService;
        this.carsService = carsService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin')")
    public ResponseEntity<List<RideDto>> listRides() {
        List<RideDto> rides = rideService.getAllRides().stream()
            .map(ride -> this.<Ride, RideDto>toDto(ride))
            .collect(Collectors.toList());
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideDto> getRide(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        return rideService.getRide(uuid)
            .map(ride -> this.<Ride, RideDto>toDto(ride))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RideDto> create(@Valid @RequestBody RideDto request) {
        Ride ride = fromDtoPartial(request);
        // Map carId -> car association if provided
        if (request.getCarId() != null) {
            carsService.findById(request.getCarId()).ifPresent(ride::setCar);
        }
        Ride saved = rideService.createRide(ride);
        return ResponseEntity.created(URI.create("/api/rides/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RideDto> update(@PathVariable("id") String id, @Valid @RequestBody RideDto request) throws IllegalAccessException {
        Ride updated = fromDtoPartial(request);
        if (request.getCarId() != null) {
            carsService.findById(request.getCarId()).ifPresent(updated::setCar);
        }
        UUID uuid = UUID.fromString(id);
        Ride saved = rideService.updateRide(uuid, updated);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        rideService.deleteRide(uuid);
        return ResponseEntity.noContent().build();
    }
}

