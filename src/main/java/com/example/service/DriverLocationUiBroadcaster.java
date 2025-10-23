package com.example.service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;


@Component
public class DriverLocationUiBroadcaster {
    private final Set<Consumer<Object>> listeners = new CopyOnWriteArraySet<>();

    public Registration register(Consumer<Object> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void broadcast(Object event) {
        for (var listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception ignored) {
            }
        }
    }

    public interface Registration {
        void remove();
    }

    public static class DriverOfflineEvent {
        private final java.util.UUID driverId;
        public DriverOfflineEvent(java.util.UUID driverId) { this.driverId = driverId; }
        public java.util.UUID getDriverId() { return driverId; }
    }
}


