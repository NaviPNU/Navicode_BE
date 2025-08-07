package com.example.navicode.location.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String navicode;
    private double latitude;
    private double longitude;
    private int type;
    private String username;
    private LocalDateTime expire;

    public Location(String name, String navicode, double latitude, double longitude, int type, String username, LocalDateTime expire) {
        this.name = name;
        this.navicode = navicode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.username = username;
        this.expire = expire;
    }
}