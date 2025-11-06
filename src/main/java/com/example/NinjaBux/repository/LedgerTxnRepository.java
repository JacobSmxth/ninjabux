package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.LedgerTxn;
import com.example.NinjaBux.domain.Ninja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerTxnRepository extends JpaRepository<LedgerTxn, Long> {
    List<LedgerTxn> findByNinjaOrderByCreatedAtDesc(Ninja ninja);
    
    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LedgerTxn lt WHERE lt.ninja = :ninja")
    int sumAmountByNinja(@Param("ninja") Ninja ninja);
    
    List<LedgerTxn> findByNinjaAndSourceTypeOrderByCreatedAtDesc(Ninja ninja, com.example.NinjaBux.domain.enums.LedgerSourceType sourceType);
    
    List<LedgerTxn> findBySourceId(Long sourceId);
    
    @Query("SELECT lt FROM LedgerTxn lt JOIN FETCH lt.ninja ORDER BY lt.createdAt DESC")
    List<LedgerTxn> findAllByOrderByCreatedAtDesc();
}

