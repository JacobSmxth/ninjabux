package com.example.NinjaBux.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class ShopItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    private String name;

    @NotBlank(message = "Item description is required")
    private String description;

    @Min(value = 1, message = "Price must be at least 1 bux")
    private int price;

    private boolean available = true;

    private String category; // e.g., "break-time", "fun", "experience", "premium"

    // Purchase limits (plan to implement if there is abuse of how much you can buy)
    private Integer maxPerStudent;
    private Integer maxPerDay;
    private Integer maxLifetime;
    private Integer maxActiveAtOnce;
    private String restrictedCategories;

    public ShopItem() {}

    public ShopItem(String name, String description, int price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
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


    // still not sure if needed
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
