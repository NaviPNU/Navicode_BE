package com.example.navicode.location.service;

import com.example.navicode.location.model.Location;
import com.example.navicode.location.model.CoordResponse;
import com.example.navicode.location.model.AddLocationRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.util.FastMath;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
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
                    String name = record.get(0);
                    String navicode = record.get(1);
                    double latitude = Double.parseDouble(record.get(2));
                    double longitude = Double.parseDouble(record.get(3));
                    int type = Integer.parseInt(record.get(4));
                    
                    // username과 expire 정보가 있는 경우 로드, 없으면 기본값 설정
                    String username = record.size() > 5 ? record.get(5) : "admin";
                    LocalDateTime expire;
                    
                    if (record.size() > 6 && !record.get(6).trim().isEmpty()) {
                        // 20260806 형식의 날짜를 LocalDateTime으로 변환
                        expire = parseExpireDate(record.get(6));
                    } else {
                        expire = calculateExpireDate(type, username);
                    }
                    
                    Location location = new Location(
                        name, navicode, latitude, longitude, type, username, expire
                    );
                    locations.add(location);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 20260806 형식의 날짜 문자열을 LocalDateTime으로 변환
     */
    private LocalDateTime parseExpireDate(String dateStr) {
        try {
            // 20260806 형식 처리
            if (dateStr.matches("\\d{8}")) {
                int year = Integer.parseInt(dateStr.substring(0, 4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int day = Integer.parseInt(dateStr.substring(6, 8));
                return LocalDateTime.of(year, month, day, 0, 0, 0);
            }
            // ISO 형식 처리 (2025-08-06T00:00:00)
            else if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr);
            }
            // 기타 형식은 현재 시간 + 1년으로 설정
            else {
                return LocalDateTime.now().plusYears(1);
            }
        } catch (Exception e) {
            // 파싱 실패 시 현재 시간 + 1년으로 설정
            return LocalDateTime.now().plusYears(1);
        }
    }
    
    private void saveLocationsToCSV() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(csvFilePath), Charset.forName("UTF-8")))) {

            // Write header
            writer.println("name,navicode,latitude,longitude,type,username,expire");
            
            // Write data
            for (Location location : locations) {
                String expireStr = formatExpireDate(location.getExpire());
                writer.printf("%s,%s,%.6f,%.6f,%d,%s,%s%n",
                    location.getName(),
                    location.getNavicode(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getType(),
                    location.getUsername(),
                    expireStr
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * LocalDateTime을 20260806 형식의 문자열로 변환
     */
    private String formatExpireDate(LocalDateTime expire) {
        if (expire == null) {
            return "";
        }
        return String.format("%04d%02d%02d", 
            expire.getYear(), 
            expire.getMonthValue(), 
            expire.getDayOfMonth());
    }
    
    private LocalDateTime calculateExpireDate(int type, String username) {
        LocalDateTime now = LocalDateTime.now();
        
        if (type == 1) { // 동적 코드
            return now.plusYears(1); // 1년 후 만료
        } else { // 정적 코드
            if ("admin".equals(username)) {
                return now.plusYears(1); // admin은 1년 후 만료
            } else {
                return now.plusDays(3); // 일반 사용자는 3일 후 만료
            }
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
        final String navicode;

        // navicode가 입력되지 않은 경우 랜덤 생성
        if (request.getNavicode() == null) {
            navicode = generateUniqueNavicode();
        } else {
            // navicode가 입력된 경우 중복 검사
            final String inputNavicode = request.getNavicode();
            boolean isDuplicate = locations.stream()
                .anyMatch(loc -> loc.getNavicode().equals(inputNavicode));

            if (isDuplicate) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Location added fail");
                response.put("success", "false");
                return response;
            }
            navicode = inputNavicode;
        }

        // username 설정 (동적 코드는 무조건 admin, 정적 코드는 사용자 이름)
        String username;
        if (request.getType() == 1) { // 동적 코드
            username = "admin";
        } else { // 정적 코드
            username = request.getUsername() != null ? request.getUsername() : "admin";
        }

        // 만료일 계산
        LocalDateTime expire = calculateExpireDate(request.getType(), username);

        Location newLocation = new Location(
            request.getName(),
            navicode,
            request.getLatitude(),
            request.getLongitude(),
            request.getType(),
            username,
            expire
        );
        
        locations.add(newLocation);
        saveLocationsToCSV();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "location added success");
        response.put("success", "true");
        response.put("navicode", navicode);
        return response;
    }
    
    private String generateUniqueNavicode() {
        Random random = new Random();
        String navicode;

        do {
            // 6자리 랜덤 숫자 생성 (100000 ~ 999999)
            navicode = String.valueOf(100000 + random.nextInt(900000));
        } while (isNavicodeExists(navicode));

        return navicode;
    }

    private boolean isNavicodeExists(String navicode) {
        return locations.stream()
            .anyMatch(loc -> loc.getNavicode().equals(navicode));
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return FastMath.sqrt(FastMath.pow(lat1 - lat2, 2) + FastMath.pow(lon1 - lon2, 2));
    }
    
    /**
     * 매일 자정에 만료된 코드를 자동으로 삭제
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void deleteExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        List<Location> expiredLocations = locations.stream()
            .filter(loc -> loc.getExpire() != null && loc.getExpire().isBefore(now))
            .collect(Collectors.toList());
        
        if (!expiredLocations.isEmpty()) {
            locations.removeAll(expiredLocations);
            saveLocationsToCSV();
            
            System.out.println("=== 만료된 코드 자동 삭제 ===");
            System.out.println("삭제된 코드 수: " + expiredLocations.size());
            for (Location location : expiredLocations) {
                System.out.println("삭제된 코드: " + location.getNavicode() + 
                    " (사용자: " + location.getUsername() + 
                    ", 만료일: " + location.getExpire() + ")");
            }
            System.out.println("==========================");
        }
    }
}
