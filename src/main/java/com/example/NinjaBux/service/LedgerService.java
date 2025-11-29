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
import com.example.NinjaBux.util.AdminUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService extends NinjaServiceBase {

  @Autowired private LedgerTxnRepository ledgerTxnRepository;

  @Autowired private LegacyLedgerTxnRepository legacyLedgerTxnRepository;

  public int getBuxBalance(Long ninjaId) {
    Ninja ninja = findNinja(ninjaId);
    return ledgerTxnRepository.sumAmountByNinja(ninja);
  }

  public int getLegacyBalance(Long ninjaId) {
    Ninja ninja = findNinja(ninjaId);
    return legacyLedgerTxnRepository.sumAmountByNinja(ninja);
  }

  @Transactional
  public int recordLessonEarning(Long ninjaId, BeltType beltType, String note) {
    Ninja ninja = findNinja(ninjaId);
    int bux = beltType.perLessonBux();
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            bux,
            LedgerTxnType.EARN,
            LedgerSourceType.PROGRESS,
            null,
            note != null
                ? note
                : String.format("Lesson completion: %s Belt (+%d Bux)", beltType, bux));
    ledgerTxnRepository.save(txn);
    return bux;
  }

  @Transactional
  public int recordBeltUpReward(Long ninjaId, BeltType beltType, String note) {
    Ninja ninja = findNinja(ninjaId);
    int bux = beltType.beltUpBonus();
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            bux,
            LedgerTxnType.EARN,
            LedgerSourceType.BELT_UP,
            null,
            note != null ? note : String.format("Belt-up reward: %s (+%d Bux)", beltType, bux));
    ledgerTxnRepository.save(txn);
    return bux;
  }

  @Transactional
  public int recordLevelUpReward(Long ninjaId, BeltType beltType, int level, String note) {
    Ninja ninja = findNinja(ninjaId);
    int bux = beltType.levelUpBonus();
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            bux,
            LedgerTxnType.EARN,
            LedgerSourceType.PROGRESS,
            null,
            note != null
                ? note
                : String.format(
                    "Level-up reward: %s Belt Level %d (+%d Bux)", beltType, level, bux));
    ledgerTxnRepository.save(txn);
    return bux;
  }

  @Transactional
  public LedgerTxn recordPurchaseSpend(Purchase purchase) {
    Ninja ninja = purchase.getNinja();
    int price = purchase.getPricePaid();
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            -price,
            LedgerTxnType.SPEND,
            LedgerSourceType.PURCHASE,
            purchase.getId(),
            String.format("Purchase: %s (-%d Bux)", purchase.getShopItem().getName(), price));
    txn = ledgerTxnRepository.save(txn);
    purchase.setSpendTxn(txn);
    return txn;
  }

  @Transactional
  public LedgerTxn recordPurchaseRefund(Purchase purchase, String note) {
    Ninja ninja = purchase.getNinja();
    int refund = purchase.getPricePaid();
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            refund,
            LedgerTxnType.REFUND,
            LedgerSourceType.PURCHASE,
            purchase.getId(),
            note != null
                ? note
                : String.format("Refund: %s (+%d Bux)", purchase.getShopItem().getName(), refund));
    txn = ledgerTxnRepository.save(txn);
    purchase.setRefundTxn(txn);
    return txn;
  }

  @Transactional
  public LedgerTxn recordAchievementReward(
      Long ninjaId, Long achievementId, int buxAmount, String achievementName) {
    Ninja ninja = findNinja(ninjaId);
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            buxAmount,
            LedgerTxnType.EARN,
            LedgerSourceType.ACHIEVEMENT,
            achievementId,
            String.format("Achievement: %s (+%d Bux)", achievementName, buxAmount));
    return ledgerTxnRepository.save(txn);
  }

  @Transactional
  public LedgerTxn recordAdminAdjustment(
      Long ninjaId, int buxAmount, String note, String adminUsername) {
    Ninja ninja = findNinja(ninjaId);
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            buxAmount,
            LedgerTxnType.ADJUST,
            LedgerSourceType.ADMIN,
            null,
            note != null
                ? note
                : String.format(
                    "Admin adjustment by %s: %+d Bux",
                    AdminUtils.getAdminUsername(adminUsername), buxAmount));
    return ledgerTxnRepository.save(txn);
  }

  @Transactional
  public LegacyLedgerTxn grantLegacy(Long ninjaId, double legacyAmount, String note) {
    Ninja ninja = findNinja(ninjaId);
    int legacyUnits = LegacyLedgerTxn.legacyToInt(legacyAmount);
    LegacyLedgerTxn txn =
        new LegacyLedgerTxn(
            ninja,
            legacyUnits,
            LedgerTxnType.GRANT,
            LedgerSourceType.IMPORT,
            null,
            note != null ? note : String.format("Legacy grant: %d Legacy", legacyUnits));
    return legacyLedgerTxnRepository.save(txn);
  }

  @Transactional
  public int onboardNinjaWithLegacy(Long ninjaId, double computedOnboardingBux) {
    Ninja ninja = findNinja(ninjaId);
    int totalBuxGrant = (int) Math.round(computedOnboardingBux);
    LedgerTxn txn =
        new LedgerTxn(
            ninja,
            totalBuxGrant,
            LedgerTxnType.EARN,
            LedgerSourceType.IMPORT,
            null,
            String.format(
                "Import/onboarding: Bux grant (calculated from belt/level/lesson = %d Bux)",
                totalBuxGrant));
    ledgerTxnRepository.save(txn);
    return totalBuxGrant;
  }

  public List<LedgerTxn> getLedgerHistory(Long ninjaId) {
    Ninja ninja = findNinja(ninjaId);
    return ledgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
  }

  public List<LedgerTxn> getAllLedgerTransactions(int limit) {
    List<LedgerTxn> all = ledgerTxnRepository.findAllByOrderByCreatedAtDesc();
    return all.stream().limit(limit).collect(Collectors.toList());
  }

  public List<LegacyLedgerTxn> getLegacyLedgerHistory(Long ninjaId) {
    Ninja ninja = findNinja(ninjaId);
    return legacyLedgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
  }

  public int getTotalBuxEarned(Long ninjaId) {
    Ninja ninja = findNinja(ninjaId);
    return ledgerTxnRepository.sumEarnedAmountByNinja(ninja);
  }

  public int getTotalBuxSpent(Long ninjaId) {
    Ninja ninja = findNinja(ninjaId);
    int spent = ledgerTxnRepository.sumSpentAmountByNinja(ninja);
    return Math.abs(spent);
  }

  @Transactional
  public LegacyLedgerTxn grantLegacyAdjustment(
      Long ninjaId, int legacyAmount, String note, String adminUsername) {
    Ninja ninja = findNinja(ninjaId);
    LegacyLedgerTxn txn =
        new LegacyLedgerTxn(
            ninja,
            legacyAmount,
            LedgerTxnType.GRANT,
            LedgerSourceType.ADMIN,
            null,
            note != null
                ? note
                : String.format(
                    "Admin Legacy adjustment by %s: %+d Legacy", adminUsername, legacyAmount));
    return legacyLedgerTxnRepository.save(txn);
  }

  public Map<Long, Integer> getBuxBalances(List<Ninja> ninjas) {
    if (ninjas.isEmpty()) {
      return new HashMap<>();
    }
    List<Object[]> results = ledgerTxnRepository.sumAmountByNinjas(ninjas);
    Map<Long, Integer> balanceMap = new HashMap<>();
    for (Object[] result : results) {
      Long ninjaId = (Long) result[0];
      Number balance = (Number) result[1];
      balanceMap.put(ninjaId, balance.intValue());
    }
    for (Ninja ninja : ninjas) {
      balanceMap.putIfAbsent(ninja.getId(), 0);
    }
    return balanceMap;
  }

  public Map<Long, Integer> getTotalBuxEarnedBatch(List<Ninja> ninjas) {
    if (ninjas.isEmpty()) {
      return new HashMap<>();
    }
    List<Object[]> results = ledgerTxnRepository.sumEarnedAmountByNinjas(ninjas);
    Map<Long, Integer> earnedMap = new HashMap<>();
    for (Object[] result : results) {
      Long ninjaId = (Long) result[0];
      Number earned = (Number) result[1];
      earnedMap.put(ninjaId, earned.intValue());
    }
    for (Ninja ninja : ninjas) {
      earnedMap.putIfAbsent(ninja.getId(), 0);
    }
    return earnedMap;
  }

  public Map<Long, Integer> getTotalBuxSpentBatch(List<Ninja> ninjas) {
    if (ninjas.isEmpty()) {
      return new HashMap<>();
    }
    List<Object[]> results = ledgerTxnRepository.sumSpentAmountByNinjas(ninjas);
    Map<Long, Integer> spentMap = new HashMap<>();
    for (Object[] result : results) {
      Long ninjaId = (Long) result[0];
      Number spent = (Number) result[1];
      spentMap.put(ninjaId, Math.abs(spent.intValue()));
    }
    for (Ninja ninja : ninjas) {
      spentMap.putIfAbsent(ninja.getId(), 0);
    }
    return spentMap;
  }

  public int getTotalEarnedGlobal() {
    return ledgerTxnRepository.sumTotalEarned();
  }

  public int getTotalSpentGlobal() {
    return ledgerTxnRepository.sumTotalSpent();
  }
}
