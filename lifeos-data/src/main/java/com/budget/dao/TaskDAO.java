package com.budget.dao;

import com.budget.db.DatabaseService; // ZMIANA: Używamy poprawnego serwisu
import com.budget.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // Usunięto metodę createTable(), ponieważ DatabaseService.initDatabase() już to robi.

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY is_done ASC, due_date ASC";

        try (Connection conn = DatabaseService.connect(); // ZMIANA
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { tasks.add(mapRowToTask(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return tasks;
    }

    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title, is_done, due_date, priority) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseService.connect(); // ZMIANA
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setBoolean(2, task.isDone());
            pstmt.setDate(3, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : Date.valueOf(LocalDate.now()));
            pstmt.setString(4, task.getPriority());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, is_done = ?, due_date = ?, priority = ? WHERE id = ?";
        try (Connection conn = DatabaseService.connect(); // ZMIANA
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setBoolean(2, task.isDone());
            pstmt.setDate(3, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            pstmt.setString(4, task.getPriority());
            pstmt.setInt(5, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ... (metody delete i mapRowToTask bez zmian, tylko pamiętaj o imporcie DatabaseService)
    private Task mapRowToTask(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("due_date");
        return new Task(rs.getInt("id"), rs.getString("title"), rs.getBoolean("is_done"),
                sqlDate != null ? sqlDate.toLocalDate() : null, rs.getString("priority"));
    }
    // Wklej to wewnątrz klasy TaskDAO

    public java.util.List<Task> getUrgentTasks() {
        java.util.List<Task> tasks = new java.util.ArrayList<>();
        // Pobiera 5 pilnych zadań (niewykonane, posortowane po dacie)
        String sql = "SELECT * FROM tasks WHERE is_done = false ORDER BY due_date ASC LIMIT 5";

        try (java.sql.Connection conn = DatabaseService.connect();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("due_date");
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getBoolean("is_done"),
                        sqlDate != null ? sqlDate.toLocalDate() : null,
                        rs.getString("priority")
                ));
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return tasks;
    }
}