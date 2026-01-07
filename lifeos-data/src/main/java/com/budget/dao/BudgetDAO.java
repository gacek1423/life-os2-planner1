package com.budget.dao;

import com.budget.db.DatabaseService;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BudgetDAO {

    public Map<String, Double> getAllBudgets() {
        Map<String, Double> budgets = new HashMap<>();
        String sql = "SELECT * FROM category_budgets";
        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                budgets.put(rs.getString("category"), rs.getDouble("monthly_limit"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return budgets;
    }

    public void setBudget(String category, double limit) {
        // MERGE działa jak "Insert or Update" w H2
        String sql = "MERGE INTO category_budgets (category, monthly_limit) KEY(category) VALUES (?, ?)";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            pstmt.setDouble(2, limit);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}