package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.model.Goal;
// WAŻNE: Importujemy interfejs z Core
import com.budget.modules.goals.GoalRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// WAŻNE: Musisz dodać "implements GoalRepository"
public class GoalDAO implements GoalRepository {

    @Override
    public void addGoal(Goal g) {
        // ... (Twój kod bez zmian) ...
        String sql = "INSERT INTO goals(name, target_amount, current_amount, deadline) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, g.getName());
            pstmt.setDouble(2, g.getTargetAmount());
            pstmt.setDouble(3, g.getCurrentAmount());
            if (g.getDeadline() != null) {
                pstmt.setDate(4, Date.valueOf(g.getDeadline()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Goal> getAllGoals() {
        // ... (Twój kod bez zmian) ...
        List<Goal> list = new ArrayList<>();
        String sql = "SELECT * FROM goals ORDER BY deadline ASC NULLS LAST";
        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date dateSql = rs.getDate("deadline");
                list.add(new Goal(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("target_amount"),
                        rs.getDouble("current_amount"),
                        dateSql != null ? dateSql.toLocalDate() : null
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // WAŻNE: Ta metoda jest wymagana przez interfejs GoalRepository!
    @Override
    public void updateCurrentAmount(int id, double newAmount) {
        String sql = "UPDATE goals SET current_amount = ? WHERE id = ?";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newAmount);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}