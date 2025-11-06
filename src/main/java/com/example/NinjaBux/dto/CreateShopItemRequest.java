package com.example.NinjaBux.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateShopItemRequest {
    @NotBlank(message = "Item name is required")
    private String name;

    @NotBlank(message = "Item description is required")
    private String description;

    @Min(value = 1, message = "Price must be at least 1 bux")
    private int price;

    private String category;
    private Integer maxPerStudent;
    private Integer maxPerDay;
    private Integer maxLifetime;
    private Integer maxActiveAtOnce;
    private String restrictedCategories;

    public CreateShopItemRequest() {}

    public CreateShopItemRequest(String name, String description, int price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
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
