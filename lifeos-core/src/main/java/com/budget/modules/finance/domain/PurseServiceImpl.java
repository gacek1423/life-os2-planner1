package com.budget.modules.finance.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurseServiceImpl implements PurseService {
    
    private final Map<Long, Purse> purses = new HashMap<>();
    private long purseIdCounter = 1;
    
    @Override
    public List<Purse> getAllPurses() {
        return new ArrayList<>(purses.values());
    }
    
    @Override
    public Purse getPurseById(Long id) {
        return purses.get(id);
    }
    
    @Override
    public Purse createPurse(Purse purse) {
        purse.setId((int) purseIdCounter++);
        purses.put((long) purse.getId(), purse);
        return purse;
    }
    
    @Override
    public Purse updatePurse(Purse purse) {
        if (purses.containsKey(purse.getId())) {
            purses.put((long) purse.getId(), purse);
            return purse;
        }
        return null;
    }
    
    @Override
    public void deletePurse(Long id) {
        purses.remove(id);
    }
}