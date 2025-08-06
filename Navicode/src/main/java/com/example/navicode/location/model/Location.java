package com.example.navicode.location.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String name;
    private String navicode;
    private double latitude;
    private double longitude;
    private int type;
}
