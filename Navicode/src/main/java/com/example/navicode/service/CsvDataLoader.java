package com.example.navicode.service;

import com.example.navicode.model.Location;
import com.example.navicode.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvDataLoader implements CommandLineRunner {
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 애플리케이션 시작 시 CSV 파일이 존재하면 데이터를 로드
        loadCsvDataIfExists();
        // 만료된 코드 삭제
        deleteExpiredCodes();
    }
    
    private void loadCsvDataIfExists() {
        try {
            // starbucks.csv 파일이 존재하는지 확인
            if (!Files.exists(Paths.get("starbucks.csv"))) {
                System.out.println("starbucks.csv 파일이 존재하지 않습니다.");
                return;
            }
            
            // CSV 파일을 읽어서 데이터 로드
            List<String> lines = Files.readAllLines(Paths.get("starbucks.csv"));
            
            // 헤더를 제외하고 데이터 로드
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] row = line.split(",");
                
                if (row.length >= 5) {
                    try {
                        Location location = new Location();
                        location.setName(row[0].trim());
                        location.setNavicode(row[1].trim());
                        location.setLatitude(Double.parseDouble(row[2].trim()));
                        location.setLongitude(Double.parseDouble(row[3].trim()));
                        location.setType(Integer.parseInt(row[4].trim()));
                        
                        // username과 expire 처리
                        if (row.length >= 7) {
                            location.setUsername(row[5].trim());
                            try {
                                location.setExpire(LocalDate.parse(row[6].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                            } catch (Exception e) {
                                // expire 파싱 실패 시 기본값 설정
                                if (location.getType() == 1) {
                                    // type 1 (동적)은 admin으로 1년 후 만료
                                    location.setUsername("admin");
                                    location.setExpire(LocalDate.now().plusYears(1));
                                } else {
                                    // type 2 (정적)는 사용자 이름으로 3일 후 만료
                                    location.setUsername(row[5].trim());
                                    location.setExpire(LocalDate.now().plusDays(3));
                                }
                            }
                        } else {
                            // username과 expire가 없는 경우 기본값 설정
                            if (location.getType() == 1) {
                                location.setUsername("admin");
                                location.setExpire(LocalDate.now().plusYears(1));
                            } else {
                                location.setUsername("unknown");
                                location.setExpire(LocalDate.now().plusDays(3));
                            }
                        }
                        
                        locationRepository.save(location);
                    } catch (NumberFormatException e) {
                        System.out.println("잘못된 데이터 형식: " + line);
                    }
                }
            }
            
            System.out.println("CSV 데이터가 성공적으로 로드되었습니다.");
            
        } catch (IOException e) {
            System.out.println("CSV 파일을 읽을 수 없습니다: " + e.getMessage());
        }
    }
    
    // 매일 자정에 실행되는 스케줄러
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredCodes() {
        LocalDate today = LocalDate.now();
        List<Location> expiredCodes = locationRepository.findByExpireBefore(today);
        
        if (!expiredCodes.isEmpty()) {
            locationRepository.deleteAll(expiredCodes);
            System.out.println("만료된 코드 " + expiredCodes.size() + "개가 삭제되었습니다.");
        }
    }
} 