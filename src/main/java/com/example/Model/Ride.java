package com.example.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "Ride")
@Data
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ride_id")
    private int id;

//    @ManyToOne
//    @JoinColumn(name = "car_id")
//    private Car car;

    @Column(nullable = false)
    private String pick_up;

    @Column(nullable = false)
    private String drop_off;

    @Column(nullable = false)
    private Timestamp departure_time;

    @Column(nullable = false)
    private int total_seats;

    @Column(nullable = false)
    private float price;

    @Column(nullable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;


}
