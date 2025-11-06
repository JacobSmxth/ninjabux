package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.Achievement;
import com.example.NinjaBux.domain.enums.AchievementCategory;
import com.example.NinjaBux.domain.enums.BadgeRarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByActiveTrue();
    List<Achievement> findByCategory(AchievementCategory category);
    List<Achievement> findByRarity(BadgeRarity rarity);
    List<Achievement> findByActiveTrueAndManualOnlyFalse();
    List<Achievement> findByCategoryAndActiveTrue(AchievementCategory category);
    List<Achievement> findByHiddenTrue();
    List<Achievement> findByHiddenFalseAndActiveTrue();

    // custom
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.active = true")
    long countActive();

}
