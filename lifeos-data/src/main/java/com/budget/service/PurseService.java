package com.budget.service;

import com.budget.dao.PurseDAO;
import com.budget.db.DatabaseService;
import com.budget.modules.finance.domain.Purse;

import java.sql.Connection;
import java.sql.SQLException;

public class PurseService {

    private final PurseDAO purseDAO = new PurseDAO();

    /**
     * Bezpieczny transfer środków między portfelami (Atomic Transaction).
     */
    public void transferFunds(int fromId, int toId, double amount, String reason) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Kwota musi być dodatnia");

        // Otwieramy połączenie, ale NIE zamykamy go w try-with-resources od razu,
        // musimy mieć kontrolę nad commit/rollback.
        Connection conn = null;

        try {
            conn = DatabaseService.connect();
            conn.setAutoCommit(false); // START TRANSAKCJI

            // 1. Pobierz stan portfeli
            Purse fromPurse = purseDAO.getPurseById(conn, fromId);
            Purse toPurse = purseDAO.getPurseById(conn, toId);

            if (fromPurse == null || toPurse == null) throw new Exception("Nie znaleziono portfela.");

            // 2. Walidacja biznesowa
            if (fromPurse.getAllocatedAmount() < amount) {
                throw new Exception("Niewystarczające środki w portfelu źródłowym: " + fromPurse.getName());
            }
            if (fromPurse.isLocked() && !"BREAK_GLASS".equals(reason)) {
                throw new Exception("Portfel źródłowy jest ZABLOKOWANY. Wymagana procedura awaryjna.");
            }

            // 3. Obliczenia
            double newFromAmount = fromPurse.getAllocatedAmount() - amount;
            double newToAmount = toPurse.getAllocatedAmount() + amount;

            // 4. Zapis zmian (Wszystko na tym samym conn!)
            purseDAO.updatePurseAllocation(conn, fromId, newFromAmount);
            purseDAO.updatePurseAllocation(conn, toId, newToAmount);

            // 5. Logowanie operacji
            purseDAO.logAudit(conn, fromId, "TRANSFER_OUT", amount, "Do: " + toPurse.getName() + " | " + reason);
            purseDAO.logAudit(conn, toId, "TRANSFER_IN", amount, "Od: " + fromPurse.getName() + " | " + reason);

            conn.commit(); // ZATWIERDZENIE ZMIAN
            System.out.println("✅ Transfer zakończony sukcesem.");

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // COFNIĘCIE ZMIAN W RAZIE BŁĘDU
                    System.err.println("⚠️ Transakcja wycofana: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e; // Rzuć wyjątek dalej, żeby UI wyświetliło błąd
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}