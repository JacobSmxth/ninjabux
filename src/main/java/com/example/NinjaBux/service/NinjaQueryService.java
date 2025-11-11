package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.exception.AccountLockedException;
import com.example.NinjaBux.repository.NinjaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class NinjaQueryService {

  @Autowired private NinjaRepository ninjaRepository;

  public Optional<Ninja> getNinja(Long id) {
    return ninjaRepository.findById(id);
  }

  public List<Ninja> getAllNinjas() {
    return ninjaRepository.findAll();
  }

  public Page<Ninja> getNinjasPaginated(
      int page,
      int size,
      String sortBy,
      String direction,
      String nameFilter,
      BeltType beltFilter,
      Boolean lockedFilter) {
    Sort.Direction sortDirection =
        "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sort;

    switch (sortBy != null ? sortBy.toLowerCase() : "name") {
      case "belt":
        sort = Sort.by(sortDirection, "currentBeltType");
        break;
      case "level":
        sort = Sort.by(sortDirection, "currentLevel");
        break;
      case "created":
        sort = Sort.by(sortDirection, "createdAt");
        break;
      case "locked":
        sort = Sort.by(sortDirection, "locked");
        break;
      case "name":
      default:
        sort = Sort.by(sortDirection, "firstName", "lastName");
        break;
    }

    Pageable pageable = PageRequest.of(page, size, sort);
    String name = (nameFilter != null && !nameFilter.trim().isEmpty()) ? nameFilter.trim() : null;

    return ninjaRepository.findByFilters(name, beltFilter, lockedFilter, pageable);
  }

  public Optional<Ninja> getNinjaByUsername(String username) {
    Optional<Ninja> ninjaOpt = ninjaRepository.findByUsernameIgnoreCase(username);
    if (ninjaOpt.isPresent() && ninjaOpt.get().isLocked()) {
      throw new AccountLockedException("Account is locked");
    }
    return ninjaOpt;
  }
}
