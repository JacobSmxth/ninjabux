package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    List<ShopItem> findByAvailableTrue();
    List<ShopItem> findByCategory(String category);
}
