package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.Purchase;
import com.example.NinjaBux.domain.ShopItem;
import com.example.NinjaBux.domain.enums.PurchaseStatus;
import com.example.NinjaBux.exception.AccountLockedException;
import com.example.NinjaBux.exception.CategoryRestrictedException;
import com.example.NinjaBux.exception.InsufficientFundsException;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.exception.PurchaseLimitExceededException;
import com.example.NinjaBux.exception.ShopItemNotFoundException;
import com.example.NinjaBux.exception.ShopItemUnavailableException;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.repository.PurchaseRepository;
import com.example.NinjaBux.repository.ShopItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {

    @Autowired
    private ShopItemRepository shopItemRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private LedgerService ledgerService;

    public List<ShopItem> getAvailableItems() {
        return shopItemRepository.findByAvailableTrue();
    }

    public List<ShopItem> getAllItems() {
        return shopItemRepository.findAll();
    }

    public List<ShopItem> getItemsByCategory(String category) {
        return shopItemRepository.findByCategory(category);
    }

    public ShopItem getItem(Long itemId) {
        return shopItemRepository.findById(itemId)
            .orElseThrow(() -> new ShopItemNotFoundException(itemId));
    }

    @Transactional
    public Purchase purchaseItem(Long ninjaId, Long itemId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        // check lock before letting them buy anything
        if (ninja.isLocked()) {
            String reason = ninja.getLockReason() != null ? ninja.getLockReason() : "Account is locked";
            throw new AccountLockedException(reason);
        }

        ShopItem item = shopItemRepository.findById(itemId)
            .orElseThrow(() -> new ShopItemNotFoundException(itemId));

        if (!item.isAvailable()) {
            throw new ShopItemUnavailableException(item.getName());
        }

        checkCategoryRestrictions(item, ninja);

        checkPurchaseLimits(ninja, item);

        // check balance from ledger because ninja object balance is unreliable
        double currentBalance = ledgerService.getBuxBalance(ninjaId);
        if (currentBalance < item.getPrice()) {
            throw new InsufficientFundsException((int) currentBalance, item.getPrice());
        }

        // create purchase record first
        Purchase purchase = new Purchase(ninja, item, item.getPrice());
        purchase = purchaseRepository.save(purchase);

        // then record the spend in ledger
        ledgerService.recordPurchaseSpend(purchase);

        return purchase;
    }

    // category restrictions exist but nobody uses them yet
    private void checkCategoryRestrictions(ShopItem item, Ninja ninja) {
        if (item.getRestrictedCategories() == null || item.getRestrictedCategories().trim().isEmpty()) {
            return;
        }
    }

    private void checkPurchaseLimits(Ninja ninja, ShopItem item) {
        if (item.getMaxPerStudent() != null) {
            long currentCount = purchaseRepository.countByNinjaAndShopItem(ninja, item);
            if (currentCount >= item.getMaxPerStudent()) {
                throw new PurchaseLimitExceededException("per student", (int) currentCount, item.getMaxPerStudent());
            }
        }

        if (item.getMaxPerDay() != null) {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            long todayCount = purchaseRepository.countByNinjaAndShopItemAndPurchaseDateAfter(ninja, item, startOfDay);
            if (todayCount >= item.getMaxPerDay()) {
                throw new PurchaseLimitExceededException("per day", (int) todayCount, item.getMaxPerDay());
            }
        }

        if (item.getMaxLifetime() != null) {
            long lifetimeCount = purchaseRepository.countByNinjaAndShopItem(ninja, item);
            if (lifetimeCount >= item.getMaxLifetime()) {
                throw new PurchaseLimitExceededException("lifetime", (int) lifetimeCount, item.getMaxLifetime());
            }
        }

        if (item.getMaxActiveAtOnce() != null) {
            long activeCount = purchaseRepository.countByNinjaAndShopItemAndRedeemedFalse(ninja, item);
            if (activeCount >= item.getMaxActiveAtOnce()) {
                throw new PurchaseLimitExceededException("active at once", (int) activeCount, item.getMaxActiveAtOnce());
            }
        }
    }

    public List<Purchase> getNinjaPurchases(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        return purchaseRepository.findByNinjaOrderByPurchaseDateDesc(ninja);
    }

    public List<Purchase> getUnredeemedPurchases(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        // use status field instead of deprecated redeemed boolean
        return purchaseRepository.findByNinjaOrderByPurchaseDateDesc(ninja).stream()
            .filter(p -> p.getStatus() == PurchaseStatus.PURCHASED)
            .collect(Collectors.toList());
    }

    @Transactional
    public Purchase redeemPurchase(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
            .orElseThrow(() -> new RuntimeException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() != PurchaseStatus.PURCHASED) {
            throw new IllegalStateException("Purchase is not in PURCHASED status");
        }

        purchase.setStatus(PurchaseStatus.REDEEMED);
        purchase.setRedeemedDate(LocalDateTime.now());
        return purchaseRepository.save(purchase);
    }

    @Transactional
    public Purchase refundPurchase(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
            .orElseThrow(() -> new RuntimeException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() == PurchaseStatus.REFUNDED) {
            throw new IllegalStateException("Purchase is already refunded");
        }

        if (purchase.getStatus() == PurchaseStatus.CANCELED) {
            throw new IllegalStateException("Purchase is canceled and cannot be refunded");
        }

        // refund goes through ledger
        ledgerService.recordPurchaseRefund(purchase, 
            String.format("Refund for purchase: %s", purchase.getShopItem().getName()));

        // mark as refunded but never delete for audit trail
        purchase.setStatus(PurchaseStatus.REFUNDED);
        return purchaseRepository.save(purchase);
    }

    public ShopItem createItem(String name, String description, int price, String category,
                               Integer maxPerStudent, Integer maxPerDay, Integer maxLifetime,
                               Integer maxActiveAtOnce, String restrictedCategories) {
        ShopItem item = new ShopItem(name, description, price, category);
        item.setMaxPerStudent(maxPerStudent);
        item.setMaxPerDay(maxPerDay);
        item.setMaxLifetime(maxLifetime);
        item.setMaxActiveAtOnce(maxActiveAtOnce);
        item.setRestrictedCategories(restrictedCategories);
        return shopItemRepository.save(item);
    }

    @Transactional
    public ShopItem updateItemAvailability(Long itemId, boolean available) {
        ShopItem item = shopItemRepository.findById(itemId)
            .orElseThrow(() -> new ShopItemNotFoundException(itemId));
        item.setAvailable(available);
        return shopItemRepository.save(item);
    }

    @Transactional
    public ShopItem updateItem(Long itemId, String name, String description, int price, String category,
                               Integer maxPerStudent, Integer maxPerDay, Integer maxLifetime,
                               Integer maxActiveAtOnce, String restrictedCategories) {
        ShopItem item = shopItemRepository.findById(itemId)
            .orElseThrow(() -> new ShopItemNotFoundException(itemId));
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setMaxPerStudent(maxPerStudent);
        item.setMaxPerDay(maxPerDay);
        item.setMaxLifetime(maxLifetime);
        item.setMaxActiveAtOnce(maxActiveAtOnce);
        item.setRestrictedCategories(restrictedCategories);
        return shopItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        ShopItem item = shopItemRepository.findById(itemId)
            .orElseThrow(() -> new ShopItemNotFoundException(itemId));
        shopItemRepository.delete(item);
    }
}
