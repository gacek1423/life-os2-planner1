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
    // Metoda do robienia backupu SQL
    public static void backupDatabase(String targetPath) throws SQLException {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // H2 command: BACKUP TO 'file.zip'
            stmt.execute(String.format("BACKUP TO '%s'", targetPath));
        }
    }
    public static void clearAllData() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            stmt.execute("TRUNCATE TABLE transactions");
            stmt.execute("TRUNCATE TABLE tasks");
            stmt.execute("TRUNCATE TABLE goals");
            // Uwaga: Purses mają klucze obce, więc kolejność jest ważna
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE purse_audit_log");
            stmt.execute("TRUNCATE TABLE purses");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            // Przywracanie domyślnych portfeli po resecie
            initDatabase();

            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
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
        // W metodzie initDatabase(), dodaj nowe zapytanie:
        String sqlBudgets = """
                CREATE TABLE IF NOT EXISTS category_budgets (
                    category VARCHAR(50) PRIMARY KEY,
                    monthly_limit DOUBLE
                );
                -- Przykładowe limity na start
                MERGE INTO category_budgets (category, monthly_limit) KEY(category) VALUES ('Jedzenie', 1500);
                MERGE INTO category_budgets (category, monthly_limit) KEY(category) VALUES ('Paliwo', 600);
                MERGE INTO category_budgets (category, monthly_limit) KEY(category) VALUES ('Rozrywka', 300);
            """;
        // W metodzie initDatabase():

        // Wklej to wewnątrz metody initDatabase(), w bloku TRY, obok innych stmt.execute(...)

        // 1. Tabela Portfeli (Purses)
                String sqlPurses = """
            CREATE TABLE IF NOT EXISTS purses (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                type VARCHAR(20) NOT NULL, -- LOCKED, FLEXIBLE, EMERGENCY
                allocated_amount DOUBLE DEFAULT 0.0,
                spent_amount DOUBLE DEFAULT 0.0,
                is_locked BOOLEAN DEFAULT FALSE,
                buffer_allowance DOUBLE DEFAULT 0.0
            );
            
            -- DANE STARTOWE (Tylko jeśli tabela pusta)
            MERGE INTO purses (id, name, type, allocated_amount, spent_amount, is_locked) 
            KEY(id) VALUES (1, 'Budżet Domowy', 'FLEXIBLE', 8000.0, 2450.0, false);
            
            MERGE INTO purses (id, name, type, allocated_amount, spent_amount, is_locked) 
            KEY(id) VALUES (2, 'Czynsz & Opłaty', 'LOCKED', 2500.0, 0.0, true);
            
            MERGE INTO purses (id, name, type, allocated_amount, spent_amount, is_locked, buffer_allowance) 
            KEY(id) VALUES (3, 'Jedzenie', 'FLEXIBLE', 1500.0, 450.0, false, 200.0);
            
            MERGE INTO purses (id, name, type, allocated_amount, spent_amount, is_locked) 
            KEY(id) VALUES (4, 'Buffer Awaryjny', 'EMERGENCY', 5000.0, 0.0, true);
        """;


        String sqlAudit = """
            CREATE TABLE IF NOT EXISTS purse_audit_log (
                id INT AUTO_INCREMENT PRIMARY KEY,
                purse_id INT,
                action_type VARCHAR(50), -- TRANSFER_IN, TRANSFER_OUT, BREAK_GLASS
                amount DOUBLE,
                reason VARCHAR(255),
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (purse_id) REFERENCES purses(id)
            );
        """;



        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTransactions);
            stmt.execute(sqlTasks);
            stmt.execute(sqlGoals); // Wykonujemy nowe zapytanie
            stmt.execute(sqlBudgets);
            stmt.execute(sqlPurses);
            stmt.execute(sqlAudit);

            System.out.println("Baza danych (Transakcje + Zadania + Cele) gotowa.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}