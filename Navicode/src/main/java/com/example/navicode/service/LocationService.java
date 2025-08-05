package com.example.navicode.service;

import com.example.navicode.dto.AddLocationRequest;
import com.example.navicode.dto.LocationResponse;
import com.example.navicode.dto.TypeResponse;
import com.example.navicode.model.StarbucksLocation;
import com.example.navicode.repository.StarbucksLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {
    
    @Autowired
    private StarbucksLocationRepository locationRepository;
    
    public TypeResponse getCoordType(String navicode) {
        if (navicode == null || !navicode.matches("\\d+")) {
            return new TypeResponse("2"); // static
        }
        
        List<Integer> types = locationRepository.findTypesByNavicodeStartingWith(navicode);
        if (!types.isEmpty()) {
            return new TypeResponse(types.get(0).toString());
        }
        return new TypeResponse("2"); // static
    }
    
    public List<LocationResponse> getCoordDynamic(String navicode, Double latitude, Double longitude) {
        List<StarbucksLocation> locations = locationRepository.findByNavicodeStartingWith(navicode);
        
        return locations.stream()
                .map(location -> {
                    double distance = calculateDistance(latitude, longitude, location.getLatitude(), location.getLongitude());
                    return new LocationWithDistance(location, distance);
                })
                .sorted(Comparator.comparing(LocationWithDistance::getDistance))
                .limit(5)
                .map(locationWithDistance -> new LocationResponse(
                        locationWithDistance.getLocation().getName(),
                        locationWithDistance.getLocation().getLatitude(),
                        locationWithDistance.getLocation().getLongitude()
                ))
                .collect(Collectors.toList());
    }
    
    public LocationResponse getCoordStatic(String navicode) {
        StarbucksLocation location = locationRepository.findByNavicode(navicode);
        if (location == null) {
            return null;
        }
        
        return new LocationResponse(
                location.getName(),
                location.getLatitude(),
                location.getLongitude()
        );
    }
    
    public StarbucksLocation addCoordLocation(AddLocationRequest request) {
        StarbucksLocation newLocation = new StarbucksLocation();
        newLocation.setName(request.getName());
        newLocation.setNavicode(request.getNavicode());
        newLocation.setLatitude(request.getLatitude());
        newLocation.setLongitude(request.getLongitude());
        newLocation.setType(request.getType());
        
        return locationRepository.save(newLocation);
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }
    
    private static class LocationWithDistance {
        private final StarbucksLocation location;
        private final double distance;
        
        public LocationWithDistance(StarbucksLocation location, double distance) {
            this.location = location;
            this.distance = distance;
        }
        
        public StarbucksLocation getLocation() {
            return location;
        }
        
        public double getDistance() {
            return distance;
        }
    }
} 