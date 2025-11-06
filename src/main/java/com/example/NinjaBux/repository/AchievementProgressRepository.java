package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.Achievement;
import com.example.NinjaBux.domain.AchievementProgress;
import com.example.NinjaBux.domain.Ninja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementProgressRepository extends JpaRepository<AchievementProgress, Long> {
    Optional<AchievementProgress> findByNinjaAndAchievement(Ninja ninja, Achievement achievement);
    List<AchievementProgress> findByNinja(Ninja ninja);
    List<AchievementProgress> findByNinjaAndUnlockedTrue(Ninja ninja);
    List<AchievementProgress> findByNinjaAndUnlockedTrueAndSeenFalse(Ninja ninja);
    List<AchievementProgress> findByAchievementAndUnlockedTrue(Achievement achievement);
    void deleteByNinja(Ninja ninja);

    // custom queries
    @Query("SELECT COUNT(ap) FROM AchievementProgress ap WHERE ap.ninja = :ninja AND ap.unlocked = true")
    long countUnlockedByNinja(@Param("ninja") Ninja ninja);

    @Query("SELECT COUNT(ap) FROM AchievementProgress ap " +
           "WHERE ap.ninja = :ninja AND ap.unlocked = true AND ap.achievement.rarity = :rarity")
    long countUnlockedByNinjaAndRarity(@Param("ninja") Ninja ninja, @Param("rarity") String rarity);

    @Query("SELECT ap FROM AchievementProgress ap " +
           "WHERE ap.ninja = :ninja AND ap.unlocked = true " +
           "ORDER BY ap.achievement.rarity DESC, ap.unlockedAt DESC")
    List<AchievementProgress> findTopAchievementsByNinja(@Param("ninja") Ninja ninja);

    @Query("SELECT CASE WHEN COUNT(ap) > 0 THEN true ELSE false END " +
           "FROM AchievementProgress ap " +
           "WHERE ap.ninja = :ninja AND ap.achievement = :achievement AND ap.unlocked = true")
    boolean hasNinjaUnlockedAchievement(@Param("ninja") Ninja ninja, @Param("achievement") Achievement achievement);
}
