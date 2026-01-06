package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.model.Goal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public void addGoal(Goal g) {
        String sql = "INSERT INTO goals(name, target_amount, current_amount, deadline) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, g.getName());
            pstmt.setDouble(2, g.getTargetAmount());
            pstmt.setDouble(3, g.getCurrentAmount());
            // Obsługa nulla dla daty (deadline może być pusty)
            if (g.getDeadline() != null) {
                pstmt.setDate(4, Date.valueOf(g.getDeadline()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Goal> getAllGoals() {
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
}