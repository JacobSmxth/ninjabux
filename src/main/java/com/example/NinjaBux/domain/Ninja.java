package com.example.NinjaBux.domain;

import com.example.NinjaBux.domain.enums.BeltType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "ninjas",
    indexes = {@Index(name = "idx_ninjas_username", columnList = "username", unique = true)})
public class Ninja {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Must have firstName")
  private String firstName;

  @NotBlank(message = "Must have lastName")
  private String lastName;

  @NotBlank(message = "Must have username")
  @Column(unique = true)
  private String username;

  private int currentLevel;
  private int currentLesson;

  @Enumerated(EnumType.STRING)
  private BeltType currentBeltType;

  private LocalDateTime createdAt;
  private LocalDateTime lastProgressUpdate;

  private int legacyLessons = 0;

  private int totalLessons = 0;

  private boolean isLocked = false;

  public Ninja() {}

  public Ninja(
      String firstName,
      String lastName,
      String username,
      Integer currentLesson,
      Integer currentLevel,
      BeltType currentBeltType) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.username = username;
    this.currentLesson = currentLesson;
    this.currentLevel = currentLevel;
    this.currentBeltType = currentBeltType;
    this.isLocked = false;
    setTimestamps();
  }

  @PrePersist
  public void setTimestamps() {
    this.createdAt = LocalDateTime.now();
    this.lastProgressUpdate = LocalDateTime.now();
  }

  public void lock() {
    this.isLocked = true;
  }

  public void unlock() {
    this.isLocked = false;
  }

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getUsername() {
    return username;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public int getCurrentLesson() {
    return currentLesson;
  }

  public BeltType getCurrentBeltType() {
    return currentBeltType;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getLastProgressUpdate() {
    return lastProgressUpdate;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setFirstName(String firstname) {
    this.firstName = firstname;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setCurrentLevel(int currentLevel) {
    this.currentLevel = currentLevel;
  }

  public void setCurrentLesson(int currentLesson) {
    this.currentLesson = currentLesson;
  }

  public void setCurrentBeltType(BeltType currentBeltType) {
    this.currentBeltType = currentBeltType;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setLastProgressUpdate(LocalDateTime lastProgressUpdate) {
    this.lastProgressUpdate = lastProgressUpdate;
  }

  public int getLegacyLessons() {
    return legacyLessons;
  }

  public void setLegacyLessons(int legacyLessons) {
    this.legacyLessons = legacyLessons;
  }

  public int getTotalLessons() {
    return totalLessons;
  }

  public void setTotalLessons(int totalLessons) {
    this.totalLessons = totalLessons;
  }

  public boolean isLocked() {
    return isLocked;
  }

  public void setLocked(boolean locked) {
    this.isLocked = locked;
  }
}
