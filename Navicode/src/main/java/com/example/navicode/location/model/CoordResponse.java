package com.example.navicode.location.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordResponse {
    private String name;
    private double latitude;
    private double longitude;
}
