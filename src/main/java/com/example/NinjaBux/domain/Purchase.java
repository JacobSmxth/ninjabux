package com.example.NinjaBux.domain;

import com.example.NinjaBux.domain.enums.PurchaseStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Purchase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "ninja_id", nullable = false)
  private Ninja ninja;

  @ManyToOne
  @JoinColumn(name = "shop_item_id", nullable = false)
  private ShopItem shopItem;

  private int pricePaid;

  private LocalDateTime purchaseDate;

  private LocalDateTime redeemedDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PurchaseStatus status = PurchaseStatus.PURCHASED;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "spend_txn_id")
  private LedgerTxn spendTxn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "refund_txn_id")
  private LedgerTxn refundTxn;

  public Purchase() {
    this.purchaseDate = LocalDateTime.now();
    this.status = PurchaseStatus.PURCHASED;
  }

  public Purchase(Ninja ninja, ShopItem shopItem, int pricePaid) {
    this.ninja = ninja;
    this.shopItem = shopItem;
    this.pricePaid = pricePaid;
    this.purchaseDate = LocalDateTime.now();
    this.status = PurchaseStatus.PURCHASED;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Ninja getNinja() {
    return ninja;
  }

  public void setNinja(Ninja ninja) {
    this.ninja = ninja;
  }

  public ShopItem getShopItem() {
    return shopItem;
  }

  public void setShopItem(ShopItem shopItem) {
    this.shopItem = shopItem;
  }

  public int getPricePaid() {
    return pricePaid;
  }

  public void setPricePaid(int pricePaid) {
    this.pricePaid = pricePaid;
  }

  public LocalDateTime getPurchaseDate() {
    return purchaseDate;
  }

  public void setPurchaseDate(LocalDateTime purchaseDate) {
    this.purchaseDate = purchaseDate;
  }

  public LocalDateTime getRedeemedDate() {
    return redeemedDate;
  }

  public void setRedeemedDate(LocalDateTime redeemedDate) {
    this.redeemedDate = redeemedDate;
  }

  public PurchaseStatus getStatus() {
    return status;
  }

  public void setStatus(PurchaseStatus status) {
    this.status = status;
  }

  public LedgerTxn getSpendTxn() {
    return spendTxn;
  }

  public void setSpendTxn(LedgerTxn spendTxn) {
    this.spendTxn = spendTxn;
  }

  public LedgerTxn getRefundTxn() {
    return refundTxn;
  }

  public void setRefundTxn(LedgerTxn refundTxn) {
    this.refundTxn = refundTxn;
  }
}
