package com.example.service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

@Component
public class RideBookingUiBroadcaster {
    private final Set<Consumer<Object>> listeners = new CopyOnWriteArraySet<>();

    public Registration register(Consumer<Object> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void broadcast(Object event) {
        for (var listener : listeners) {
            try { listener.accept(event); } catch (Exception ignored) {}
        }
    }

    public interface Registration { void remove(); }

    public static class BookingStatusChangedEvent {
        private final java.util.UUID bookingId;
        private final String status;
        public BookingStatusChangedEvent(java.util.UUID bookingId, String status) {
            this.bookingId = bookingId; this.status = status;
        }
        public java.util.UUID getBookingId() { return bookingId; }
        public String getStatus() { return status; }
    }

    public static class NavigationTargetChangedEvent {
        private final java.util.UUID bookingId;
        private final java.util.UUID driverId;
        private final String target; // PICKUP or DROPOFF
        private final double lat;
        private final double lon;
        public NavigationTargetChangedEvent(java.util.UUID bookingId, java.util.UUID driverId, String target, double lat, double lon) {
            this.bookingId = bookingId; this.driverId = driverId; this.target = target; this.lat = lat; this.lon = lon;
        }
        public java.util.UUID getBookingId() { return bookingId; }
        public java.util.UUID getDriverId() { return driverId; }
        public String getTarget() { return target; }
        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }
}


