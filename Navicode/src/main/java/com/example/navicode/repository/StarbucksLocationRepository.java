package com.example.navicode.repository;

import com.example.navicode.model.StarbucksLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarbucksLocationRepository extends JpaRepository<StarbucksLocation, Long> {
    
    @Query("SELECT s FROM StarbucksLocation s WHERE s.navicode LIKE :navicode%")
    List<StarbucksLocation> findByNavicodeStartingWith(@Param("navicode") String navicode);
    
    StarbucksLocation findByNavicode(String navicode);
    
    @Query("SELECT s.type FROM StarbucksLocation s WHERE s.navicode LIKE :navicode%")
    List<Integer> findTypesByNavicodeStartingWith(@Param("navicode") String navicode);
} 