package com.budget.dao;

import com.budget.db.Database; // Zakładam, że masz klasę Database do połączenia
import com.budget.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    /**
     * Inicjalizuje tabelę w bazie danych, jeśli jeszcze nie istnieje.
     */
    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    is_done BOOLEAN DEFAULT 0,
                    due_date DATE,
                    priority TEXT
                );
                """;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pobiera wszystkie zadania z bazy.
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY is_done ASC, due_date ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Pobiera tylko pilne zadania (niewykonane, posortowane po dacie).
     * Używane w Kokpicie (Dashboard).
     */
    public List<Task> getUrgentTasks() {
        List<Task> tasks = new ArrayList<>();
        // Pobierz max 5 pilnych zadań
        String sql = "SELECT * FROM tasks WHERE is_done = 0 ORDER BY due_date ASC LIMIT 5";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Dodaje nowe zadanie.
     */
    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title, is_done, due_date, priority) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setBoolean(2, task.isDone());
            // Konwersja LocalDate na java.sql.Date
            pstmt.setDate(3, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : Date.valueOf(LocalDate.now()));
            pstmt.setString(4, task.getPriority());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aktualizuje istniejące zadanie (np. zmiana statusu isDone).
     */
    public void updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, is_done = ?, due_date = ?, priority = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setBoolean(2, task.isDone());
            pstmt.setDate(3, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            pstmt.setString(4, task.getPriority());
            pstmt.setInt(5, task.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Usuwa zadanie (opcjonalne, ale przydatne).
     */
    public void deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pomocnicza metoda mapująca wiersz z bazy na obiekt Task.
     */
    private Task mapRowToTask(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        boolean isDone = rs.getBoolean("is_done");

        Date sqlDate = rs.getDate("due_date");
        LocalDate dueDate = sqlDate != null ? sqlDate.toLocalDate() : null;

        String priority = rs.getString("priority");

        return new Task(id, title, isDone, dueDate, priority);
    }
}