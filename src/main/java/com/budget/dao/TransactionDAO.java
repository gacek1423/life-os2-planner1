package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions(type, category, amount, date, description) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getType());
            pstmt.setString(2, t.getCategory());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setDate(4, Date.valueOf(t.getDate()));
            pstmt.setString(5, t.getDescription());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";

        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (java.sql.Connection conn = com.budget.db.DatabaseService.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}