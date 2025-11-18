package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ProgressHistory;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.repository.AchievementProgressRepository;
import com.example.NinjaBux.repository.LedgerTxnRepository;
import com.example.NinjaBux.repository.LegacyLedgerTxnRepository;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.repository.ProgressHistoryRepository;
import com.example.NinjaBux.repository.PurchaseRepository;
import com.example.NinjaBux.repository.QuestionAnswerRepository;
import com.example.NinjaBux.util.AdminUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NinjaAdminService extends NinjaServiceBase {

  @Autowired private QuestionAnswerRepository questionAnswerRepository;

  @Autowired private PurchaseRepository purchaseRepository;

  @Autowired private ProgressHistoryRepository progressHistoryRepository;

  @Autowired private LedgerTxnRepository ledgerTxnRepository;

  @Autowired private LegacyLedgerTxnRepository legacyLedgerTxnRepository;

  @Autowired private LedgerService ledgerService;

  @Autowired(required = false)
  private AchievementProgressRepository achievementProgressRepository;

  @Autowired(required = false)
  private NotificationService notificationService;

  @Transactional
  public void deleteNinja(Long ninjaId) {
    if (!ninjaRepository.existsById(ninjaId)) {
      throw new NinjaNotFoundException(ninjaId);
    }

    Ninja ninja = findNinja(ninjaId);

    if (achievementProgressRepository != null) {
      achievementProgressRepository.deleteByNinja(ninja);
    }

    List<com.example.NinjaBux.domain.QuestionAnswer> answers =
        questionAnswerRepository.findAll().stream()
            .filter(qa -> qa.getNinja().getId().equals(ninjaId))
            .collect(Collectors.toList());
    if (!answers.isEmpty()) {
      questionAnswerRepository.deleteAll(answers);
    }

    List<com.example.NinjaBux.domain.Purchase> purchases =
        purchaseRepository.findByNinjaOrderByPurchaseDateDesc(ninja);
    if (!purchases.isEmpty()) {
      purchaseRepository.deleteAll(purchases);
    }

    List<ProgressHistory> progressHistory =
        progressHistoryRepository.findAll().stream()
            .filter(ph -> ph.getNinja().getId().equals(ninjaId))
            .collect(Collectors.toList());
    if (!progressHistory.isEmpty()) {
      progressHistoryRepository.deleteAll(progressHistory);
    }

    List<com.example.NinjaBux.domain.LedgerTxn> ledgerTxns =
        ledgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
    if (!ledgerTxns.isEmpty()) {
      ledgerTxnRepository.deleteAll(ledgerTxns);
    }

    List<com.example.NinjaBux.domain.LegacyLedgerTxn> legacyTxns =
        legacyLedgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
    if (!legacyTxns.isEmpty()) {
      legacyLedgerTxnRepository.deleteAll(legacyTxns);
    }

    ninjaRepository.deleteById(ninjaId);
  }

  @Transactional
  public Ninja awardBux(Long ninjaId, int amount, String adminUsername, String notes) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Award amount must be positive");
    }

    Ninja ninja = findNinja(ninjaId);

    String noteText =
        notes != null && !notes.trim().isEmpty()
            ? notes
            : String.format("Admin award: %d Bux", amount);

    ledgerService.recordAdminAdjustment(
        ninjaId, amount, noteText, AdminUtils.getAdminUsername(adminUsername));

    ProgressHistory history =
        new ProgressHistory(
            ninja,
            ninja.getCurrentBeltType(),
            ninja.getCurrentLevel(),
            ninja.getCurrentLesson(),
            amount,
            ProgressHistory.EarningType.ADMIN_AWARD);
    history.setNotes(noteText);
    history.setAdminUsername(AdminUtils.getAdminUsername(adminUsername));
    progressHistoryRepository.save(history);

    return ninjaRepository.findById(ninjaId).orElse(ninja);
  }

  @Transactional
  public Ninja deductBux(Long ninjaId, int amount, String adminUsername, String notes) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Deduct amount must be positive");
    }

    Ninja ninja = findNinja(ninjaId);

    int currentBalance = ledgerService.getBuxBalance(ninjaId);
    if (currentBalance < amount) {
      throw new IllegalArgumentException("Cannot deduct more Bux than ninja has");
    }

    String noteText =
        notes != null && !notes.trim().isEmpty()
            ? notes
            : String.format("Admin deduction: %d Bux", amount);

    ledgerService.recordAdminAdjustment(
        ninjaId, -amount, noteText, AdminUtils.getAdminUsername(adminUsername));

    return ninjaRepository.findById(ninjaId).orElse(ninja);
  }

  @Transactional
  public Ninja banSuggestions(Long ninjaId, boolean banned) {
    Ninja ninja = findNinja(ninjaId);
    ninja.setSuggestionsBanned(banned);
    return ninjaRepository.save(ninja);
  }

  @Transactional
  public Ninja lockAccount(Long ninjaId, String reason, String adminUsername) {
    Ninja ninja = findNinja(ninjaId);
    ninja.setLocked(true);
    ninja = ninjaRepository.save(ninja);

    if (notificationService != null) {
      try {
        String lockMessage =
            reason != null && !reason.trim().isEmpty()
                ? reason
                : "Your account has been locked. Please get back to work!";
        notificationService.sendLockNotification(ninjaId, lockMessage);
      } catch (Exception ignored) {
      }
    }

    return ninja;
  }

  @Transactional
  public Ninja unlockAccount(Long ninjaId, String adminUsername) {
    Ninja ninja = findNinja(ninjaId);
    ninja.setLocked(false);
    ninja = ninjaRepository.save(ninja);

    if (notificationService != null) {
      try {
        notificationService.sendUnlockNotification(ninjaId);
      } catch (Exception ignored) {
      }
    }

    return ninja;
  }
}
