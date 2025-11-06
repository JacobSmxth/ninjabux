package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.LegacyLedgerTxn;
import com.example.NinjaBux.domain.Ninja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegacyLedgerTxnRepository extends JpaRepository<LegacyLedgerTxn, Long> {
    List<LegacyLedgerTxn> findByNinjaOrderByCreatedAtDesc(Ninja ninja);
    
    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LegacyLedgerTxn lt WHERE lt.ninja = :ninja")
    int sumAmountByNinja(@Param("ninja") Ninja ninja);
}

