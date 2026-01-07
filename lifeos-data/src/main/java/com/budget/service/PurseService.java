package com.budget.service; // ZMIANA PAKIETU

import com.budget.dao.PurseDAO;
import com.budget.modules.finance.domain.Purse;
import com.budget.modules.finance.domain.PurseType;

public class PurseService {
    private final PurseDAO purseDAO = new PurseDAO();

    // Główna logika transferu
    public void transferFunds(int fromId, int toId, double amount, String reason) throws Exception {
        Purse from = getPurseById(fromId); // Uproszczenie: DAO powinno mieć metodę getById
        // ... (reszta logiki walidacji, którą pisałeś wcześniej)

        // Na potrzeby startu wystarczy prosta metoda,
        // ale skoro używamy DAO bezpośrednio w kontrolerze,
        // ta klasa może być na razie pusta lub służyć do specjalnych operacji.
    }

    // Pomocnicza metoda (jeśli DAO jej nie ma, trzeba dodać do PurseDAO)
    private Purse getPurseById(int id) {
        return purseDAO.getAllPurses().stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Purse not found: " + id));
    }
}