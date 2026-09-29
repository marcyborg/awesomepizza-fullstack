package com.awesome.awesomepizza.repository;

import com.awesome.awesomepizza.domain.ChefStation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChefStationRepository extends JpaRepository<ChefStation, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select station from ChefStation station where station.id = :id")
    ChefStation lockStation(@Param("id") Long id);
}
