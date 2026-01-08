package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.modules.finance.domain.Purse;
import com.budget.modules.finance.domain.PurseType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurseDAO {

    public List<Purse> getAllPurses() {
        List<Purse> list = new ArrayList<>();
        String sql = "SELECT * FROM purses ORDER BY id";
        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Purse(
                        rs.getInt("id"), rs.getString("name"), PurseType.valueOf(rs.getString("type")),
                        rs.getDouble("allocated_amount"), rs.getDouble("spent_amount"),
                        rs.getBoolean("is_locked"), rs.getDouble("buffer_allowance")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- NOWE METODY DO TRANSAKCJI ---

    // Aktualizuje kwotę w portfelu (np. przesunięcie środków)
    public void updatePurseAllocation(Connection conn, int purseId, double newAmount) throws SQLException {
        String sql = "UPDATE purses SET allocated_amount = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newAmount);
            pstmt.setInt(2, purseId);
            pstmt.executeUpdate();
        }
    }

    // Zapisuje log audytowy (kto, co, gdzie)
    public void logAudit(Connection conn, int purseId, String action, double amount, String reason) throws SQLException {
        String sql = "INSERT INTO purse_audit_log (purse_id, action_type, amount, reason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purseId);
            pstmt.setString(2, action);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, reason);
            pstmt.executeUpdate();
        }
    }

    // Pobiera portfel wewnątrz aktywnej transakcji (blokada wiersza opcjonalna, tu standardowy select)
    public Purse getPurseById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM purses WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Purse(
                        rs.getInt("id"), rs.getString("name"), PurseType.valueOf(rs.getString("type")),
                        rs.getDouble("allocated_amount"), rs.getDouble("spent_amount"),
                        rs.getBoolean("is_locked"), rs.getDouble("buffer_allowance")
                );
            }
        }
        return null;
    }
}