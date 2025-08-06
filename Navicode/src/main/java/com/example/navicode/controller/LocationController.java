package com.example.navicode.controller;

import com.example.navicode.dto.AddLocationRequest;
import com.example.navicode.dto.LocationResponse;
import com.example.navicode.dto.TypeResponse;
import com.example.navicode.model.Location;
import com.example.navicode.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "*")
public class LocationController {
    
    @Autowired
    private LocationService locationService;
    
    @GetMapping("/coord_type")
    public ResponseEntity<TypeResponse> getCoordType(@RequestParam String navicode) {
        TypeResponse response = locationService.getCoordType(navicode);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get_coord_dynamic")
    public ResponseEntity<?> getCoordDynamic(
            @RequestParam String navicode,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        if (navicode == null || latitude == null || longitude == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing parameters"));
        }
        
        List<LocationResponse> locations = locationService.getCoordDynamic(navicode, latitude, longitude);
        
        if (locations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No matching navicode"));
        }
        
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/get_coord_static")
    public ResponseEntity<?> getCoordStatic(@RequestParam String navicode) {
        LocationResponse location = locationService.getCoordStatic(navicode);
        
        if (location == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "404"));
        }
        
        return ResponseEntity.ok(location);
    }
    
    @PostMapping("/add_coord_location")
    public ResponseEntity<?> addCoordLocation(@RequestBody AddLocationRequest request) {
        try {
            if (request.getName() == null || request.getNavicode() == null || 
                request.getLatitude() == null || request.getLongitude() == null || 
                request.getType() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }
            
            Location savedLocation = locationService.addCoordLocation(request);
            return ResponseEntity.ok(Map.of("message", "location added success"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 