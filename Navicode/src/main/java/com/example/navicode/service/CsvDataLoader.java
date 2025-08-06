package com.example.navicode.service;

import com.example.navicode.model.StarbucksLocation;
import com.example.navicode.repository.StarbucksLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class CsvDataLoader implements CommandLineRunner {
    
    @Autowired
    private StarbucksLocationRepository locationRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 애플리케이션 시작 시 CSV 파일이 존재하면 데이터를 로드
        loadCsvDataIfExists();
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
                        StarbucksLocation location = new StarbucksLocation();
                        location.setName(row[0].trim());
                        location.setNavicode(row[1].trim());
                        location.setLatitude(Double.parseDouble(row[2].trim()));
                        location.setLongitude(Double.parseDouble(row[3].trim()));
                        location.setType(Integer.parseInt(row[4].trim()));
                        
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
} 