package com.example.navicode.location.controller;

import com.example.navicode.location.model.AddLocationRequest;
import com.example.navicode.location.model.CoordResponse;
import com.example.navicode.location.service.LocationService;
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
    
    /**
     * navicode의 타입을 반환하는 API
     * @param navicode : 검색할 navicode
     * return type: 1(dynamic), 2(static)
     */
    @GetMapping("/coord_type")
    public ResponseEntity<Map<String, Object>> getCoordType(@RequestParam String navicode) {
        try {
            Map<String, Object> result = locationService.getCoordType(navicode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 현재 위치 기반으로 가장 가까운 5개 위치를 반환하는 API
     * @param navicode : 검색할 navicode
     * @param latitude : 현재 위도
     * @param longitude : 현재 경도
     * return 가장 가까운 5개 위치 목록
     */
    @GetMapping("/get_coord_dynamic")
    public ResponseEntity<?> getCoordDynamic(
            @RequestParam String navicode,
            @RequestParam String latitude,
            @RequestParam String longitude) {
        
        try {
            if (navicode == null || latitude == null || longitude == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing parameters"));
            }
            
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            
            List<CoordResponse> result = locationService.getCoordDynamic(navicode, lat, lon);
            return ResponseEntity.ok(result);
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid latitude or longitude"));
        } catch (RuntimeException e) {
            if ("No matching navicode".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No matching navicode"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 정확한 navicode에 해당하는 위치를 반환하는 API
     * @param navicode : 검색할 navicode
     * return 해당 위치 정보
     */
    @GetMapping("/get_coord_static")
    public ResponseEntity<?> getCoordStatic(@RequestParam String navicode) {
        try {
            CoordResponse result = locationService.getCoordStatic(navicode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if ("404".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "404"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 새로운 위치를 추가하는 API
     * @param request : 추가할 위치 정보 (name, navicode, latitude, longitude, type, username)
     * return 추가 성공 메시지
     */
    @PostMapping("/add_coord_location")
    public ResponseEntity<?> addCoordLocation(@RequestBody AddLocationRequest request) {
        try {
            // Validate required fields (navicode는 자동 생성되므로 검증에서 제외)
            if (request.getName() == null || request.getName().trim().isEmpty() ||
                request.getLatitude() == 0 || request.getLongitude() == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Required fields are missing"));
            }
            
            // 정적 코드(type=2)인 경우 username 필수
            if (request.getType() == 2 && (request.getUsername() == null || request.getUsername().trim().isEmpty())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required for static codes"));
            }
            
            Map<String, String> result = locationService.addLocation(request);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }


}
