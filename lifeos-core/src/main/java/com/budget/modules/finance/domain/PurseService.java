package com.budget.modules.finance.domain;

import com.budget.modules.finance.domain.Purse;
import java.util.List;

public interface PurseService {
    List<Purse> getAllPurses();
    Purse getPurseById(Long id);
    Purse createPurse(Purse purse);
    Purse updatePurse(Purse purse);
    void deletePurse(Long id);
}