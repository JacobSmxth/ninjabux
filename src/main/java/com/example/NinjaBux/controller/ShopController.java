package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.Purchase;
import com.example.NinjaBux.domain.ShopItem;
import com.example.NinjaBux.dto.CreateShopItemRequest;
import com.example.NinjaBux.dto.PurchaseRequest;
import com.example.NinjaBux.dto.PurchaseResponse;
import com.example.NinjaBux.dto.ShopItemResponse;
import com.example.NinjaBux.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("/items")
    public ResponseEntity<List<ShopItemResponse>> getAvailableItems() {
        List<ShopItem> items = shopService.getAvailableItems();
        List<ShopItemResponse> responses = items.stream()
            .map(ShopItemResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/items/all")
    public ResponseEntity<List<ShopItemResponse>> getAllItems() {
        List<ShopItem> items = shopService.getAllItems();
        List<ShopItemResponse> responses = items.stream()
            .map(ShopItemResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/items/category/{category}")
    public ResponseEntity<List<ShopItemResponse>> getItemsByCategory(@PathVariable String category) {
        List<ShopItem> items = shopService.getItemsByCategory(category);
        List<ShopItemResponse> responses = items.stream()
            .map(ShopItemResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ShopItemResponse> getItem(@PathVariable Long id) {
        ShopItem item = shopService.getItem(id);
        return ResponseEntity.ok(new ShopItemResponse(item));
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchaseItem(@Valid @RequestBody PurchaseRequest request) {
        Purchase purchase = shopService.purchaseItem(request.getNinjaId(), request.getItemId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new PurchaseResponse(purchase));
    }

    @GetMapping("/purchases/ninja/{ninjaId}")
    public ResponseEntity<List<PurchaseResponse>> getNinjaPurchases(@PathVariable Long ninjaId) {
        List<Purchase> purchases = shopService.getNinjaPurchases(ninjaId);
        List<PurchaseResponse> responses = purchases.stream()
            .map(PurchaseResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/purchases/ninja/{ninjaId}/unredeemed")
    public ResponseEntity<List<PurchaseResponse>> getUnredeemedPurchases(@PathVariable Long ninjaId) {
        List<Purchase> purchases = shopService.getUnredeemedPurchases(ninjaId);
        List<PurchaseResponse> responses = purchases.stream()
            .map(PurchaseResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/purchases/{id}/redeem")
    public ResponseEntity<PurchaseResponse> redeemPurchase(@PathVariable Long id) {
        Purchase purchase = shopService.redeemPurchase(id);
        return ResponseEntity.ok(new PurchaseResponse(purchase));
    }

    @DeleteMapping("/purchases/{id}/refund")
    public ResponseEntity<Void> refundPurchase(@PathVariable Long id) {
        shopService.refundPurchase(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items")
    public ResponseEntity<ShopItemResponse> createItem(@Valid @RequestBody CreateShopItemRequest request) {
        ShopItem item = shopService.createItem(
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getCategory(),
            request.getMaxPerStudent(),
            request.getMaxPerDay(),
            request.getMaxLifetime(),
            request.getMaxActiveAtOnce(),
            request.getRestrictedCategories()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new ShopItemResponse(item));
    }

    @PutMapping("/items/{id}/availability")
    public ResponseEntity<ShopItemResponse> updateItemAvailability(
        @PathVariable Long id,
        @RequestParam boolean available
    ) {
        ShopItem item = shopService.updateItemAvailability(id, available);
        return ResponseEntity.ok(new ShopItemResponse(item));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ShopItemResponse> updateItem(
        @PathVariable Long id,
        @Valid @RequestBody CreateShopItemRequest request
    ) {
        ShopItem item = shopService.updateItem(
            id,
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getCategory(),
            request.getMaxPerStudent(),
            request.getMaxPerDay(),
            request.getMaxLifetime(),
            request.getMaxActiveAtOnce(),
            request.getRestrictedCategories()
        );
        return ResponseEntity.ok(new ShopItemResponse(item));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        shopService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
