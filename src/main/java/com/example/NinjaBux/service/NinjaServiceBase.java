package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.repository.NinjaRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class NinjaServiceBase {

    @Autowired
    protected NinjaRepository ninjaRepository;

    protected Ninja findNinja(Long ninjaId) {
        return ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
    }
}
