package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.Purchase;
import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ShopItem;
import com.example.NinjaBux.domain.enums.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByNinja(Ninja ninja);
    List<Purchase> findByNinjaAndStatus(Ninja ninja, PurchaseStatus status);
    List<Purchase> findByNinjaOrderByPurchaseDateDesc(Ninja ninja);
    
    long countByNinjaAndShopItem(Ninja ninja, ShopItem shopItem);
    long countByNinjaAndShopItemAndPurchaseDateAfter(Ninja ninja, ShopItem shopItem, LocalDateTime date);
    long countByNinjaAndShopItemAndStatus(Ninja ninja, ShopItem shopItem, PurchaseStatus status);
}
