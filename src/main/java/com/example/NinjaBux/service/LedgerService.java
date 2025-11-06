package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.LedgerTxn;
import com.example.NinjaBux.domain.LegacyLedgerTxn;
import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.Purchase;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.domain.enums.LedgerSourceType;
import com.example.NinjaBux.domain.enums.LedgerTxnType;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.repository.LedgerTxnRepository;
import com.example.NinjaBux.repository.LegacyLedgerTxnRepository;
import com.example.NinjaBux.repository.NinjaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    @Autowired
    private LedgerTxnRepository ledgerTxnRepository;

    @Autowired
    private LegacyLedgerTxnRepository legacyLedgerTxnRepository;

    @Autowired
    private NinjaRepository ninjaRepository;


    // Configuration constants (can be moved to a config table later)
    private static final int CONVERSION_LESSON_STEP = 10;
    private static final int CONVERSION_LEGACY_COST = 10; // 10 Legacy (whole units)
    private static final int CONVERSION_BUX_GRANT_QUARTERS = 8; // 2 Bux = 8 quarters
    private static final int BELT_UP_REWARD_QUARTERS = 20; // 5.0 Bux = 20 quarters
    private static final int INITIAL_BUX_GRANT_QUARTERS = 480; // 120 Bux = 480 quarters
    
    // Level rewards (in quarters)
    private static final int WHITE_LEVEL_REWARD_QUARTERS = 8; // 2 Bux = 8 quarters
    private static final int YELLOW_LEVEL_REWARD_QUARTERS = 12; // 3 Bux = 12 quarters
    private static final int ORANGE_LEVEL_REWARD_QUARTERS = 16; // 4 Bux = 16 quarters
    private static final int GREEN_LEVEL_REWARD_QUARTERS = 20; // 5 Bux = 20 quarters
    private static final int BLUE_LEVEL_REWARD_QUARTERS = 24; // 6 Bux = 24 quarters

    /**
     * Calculate Bux balance from ledger entries
     */
    public int getBuxBalanceQuarters(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        
        // Use cache if available and valid
        if (ninja.getCachedBuxBalanceQuarters() != null) {
            return ninja.getCachedBuxBalanceQuarters();
        }
        
        // Recalculate from ledger
        int balance = ledgerTxnRepository.sumAmountByNinja(ninja);
        
        // Update cache
        ninja.setCachedBuxBalanceQuarters(balance);
        ninjaRepository.save(ninja);
        
        return balance;
    }

    /**
     * Get Bux balance as double (for display)
     */
    public double getBuxBalance(Long ninjaId) {
        return LedgerTxn.quartersToBux(getBuxBalanceQuarters(ninjaId));
    }

    /**
     * Calculate Legacy balance from legacy ledger entries (returns whole integer Legacy units)
     */
    public int getLegacyBalance(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        
        // Use cache if available
        if (ninja.getCachedLegacyBalance() != null) {
            return ninja.getCachedLegacyBalance();
        }
        
        // Recalculate from ledger
        int balance = legacyLedgerTxnRepository.sumAmountByNinja(ninja);
        
        // Update cache
        ninja.setCachedLegacyBalance(balance);
        ninjaRepository.save(ninja);
        
        return balance;
    }

    /**
     * Record a lesson completion earning.
     * 
     * Always pays belt-based whole numbers with alternation for .5 belts.
     * Legacy decreases by 10 for every 10 lessons completed.
     */
    @Transactional
    public LedgerTxn recordLessonEarning(Long ninjaId, BeltType beltType, String note) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        // Increment counters
        ninja.setLessonsAllTime(ninja.getLessonsAllTime() + 1);
        ninja.setPostOnboardLessonCount(ninja.getPostOnboardLessonCount() + 1);
        
        // Always pay belt-based whole number earnings (bux increases for every lesson)
        int quartersEarned = calculateBeltBasedLessonEarning(beltType, ninja);
        
        // Increment conversion counter
        int lessonsSinceConversion = ninja.getLessonsSinceConversion() + 1;
        ninja.setLessonsSinceConversion(lessonsSinceConversion);
        
        ninjaRepository.save(ninja);

        // Create ledger entry for lesson earning
        LedgerTxn txn = new LedgerTxn(
            ninja,
            quartersEarned,
            LedgerTxnType.EARN,
            LedgerSourceType.PROGRESS,
            null,
            note != null ? note : String.format("Lesson completion: %s Belt (+%.2f Bux)", beltType, LedgerTxn.quartersToBux(quartersEarned))
        );
        txn = ledgerTxnRepository.save(txn);
        updateCachedBuxBalance(ninja);

        // Legacy points removed - no conversion needed

        return txn;
    }

    /**
     * Calculate belt-based per-lesson earning with whole numbers.
     * For .5 belts, alternates between low and high values.
     */
    private int calculateBeltBasedLessonEarning(BeltType beltType, Ninja ninja) {
        int quartersEarned;
        
        switch (beltType) {
            case WHITE:
                // 1.0 Bux = 4 quarters (integer)
                quartersEarned = 4;
                break;
            case YELLOW:
                // 1.5 Bux → alternate 1, 2 (4 quarters, 8 quarters)
                quartersEarned = ninja.isPostLegacyAlternator() ? 8 : 4; // 2 Bux or 1 Bux
                ninja.setPostLegacyAlternator(!ninja.isPostLegacyAlternator());
                break;
            case ORANGE:
                // 2.0 Bux = 8 quarters (integer)
                quartersEarned = 8;
                break;
            case GREEN:
                // 2.5 Bux → alternate 2, 3 (8 quarters, 12 quarters)
                quartersEarned = ninja.isPostLegacyAlternator() ? 12 : 8; // 3 Bux or 2 Bux
                ninja.setPostLegacyAlternator(!ninja.isPostLegacyAlternator());
                break;
            case BLUE:
                // 3.0 Bux = 12 quarters (integer)
                quartersEarned = 12;
                break;
            default:
                // Default to 4 quarters (1 Bux) for other belts
                quartersEarned = 4;
                break;
        }
        
        return quartersEarned;
    }

    /**
     * Record a belt-up reward (+5 Bux)
     */
    @Transactional
    public LedgerTxn recordBeltUpReward(Long ninjaId, String beltName, String note) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        LedgerTxn txn = new LedgerTxn(
            ninja,
            BELT_UP_REWARD_QUARTERS,
            LedgerTxnType.EARN,
            LedgerSourceType.BELT_UP,
            null,
            note != null ? note : String.format("Belt-up reward: %s (+%d quarters)", beltName, BELT_UP_REWARD_QUARTERS)
        );
        txn = ledgerTxnRepository.save(txn);

        updateCachedBuxBalance(ninja);
        return txn;
    }

    /**
     * Record a level-up reward (whole numbers: White 2, Yellow 3, Orange 4, Green 5, Blue 6)
     */
    @Transactional
    public LedgerTxn recordLevelUpReward(Long ninjaId, BeltType beltType, int level, String note) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        int quartersReward;
        switch (beltType) {
            case WHITE:
                quartersReward = WHITE_LEVEL_REWARD_QUARTERS; // 2 Bux
                break;
            case YELLOW:
                quartersReward = YELLOW_LEVEL_REWARD_QUARTERS; // 3 Bux
                break;
            case ORANGE:
                quartersReward = ORANGE_LEVEL_REWARD_QUARTERS; // 4 Bux
                break;
            case GREEN:
                quartersReward = GREEN_LEVEL_REWARD_QUARTERS; // 5 Bux
                break;
            case BLUE:
                quartersReward = BLUE_LEVEL_REWARD_QUARTERS; // 6 Bux
                break;
            default:
                quartersReward = WHITE_LEVEL_REWARD_QUARTERS; // Default to 2 Bux
                break;
        }

        LedgerTxn txn = new LedgerTxn(
            ninja,
            quartersReward,
            LedgerTxnType.EARN,
            LedgerSourceType.PROGRESS,
            null,
            note != null ? note : String.format("Level-up reward: %s Belt Level %d (+%d quarters)", beltType, level, quartersReward)
        );
        txn = ledgerTxnRepository.save(txn);

        updateCachedBuxBalance(ninja);
        return txn;
    }

    /**
     * Record a purchase spend
     */
    @Transactional
    public LedgerTxn recordPurchaseSpend(Purchase purchase) {
        Ninja ninja = purchase.getNinja();
        int priceQuarters = LedgerTxn.buxToQuarters(purchase.getPricePaid());

        LedgerTxn txn = new LedgerTxn(
            ninja,
            -priceQuarters, // Negative for spend
            LedgerTxnType.SPEND,
            LedgerSourceType.PURCHASE,
            purchase.getId(),
            String.format("Purchase: %s (-%d quarters)", purchase.getShopItem().getName(), priceQuarters)
        );
        txn = ledgerTxnRepository.save(txn);

        // Link purchase to transaction
        purchase.setSpendTxn(txn);

        updateCachedBuxBalance(ninja);
        return txn;
    }

    /**
     * Record a purchase refund
     */
    @Transactional
    public LedgerTxn recordPurchaseRefund(Purchase purchase, String note) {
        Ninja ninja = purchase.getNinja();
        int refundQuarters = LedgerTxn.buxToQuarters(purchase.getPricePaid());

        LedgerTxn txn = new LedgerTxn(
            ninja,
            refundQuarters, // Positive for refund
            LedgerTxnType.REFUND,
            LedgerSourceType.PURCHASE,
            purchase.getId(),
            note != null ? note : String.format("Refund: %s (+%d quarters)", purchase.getShopItem().getName(), refundQuarters)
        );
        txn = ledgerTxnRepository.save(txn);

        // Link purchase to refund transaction
        purchase.setRefundTxn(txn);

        updateCachedBuxBalance(ninja);
        return txn;
    }

    /**
     * Record an achievement reward
     */
    @Transactional
    public LedgerTxn recordAchievementReward(Long ninjaId, Long achievementId, double buxAmount, String achievementName) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        int quarters = LedgerTxn.buxToQuarters(buxAmount);

        LedgerTxn txn = new LedgerTxn(
            ninja,
            quarters,
            LedgerTxnType.EARN,
            LedgerSourceType.ACHIEVEMENT,
            achievementId,
            String.format("Achievement: %s (+%d quarters)", achievementName, quarters)
        );
        txn = ledgerTxnRepository.save(txn);

        updateCachedBuxBalance(ninja);
        return txn;
    }

    /**
     * Record a quiz reward
     */
    @Transactional
    public LedgerTxn recordQuizReward(Long ninjaId, double buxAmount, String note) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        int quarters = LedgerTxn.buxToQuarters(buxAmount);

        LedgerTxn txn = new LedgerTxn(
            ninja,
            quarters,
            LedgerTxnType.EARN,
            LedgerSourceType.QUIZ,
            null,
            note != null ? note : String.format("Quiz reward (+%d quarters)", quarters)
        );
        txn = ledgerTxnRepository.save(txn);

        updateCachedBuxBalance(ninja);
        return txn;
    }

    /**
     * Record an admin adjustment
     */
    @Transactional
    public LedgerTxn recordAdminAdjustment(Long ninjaId, double buxAmount, String note, String adminUsername) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        int quarters = LedgerTxn.buxToQuarters(buxAmount);

        LedgerTxn txn = new LedgerTxn(
            ninja,
            quarters,
            LedgerTxnType.ADJUST,
            LedgerSourceType.ADMIN,
            null,
            note != null ? note : String.format("Admin adjustment by %s: %s%+.2f Bux", adminUsername, quarters >= 0 ? "+" : "", LedgerTxn.quartersToBux(quarters))
        );
        txn = ledgerTxnRepository.save(txn);

        updateCachedBuxBalance(ninja);
        return txn;
    }

    /**
     * Grant Legacy currency (for imports/backpay)
     * @param legacyAmount Amount in Legacy units (will be rounded to nearest whole number)
     */
    @Transactional
    public LegacyLedgerTxn grantLegacy(Long ninjaId, double legacyAmount, String note) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        int legacyUnits = LegacyLedgerTxn.legacyToInt(legacyAmount);

        LegacyLedgerTxn txn = new LegacyLedgerTxn(
            ninja,
            legacyUnits,
            LedgerTxnType.GRANT,
            LedgerSourceType.IMPORT,
            null,
            note != null ? note : String.format("Legacy grant: %d Legacy", legacyUnits)
        );
        txn = legacyLedgerTxnRepository.save(txn);

        updateCachedLegacyBalance(ninja);
        return txn;
    }

    /**
     * Check and process Legacy conversion if conditions are met.
     * Only converts when Legacy >= 10 AND lessonsSinceConversion >= 10.
     */
    @Transactional
    public boolean checkAndProcessLegacyConversion(Ninja ninja) {
        int legacyBalance = getLegacyBalance(ninja.getId());
        
        if (legacyBalance >= CONVERSION_LEGACY_COST && 
            ninja.getLessonsSinceConversion() >= CONVERSION_LESSON_STEP) {
            
            // Debit Legacy (-10 Legacy)
            LegacyLedgerTxn legacyTxn = new LegacyLedgerTxn(
                ninja,
                -CONVERSION_LEGACY_COST,
                LedgerTxnType.CONVERT,
                LedgerSourceType.CONVERT,
                null,
                String.format("Legacy conversion: -%d Legacy", CONVERSION_LEGACY_COST)
            );
            legacyLedgerTxnRepository.save(legacyTxn);

            // Credit Bux (+2 Bux)
            LedgerTxn buxTxn = new LedgerTxn(
                ninja,
                CONVERSION_BUX_GRANT_QUARTERS,
                LedgerTxnType.EARN,
                LedgerSourceType.CONVERT,
                null,
                String.format("Legacy conversion: +%.2f Bux", LedgerTxn.quartersToBux(CONVERSION_BUX_GRANT_QUARTERS))
            );
            ledgerTxnRepository.save(buxTxn);

            // Reset conversion counter
            ninja.setLessonsSinceConversion(0);
            ninjaRepository.save(ninja);

            updateCachedBuxBalance(ninja);
            updateCachedLegacyBalance(ninja);

            return true;
        }
        
        return false;
    }

    /**
     * Onboard/import an existing ninja.
     * 
     * Calculates grant based on total lessons completed:
     *   - Lessons 0-50: 1 bux each = 50 bux max
     *   - Lessons 51-100: 0.5 bux each = 25 bux max (for lessons 51-100)
     *   - Lessons 101+: 0.25 bux each
     * 
     * Then adds extra bux: computedLegacyAmount / 10
     * 
     * Total grant = buxGrant (from lessons) + extra (from computedLegacyAmount / 10)
     * 
     * Examples:
     *   - 49 lessons = 49 bux + legacy
     *   - 72 lessons = 50 + (72-50)*0.5 = 50 + 11 = 61 bux + legacy
     *   - 300 lessons = 50 + 25 + (300-100)*0.25 = 50 + 25 + 50 = 125 bux + legacy
     * 
     * @param ninjaId The ninja to onboard
     * @param computedLegacyAmount The computed Legacy amount (what they'd earn under today's belt rules)
     * @return The total bux grant amount
     */
    @Transactional
    public int onboardNinjaWithLegacy(Long ninjaId, double computedLegacyAmount) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        // Calculate total lessons completed
        int totalLessons = BeltRewardCalculator.calculateTotalLessons(
            ninja.getCurrentBeltType(), 
            ninja.getCurrentLevel(), 
            ninja.getCurrentLesson()
        );
        
        // Calculate bux grant based on lessons completed (tiered system)
        int buxGrant = calculateBuxGrant(totalLessons);
        
        // Calculate extra bux from computed Legacy amount (divide by 10)
        int extraBux = (int) Math.round(computedLegacyAmount / 10.0);
        
        // Total grant = buxGrant (from lessons) + extra (from Legacy conversion)
        int totalBuxGrant = buxGrant + extraBux;
        
        // Grant total Bux (in quarters)
        int totalBuxGrantQuarters = LedgerTxn.buxToQuarters(totalBuxGrant);
        LedgerTxn buxGrantTxn = new LedgerTxn(
            ninja,
            totalBuxGrantQuarters,
            LedgerTxnType.EARN,
            LedgerSourceType.IMPORT,
            null,
            String.format("Import/onboarding: Bux grant (%d from lessons + %d from legacy = %d total Bux)", 
                buxGrant, extraBux, totalBuxGrant)
        );
        ledgerTxnRepository.save(buxGrantTxn);

        // Initialize control counters
        ninja.setLessonsSinceConversion(0);
        ninja.setPostLegacyAlternator(false); // Start with "low first" for .5 belts
        ninjaRepository.save(ninja);

        updateCachedBuxBalance(ninja);

        return totalBuxGrant;
    }

    /**
     * Calculate bux grant based on total lessons completed using tiered system:
     * - Lessons 0-50: 1 bux each = 50 bux max
     * - Lessons 51-100: 0.5 bux each = 25 bux max (for lessons 51-100)
     * - Lessons 101+: 0.25 bux each
     * 
     * Examples:
     *   - 49 lessons = 49 bux
     *   - 72 lessons = 50 + (72-50)*0.5 = 50 + 11 = 61 bux
     *   - 300 lessons = 50 + 25 + (300-100)*0.25 = 50 + 25 + 50 = 125 bux
     */
    private int calculateBuxGrant(int totalLessons) {
        int grant = 0;
        
        if (totalLessons <= 50) {
            // First 50 lessons: 1 bux each
            grant = totalLessons;
        } else if (totalLessons <= 100) {
            // First 50: 50 bux, lessons 51-100: 0.5 bux each
            grant = 50 + (int) Math.round((totalLessons - 50) * 0.5);
        } else {
            // First 50: 50 bux, lessons 51-100: 25 bux, lessons 101+: 0.25 bux each
            grant = 50 + 25 + (int) Math.round((totalLessons - 100) * 0.25);
        }
        
        return grant;
    }

    /**
     * Get all ledger transactions for a ninja
     */
    public List<LedgerTxn> getLedgerHistory(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        return ledgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
    }

    /**
     * Get all ledger transactions (admin only), limited to most recent
     */
    public List<LedgerTxn> getAllLedgerTransactions(int limit) {
        List<LedgerTxn> all = ledgerTxnRepository.findAllByOrderByCreatedAtDesc();
        return all.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get all legacy ledger transactions for a ninja
     */
    public List<LegacyLedgerTxn> getLegacyLedgerHistory(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        return legacyLedgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
    }

    /**
     * Update cached Bux balance
     */
    private void updateCachedBuxBalance(Ninja ninja) {
        int balance = ledgerTxnRepository.sumAmountByNinja(ninja);
        ninja.setCachedBuxBalanceQuarters(balance);
        ninjaRepository.save(ninja);
    }

    /**
     * Update cached Legacy balance
     */
    private void updateCachedLegacyBalance(Ninja ninja) {
        int balance = legacyLedgerTxnRepository.sumAmountByNinja(ninja);
        ninja.setCachedLegacyBalance(balance);
        ninjaRepository.save(ninja);
    }

    /**
     * Recalculate and update all cached balances (useful for data migration)
     */
    @Transactional
    public void recalculateAllBalances(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        
        updateCachedBuxBalance(ninja);
        updateCachedLegacyBalance(ninja);
    }

    /**
     * Grant Legacy currency via admin adjustment (for corrections/imports)
     */
    @Transactional
    public LegacyLedgerTxn grantLegacyAdjustment(Long ninjaId, int legacyAmount, String note, String adminUsername) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        LegacyLedgerTxn txn = new LegacyLedgerTxn(
            ninja,
            legacyAmount,
            LedgerTxnType.GRANT,
            LedgerSourceType.ADMIN,
            null,
            note != null ? note : String.format("Admin Legacy adjustment by %s: %+d Legacy", adminUsername, legacyAmount)
        );
        txn = legacyLedgerTxnRepository.save(txn);

        updateCachedLegacyBalance(ninja);
        return txn;
    }
}

