package com.budget.dao;

import com.budget.db.DatabaseService;
import com.budget.model.Priority;
import com.budget.model.Task;
import com.budget.model.TaskStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY is_done ASC, due_date ASC";

        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- PRZYWRÓCONA METODA (Dla Kokpitu) ---
    public List<Task> getUrgentTasks() {
        List<Task> list = new ArrayList<>();
        // Pobieramy 5 zadań, które nie są wykonane, sortując od najpilniejszych
        String sql = "SELECT * FROM tasks WHERE is_done = false ORDER BY due_date ASC LIMIT 5";

        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addTask(Task t) {
        String sql = "INSERT INTO tasks (title, is_done, due_date, priority) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getTitle());
            pstmt.setBoolean(2, t.getStatus() == TaskStatus.COMPLETED);

            if (t.getDueDate() != null) {
                pstmt.setDate(3, Date.valueOf(t.getDueDate()));
            } else {
                pstmt.setDate(3, null);
            }

            pstmt.setString(4, t.getPriority() != null ? t.getPriority().name() : "MEDIUM");

            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void toggleTaskStatus(Long id, boolean isDone) {
        String sql = "UPDATE tasks SET is_done = ? WHERE id = ?";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isDone);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteTask(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getLong("id"));
        t.setTitle(rs.getString("title"));

        boolean done = rs.getBoolean("is_done");
        t.setStatus(done ? TaskStatus.COMPLETED : TaskStatus.PENDING);

        java.sql.Date d = rs.getDate("due_date");
        if (d != null) t.setDueDate(d.toLocalDate());

        String p = rs.getString("priority");
        if (p != null) t.setPriority(Priority.valueOf(p));
        else t.setPriority(Priority.MEDIUM);

        return t;
    }
}