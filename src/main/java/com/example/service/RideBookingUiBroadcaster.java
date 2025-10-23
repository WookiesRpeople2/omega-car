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
}


