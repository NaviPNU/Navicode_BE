package com.example.navicode.service;

import com.example.navicode.model.StarbucksLocation;
import com.example.navicode.repository.StarbucksLocationRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
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
            // starbucks.csv 파일이 존재하는지 확인하고 로드
            CSVReader reader = new CSVReader(new FileReader("starbucks.csv"));
            List<String[]> rows = reader.readAll();
            reader.close();
            
            // 헤더를 제외하고 데이터 로드
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 5) {
                    StarbucksLocation location = new StarbucksLocation();
                    location.setName(row[0]);
                    location.setNavicode(row[1]);
                    location.setLatitude(Double.parseDouble(row[2]));
                    location.setLongitude(Double.parseDouble(row[3]));
                    location.setType(Integer.parseInt(row[4]));
                    
                    locationRepository.save(location);
                }
            }
            
            System.out.println("CSV 데이터가 성공적으로 로드되었습니다.");
            
        } catch (IOException | CsvException e) {
            System.out.println("CSV 파일을 찾을 수 없거나 읽을 수 없습니다: " + e.getMessage());
        }
    }
} 