package com.example.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.RideBookingDto;
import com.example.model.RideBooking;
import com.example.service.RideBookingService;
import com.example.model.BookingStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
@Validated
@PreAuthorize("hasAnyRole('User','Driver','Admin')")
public class RideBookingController extends BaseController<RideBooking, RideBookingDto> {

    private final RideBookingService rideBookingService;
    private final com.example.service.DriverLocationService driverLocationService;
    private final com.example.service.RideService rideService;
    private final com.example.repository.UserRepository users;

    public RideBookingController(RideBookingService rideBookingService, com.example.service.DriverLocationService driverLocationService, com.example.repository.UserRepository users, com.example.service.RideService rideService) {
        this.rideBookingService = rideBookingService;
        this.driverLocationService = driverLocationService;
        this.users = users;
        this.rideService = rideService;
    }

    @GetMapping
    public ResponseEntity<List<RideBookingDto>> listBookings() {
        List<RideBookingDto> bookings = rideBookingService.getAllBookings().stream()
            .map(booking -> this.<RideBooking, RideBookingDto>toDto(booking))
            .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('Driver','Admin')")
    public ResponseEntity<List<RideBookingDto>> listMyBookings() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        var userOpt = users.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();
        var me = userOpt.get();
        List<RideBookingDto> bookings = rideBookingService.getByDriver(me.getId()).stream()
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
    @PreAuthorize("hasAnyRole('User','Admin')")
    public ResponseEntity<RideBookingDto> create(@Valid @RequestBody RideBookingDto request) {
        // Basic validation
        if (request.getRideId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getSeatsBooked() == null || request.getSeatsBooked() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        // Ensure driver is online if specified
        if (request.getDriverId() != null) {
            var loc = driverLocationService.getLocation(request.getDriverId());
            if (loc == null) {
                return ResponseEntity.status(409).build();
            }
        }
        RideBooking entity = fromDtoPartial(request);
        if (request.getSeatsBooked() != null) {
            entity.setSeatsBooked(request.getSeatsBooked());
        }
        RideBooking saved = rideBookingService.createBooking(entity);
        return ResponseEntity.created(URI.create("/api/bookings/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RideBookingDto> update(@PathVariable("id") String id, @Valid @RequestBody RideBookingDto request) throws IllegalAccessException {
        RideBooking updated = fromDtoPartial(request);
        if (request.getSeatsBooked() != null) {
            updated.setSeatsBooked(request.getSeatsBooked());
        }
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

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('Driver','Admin')")
    public ResponseEntity<RideBookingDto> accept(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        // Only the assigned driver can accept
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        var currentUserOpt = users.findByEmail(auth.getName());
        var bookingOpt = rideBookingService.getBooking(uuid);
        if (bookingOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (currentUserOpt.isEmpty() || bookingOpt.get().getDriverId() == null || !bookingOpt.get().getDriverId().equals(currentUserOpt.get().getId())) {
            return ResponseEntity.status(403).build();
        }
        RideBooking updated = new RideBooking();
        updated.setStatus(BookingStatus.CONFIRMED);
        try {
            RideBooking saved = rideBookingService.updateBooking(uuid, updated);
            return ResponseEntity.ok(toDto(saved));
        } catch (IllegalAccessException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/picked-up")
    @PreAuthorize("hasAnyRole('User','Admin')")
    public ResponseEntity<RideBookingDto> pickedUp(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        var opt = rideBookingService.getBooking(uuid);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var booking = opt.get();
        var ride = rideService.getRide(booking.getRideId()).orElse(null);
        if (ride == null) return ResponseEntity.notFound().build();
        // Broadcast navigation target changed to DROPOFF for assigned driver
        if (booking.getDriverId() != null && ride.getDropoffLat() != null && ride.getDropoffLon() != null) {
            rideBookingService.broadcastNavTarget(booking.getId(), booking.getDriverId(), "DROPOFF", ride.getDropoffLat(), ride.getDropoffLon());
        }
        return ResponseEntity.ok(toDto(booking));
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasAnyRole('Driver','Admin')")
    public ResponseEntity<RideBookingDto> decline(@PathVariable("id") String id) {
        UUID uuid = UUID.fromString(id);
        // Only the assigned driver can decline
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return ResponseEntity.status(401).build();
        var currentUserOpt = users.findByEmail(auth.getName());
        var bookingOpt = rideBookingService.getBooking(uuid);
        if (bookingOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (currentUserOpt.isEmpty() || bookingOpt.get().getDriverId() == null || !bookingOpt.get().getDriverId().equals(currentUserOpt.get().getId())) {
            return ResponseEntity.status(403).build();
        }
        RideBooking updated = new RideBooking();
        updated.setStatus(BookingStatus.CANCELLED);
        try {
            RideBooking saved = rideBookingService.updateBooking(uuid, updated);
            return ResponseEntity.ok(toDto(saved));
        } catch (IllegalAccessException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

