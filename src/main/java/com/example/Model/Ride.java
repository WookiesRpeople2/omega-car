package com.example.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Ride")
@Data
@NoArgsConstructor
public class Ride extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    private Car car;

    @Column(nullable = false)
    private String pick_up;

    @Column(nullable = false)
    private String drop_off;

    @Column(nullable = false)
    private LocalDateTime departure_time;

    @Column(nullable = false)
    private double price;
}
