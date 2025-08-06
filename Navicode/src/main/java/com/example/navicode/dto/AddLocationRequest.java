package com.example.navicode.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddLocationRequest {
    private String name;
    private String navicode;
    private Double latitude;
    private Double longitude;
    private Integer type;
    private String username;
} 