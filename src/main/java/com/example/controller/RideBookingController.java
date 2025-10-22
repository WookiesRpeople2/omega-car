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

import com.example.dto.RideBookingDto;
import com.example.model.RideBooking;
import com.example.service.RideBookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
@Validated
@PreAuthorize("hasRole('Admin')")
public class RideBookingController extends BaseController<RideBooking, RideBookingDto> {

    private final RideBookingService rideBookingService;

    public RideBookingController(RideBookingService rideBookingService) {
        this.rideBookingService = rideBookingService;
    }

    @GetMapping
    public ResponseEntity<List<RideBookingDto>> listBookings() {
        List<RideBookingDto> bookings = rideBookingService.getAllBookings().stream()
            .map(booking -> this.<RideBooking, RideBookingDto>toDto(booking))
            .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideBookingDto> getBooking(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        return rideBookingService.getBooking(uuid)
            .map(booking -> this.<RideBooking, RideBookingDto>toDto(booking))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RideBookingDto> create(@Valid @RequestBody RideBookingDto request) {
        RideBooking saved = rideBookingService.createBooking(fromDtoPartial(request));
        return ResponseEntity.created(URI.create("/api/bookings/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RideBookingDto> update(@PathVariable("id") String id, @Valid @RequestBody RideBookingDto request) throws IllegalAccessException {
        RideBooking updated = fromDtoPartial(request);
        UUID uuid = UUID.fromString(id);
        RideBooking saved = rideBookingService.updateBooking(uuid, updated);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        rideBookingService.deleteBooking(uuid);
        return ResponseEntity.noContent().build();
    }
}

