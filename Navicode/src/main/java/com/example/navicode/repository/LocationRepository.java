package com.example.navicode.repository;

import com.example.navicode.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    @Query("SELECT s FROM Location s WHERE s.navicode LIKE :navicode%")
    List<Location> findByNavicodeStartingWith(@Param("navicode") String navicode);
    
    Location findByNavicode(String navicode);
    
    @Query("SELECT s.type FROM Location s WHERE s.navicode LIKE :navicode%")
    List<Integer> findTypesByNavicodeStartingWith(@Param("navicode") String navicode);
    
    // 만료된 코드 조회
    List<Location> findByExpireBefore(LocalDate date);
} 