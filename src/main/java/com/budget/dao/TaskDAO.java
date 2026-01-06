package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public void addTask(Task t) {
        String sql = "INSERT INTO tasks(title, priority, is_done, due_date) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getTitle());
            pstmt.setString(2, t.getPriority());
            pstmt.setBoolean(3, t.isDone());
            pstmt.setDate(4, Date.valueOf(t.getDueDate()));
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void toggleStatus(int id, boolean isDone) {
        String sql = "UPDATE tasks SET is_done = ? WHERE id = ?";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isDone);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY is_done ASC, due_date ASC"; // Najpierw do zrobienia
        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("priority"),
                        rs.getBoolean("is_done"),
                        rs.getDate("due_date").toLocalDate()
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}