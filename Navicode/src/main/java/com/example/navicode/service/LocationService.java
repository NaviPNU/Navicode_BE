package com.example.navicode.service;

import com.example.navicode.dto.AddLocationRequest;
import com.example.navicode.dto.LocationResponse;
import com.example.navicode.dto.TypeResponse;
import com.example.navicode.model.Location;
import com.example.navicode.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {
    
    @Autowired
    private LocationRepository locationRepository;
    
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
        List<Location> locations = locationRepository.findByNavicodeStartingWith(navicode);
        
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
        Location location = locationRepository.findByNavicode(navicode);
        if (location == null) {
            return null;
        }
        
        return new LocationResponse(
                location.getName(),
                location.getLatitude(),
                location.getLongitude()
        );
    }

    public Location addCoordLocation(AddLocationRequest request) {
        Location newLocation = new Location();
        newLocation.setName(request.getName());
        newLocation.setNavicode(request.getNavicode());
        newLocation.setLatitude(request.getLatitude());
        newLocation.setLongitude(request.getLongitude());
        newLocation.setType(request.getType());
        
        // username과 expire 설정
        if (request.getType() == 1) {
            // type 1 (동적)은 무조건 admin
            newLocation.setUsername("admin");
            // admin은 1년 후 만료
            newLocation.setExpire(LocalDate.now().plusYears(1));
        } else if (request.getType() == 2) {
            // type 2 (정적)는 사용자 이름으로 저장
            newLocation.setUsername(request.getUsername() != null ? request.getUsername() : "unknown");
            // admin이 포함된 사용자는 1년, 일반 사용자는 3일 후 만료
            if (request.getUsername() != null && request.getUsername().contains("admin")) {
                newLocation.setExpire(LocalDate.now().plusYears(1));
            } else {
                newLocation.setExpire(LocalDate.now().plusDays(3));
            }
        }
        
        return locationRepository.save(newLocation);
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }
    
    private static class LocationWithDistance {
        private final Location location;
        private final double distance;
        
        public LocationWithDistance(Location location, double distance) {
            this.location = location;
            this.distance = distance;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public double getDistance() {
            return distance;
        }
    }
} 