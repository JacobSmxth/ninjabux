package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.ShopItem;

public class ShopItemResponse {
    private Long id;
    private String name;
    private String description;
    private int price;
    private boolean available;
    private String category;
    private Integer maxPerStudent;
    private Integer maxPerDay;
    private Integer maxLifetime;
    private Integer maxActiveAtOnce;
    private String restrictedCategories;

    public ShopItemResponse(ShopItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.price = item.getPrice();
        this.available = item.isAvailable();
        this.category = item.getCategory();
        this.maxPerStudent = item.getMaxPerStudent();
        this.maxPerDay = item.getMaxPerDay();
        this.maxLifetime = item.getMaxLifetime();
        this.maxActiveAtOnce = item.getMaxActiveAtOnce();
        this.restrictedCategories = item.getRestrictedCategories();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getMaxPerStudent() { return maxPerStudent; }
    public void setMaxPerStudent(Integer maxPerStudent) { this.maxPerStudent = maxPerStudent; }

    public Integer getMaxPerDay() { return maxPerDay; }
    public void setMaxPerDay(Integer maxPerDay) { this.maxPerDay = maxPerDay; }

    public Integer getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(Integer maxLifetime) { this.maxLifetime = maxLifetime; }

    public Integer getMaxActiveAtOnce() { return maxActiveAtOnce; }
    public void setMaxActiveAtOnce(Integer maxActiveAtOnce) { this.maxActiveAtOnce = maxActiveAtOnce; }

    public String getRestrictedCategories() { return restrictedCategories; }
    public void setRestrictedCategories(String restrictedCategories) { this.restrictedCategories = restrictedCategories; }
}
