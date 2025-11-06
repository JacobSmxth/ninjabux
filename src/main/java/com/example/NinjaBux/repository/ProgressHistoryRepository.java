package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ProgressHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProgressHistoryRepository extends JpaRepository<ProgressHistory, Long> {
    List<ProgressHistory> findByNinjaOrderByTimestampDesc(Ninja ninja);

    // custom
    @Query("SELECT ph.ninja.id as ninjaId, SUM(ph.buxEarned) as totalEarned " +
           "FROM ProgressHistory ph " +
           "WHERE ph.timestamp >= :startDate " +
           "GROUP BY ph.ninja.id " +
           "ORDER BY totalEarned DESC")
    List<Object[]> findTopEarnersSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT ph.ninja.id as ninjaId, COUNT(ph.id) as lessonCount " +
           "FROM ProgressHistory ph " +
           "WHERE ph.timestamp >= :startDate AND ph.earningType = 'LEVEL_UP' " +
           "GROUP BY ph.ninja.id " +
           "ORDER BY lessonCount DESC")
    List<Object[]> findMostImprovedSince(@Param("startDate") LocalDateTime startDate);

    List<ProgressHistory> findByCorrectionToId(Long correctionToId);
}
