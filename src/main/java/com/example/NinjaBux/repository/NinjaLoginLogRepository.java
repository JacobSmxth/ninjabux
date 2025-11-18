package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.NinjaLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NinjaLoginLogRepository extends JpaRepository<NinjaLoginLog, Long> {

    List<NinjaLoginLog> findTop100ByOrderByLoginTimeDesc();

    List<NinjaLoginLog> findByNinjaOrderByLoginTimeDesc(Ninja ninja);

    List<NinjaLoginLog> findByLoginTimeBetweenOrderByLoginTimeDesc(LocalDateTime start, LocalDateTime end);

    List<NinjaLoginLog> findByNinjaAndLoginTimeBetween(Ninja ninja, LocalDateTime start, LocalDateTime end);
}
