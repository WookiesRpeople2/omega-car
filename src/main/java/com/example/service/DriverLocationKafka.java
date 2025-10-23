package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.dto.DriverLocationEvent;

@Component
public class DriverLocationKafka {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DriverLocationService driverLocationService;
    private final DriverLocationUiBroadcaster broadcaster;
    private final String topic;

    public DriverLocationKafka(
        KafkaTemplate<String, Object> kafkaTemplate,
        DriverLocationService driverLocationService,
        DriverLocationUiBroadcaster broadcaster,
        @Value("${app.kafka.topics.driver-location:driver-location}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.driverLocationService = driverLocationService;
        this.broadcaster = broadcaster;
        this.topic = topic;
    }

    public void publish(DriverLocationEvent event) {
        kafkaTemplate.send(topic, event.getDriverId().toString(), event);
    }

    public void publishOffline(java.util.UUID driverId) {

        DriverLocationEvent evt = new DriverLocationEvent(driverId, 0, 0, java.time.Instant.now());
        kafkaTemplate.send(topic + "-offline", driverId.toString(), evt);
    }

    @KafkaListener(topics = "${app.kafka.topics.driver-location:driver-location}", groupId = "${app.kafka.group-id:omega-car}")
    public void onMessage(DriverLocationEvent event) {
        driverLocationService.upsertLocation(event);
        broadcaster.broadcast(event);
    }

    @KafkaListener(topics = "${app.kafka.topics.driver-location:driver-location}-offline", groupId = "${app.kafka.group-id:omega-car}")
    public void onOffline(DriverLocationEvent event) {
        driverLocationService.markOffline(event.getDriverId());
        broadcaster.broadcast(new DriverLocationUiBroadcaster.DriverOfflineEvent(event.getDriverId()));
    }
}


