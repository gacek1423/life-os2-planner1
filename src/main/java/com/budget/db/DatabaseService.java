package com.budget.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    // ... (DB_URL, USER, PASS bez zmian) ...
    private static final String DB_URL = "jdbc:h2:./budget_db";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initDatabase() {
        // SQL dla Transakcji
        String sqlTransactions = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    type VARCHAR(10),
                    category VARCHAR(50),
                    amount DOUBLE,
                    date DATE,
                    description VARCHAR(255)
                );
                """;

        // SQL dla Zadań
        String sqlTasks = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255),
                    priority VARCHAR(20),
                    is_done BOOLEAN,
                    due_date DATE
                );
                """;

        // NOWOŚĆ: SQL dla Celów
        String sqlGoals = """
                CREATE TABLE IF NOT EXISTS goals (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255),
                    target_amount DOUBLE,   -- Ile chcemy uzbierać
                    current_amount DOUBLE,  -- Ile już mamy
                    deadline DATE           -- Do kiedy (opcjonalne)
                );
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTransactions);
            stmt.execute(sqlTasks);
            stmt.execute(sqlGoals); // Wykonujemy nowe zapytanie
            System.out.println("Baza danych (Transakcje + Zadania + Cele) gotowa.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}