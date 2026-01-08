package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.model.Goal;
import com.budget.model.GoalStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public List<Goal> getAllGoals() {
        List<Goal> list = new ArrayList<>();
        String sql = "SELECT * FROM goals";

        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Goal g = new Goal();
                g.setId(rs.getLong("id"));
                g.setName(rs.getString("name"));

                // Konwersja double (z bazy) -> BigDecimal (w modelu)
                g.setTargetAmount(BigDecimal.valueOf(rs.getDouble("target_amount")));
                g.setCurrentAmount(BigDecimal.valueOf(rs.getDouble("current_amount")));

                java.sql.Date d = rs.getDate("deadline");
                // W Twoim modelu pole nazywa się targetDate, a w bazie deadline
                if (d != null) g.setTargetDate(d.toLocalDate());

                // Status domyślny, bo w bazie go nie ma w prostej wersji
                g.setStatus(GoalStatus.ACTIVE);
                if (g.getCurrentAmount().compareTo(g.getTargetAmount()) >= 0) {
                    g.setStatus(GoalStatus.COMPLETED);
                }

                list.add(g);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addGoal(Goal g) {
        String sql = "INSERT INTO goals (name, target_amount, current_amount, deadline) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, g.getName());
            // Konwersja BigDecimal -> Double
            pstmt.setDouble(2, g.getTargetAmount() != null ? g.getTargetAmount().doubleValue() : 0.0);
            pstmt.setDouble(3, g.getCurrentAmount() != null ? g.getCurrentAmount().doubleValue() : 0.0);

            if (g.getTargetDate() != null) {
                pstmt.setDate(4, Date.valueOf(g.getTargetDate()));
            } else {
                pstmt.setDate(4, null);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateGoalAmount(Long id, BigDecimal newAmount) {
        String sql = "UPDATE goals SET current_amount = ? WHERE id = ?";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newAmount.doubleValue());
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteGoal(Long id) {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}