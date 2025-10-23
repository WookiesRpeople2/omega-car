package com.example.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.DriverLocationEvent;
import com.example.service.DriverLocationKafka;
import com.example.service.DriverLocationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/driver-locations")
@Validated
public class DriverLocationController {

    private final DriverLocationKafka producer;
    private final DriverLocationService service;

    public DriverLocationController(DriverLocationKafka producer, DriverLocationService service) {
        this.producer = producer;
        this.service = service;
    }

    @PostMapping("/{driverId}")
    @PreAuthorize("hasRole('Driver') or hasRole('Admin')")
    public ResponseEntity<Void> postLocation(@PathVariable("driverId") UUID driverId, @Valid @RequestBody DriverLocationEvent body) {
        body.setDriverId(driverId);
        if (body.getTimestamp() == null) {
            body.setTimestamp(Instant.now());
        }
        producer.publish(body);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{driverId}/offline")
    @PreAuthorize("hasRole('Driver') or hasRole('Admin')")
    public ResponseEntity<Void> goOffline(@PathVariable("driverId") UUID driverId) {
        producer.publishOffline(driverId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<List<DriverLocationEvent>> listActiveDrivers() {
        return ResponseEntity.ok(service.getAllActiveDrivers());
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverLocationEvent> getDriver(@PathVariable("driverId") UUID driverId) {
        DriverLocationEvent e = service.getLocation(driverId);
        if (e == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(e);
    }
}


