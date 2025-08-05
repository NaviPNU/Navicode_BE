package com.example.navicode.location.service;

import com.example.navicode.location.model.Location;
import com.example.navicode.location.model.CoordResponse;
import com.example.navicode.location.model.AddLocationRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {
    
    private List<Location> locations = new ArrayList<>();
    private static final String CSV_FILE_PATH = "starbucks.csv";
    private String csvFilePath = CSV_FILE_PATH;
    
    public LocationService() {
        loadLocationsFromCSV();
    }
    
    // 테스트용 생성자
    public LocationService(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        loadLocationsFromCSV();
    }
    
    private void loadLocationsFromCSV() {
        try {
            File file = new File(csvFilePath);
            if (!file.exists()) {
                return;
            }
            
            try (Reader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                
                for (CSVRecord record : csvParser) {
                    Location location = new Location(
                        record.get(0), // name
                        record.get(1), // navicode
                        Double.parseDouble(record.get(2)), // latitude
                        Double.parseDouble(record.get(3)), // longitude
                        Integer.parseInt(record.get(4)) // type
                    );
                    locations.add(location);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveLocationsToCSV() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(csvFilePath), Charset.forName("UTF-8")))) {

            // Write header
            writer.println("name,navicode,latitude,longitude,type");
            
            // Write data
            for (Location location : locations) {
                writer.printf("%s,%s,%.6f,%.6f,%d%n",
                    location.getName(),
                    location.getNavicode(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getType()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, Object> getCoordType(String navicode) {
        Map<String, Object> response = new HashMap<>();
        
        if (navicode != null && navicode.matches("\\d+")) {
            Optional<Location> result = locations.stream()
                .filter(loc -> loc.getNavicode().startsWith(navicode))
                .findFirst();
            
            if (result.isPresent()) {
                response.put("type", String.valueOf(result.get().getType()));
            } else {
                response.put("type", "2");
            }
        } else {
            response.put("type", "2"); // 1 dynamic, 2 static
        }
        
        return response;
    }
    
    public List<CoordResponse> getCoordDynamic(String navicode, double latitude, double longitude) {
        List<Location> matchingLocations = locations.stream()
            .filter(loc -> loc.getNavicode().startsWith(navicode))
            .collect(Collectors.toList());
        
        if (matchingLocations.isEmpty()) {
            throw new RuntimeException("No matching navicode");
        }
        
        // Calculate distances and sort by nearest
        List<Location> nearest = matchingLocations.stream()
            .map(loc -> {
                double distance = calculateDistance(latitude, longitude, loc.getLatitude(), loc.getLongitude());
                return new AbstractMap.SimpleEntry<>(loc, distance);
            })
            .sorted(Map.Entry.comparingByValue())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        return nearest.stream()
            .map(loc -> new CoordResponse(loc.getName(), loc.getLatitude(), loc.getLongitude()))
            .collect(Collectors.toList());
    }
    
    public CoordResponse getCoordStatic(String navicode) {
        Optional<Location> result = locations.stream()
            .filter(loc -> navicode.equals(loc.getNavicode()))
            .findFirst();
        
        if (result.isPresent()) {
            Location location = result.get();
            return new CoordResponse(location.getName(), location.getLatitude(), location.getLongitude());
        } else {
            throw new RuntimeException("404");
        }
    }
    
    public Map<String, String> addLocation(AddLocationRequest request) {
        // navicode 중복 검사
        boolean isDuplicate = locations.stream()
            .anyMatch(loc -> loc.getNavicode().equals(request.getNavicode()));

        if (isDuplicate) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Location added fail");
            response.put("success", "false");
            return response;
        }

        Location newLocation = new Location(
            request.getName(),
            request.getNavicode(),
            request.getLatitude(),
            request.getLongitude(),
            request.getType()
        );
        
        locations.add(newLocation);
        saveLocationsToCSV();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "location added success");
        response.put("success", "true");
        return response;
    }
    
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return FastMath.sqrt(FastMath.pow(lat1 - lat2, 2) + FastMath.pow(lon1 - lon2, 2));
    }
}
