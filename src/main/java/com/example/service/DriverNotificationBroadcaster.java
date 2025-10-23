package com.example.service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DriverNotificationBroadcaster {
    private final Set<Consumer<RideBookedEvent>> listeners = new CopyOnWriteArraySet<>();

    public Registration register(Consumer<RideBookedEvent> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    @EventListener
    public void onRideBooked(RideBookedEvent event) {
        for (var l : listeners) {
            try { l.accept(event); } catch (Exception ignored) {}
        }
    }

    public interface Registration {
        void remove();
    }
}


