package com.budget.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    // ULEPSZENIE: Dodano opcje buforowania (CACHE_SIZE) dla większej wydajności
    private static final String DB_URL = "jdbc:h2:" + System.getProperty("user.home") + "/lifeos_db;AUTO_SERVER=TRUE;CACHE_SIZE=131072";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // 1. TRANSAKCJE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    type VARCHAR(20),
                    category VARCHAR(100),
                    amount DOUBLE,
                    date DATE,
                    description VARCHAR(255)
                );
            """);

            // 2. BUDŻETY
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS category_budgets (
                    category VARCHAR(100) PRIMARY KEY,
                    monthly_limit DOUBLE
                );
            """);

            // 3. ZADANIA
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    is_done BOOLEAN DEFAULT FALSE,
                    due_date DATE,
                    priority VARCHAR(20)
                );
            """);

            // 4. CELE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS goals (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255),
                    target_amount DOUBLE,
                    current_amount DOUBLE DEFAULT 0,
                    deadline DATE
                );
            """);

            // 5. PORTFELE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS purses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    allocated_amount DOUBLE DEFAULT 0.0,
                    spent_amount DOUBLE DEFAULT 0.0,
                    is_locked BOOLEAN DEFAULT FALSE,
                    buffer_allowance DOUBLE DEFAULT 0.0
                );
            """);

            // 6. NOWOŚĆ: PŁATNOŚCI CYKLICZNE (Recurring Payments)
            // Przygotowane pod przyszłe funkcje Enterprise
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS recurring_transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100),
                    amount DOUBLE,
                    category VARCHAR(100),
                    frequency VARCHAR(20), -- MONTHLY, WEEKLY
                    day_of_month INT,
                    is_active BOOLEAN DEFAULT TRUE
                );
            """);

            // Dane startowe
            seedDefaultData(stmt);

            System.out.println("✅ Baza danych LifeOS Enterprise Ready.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedDefaultData(Statement stmt) throws SQLException {
        // Dodajemy przykładowe dane tylko jeśli baza jest pusta
        if (!stmt.getConnection().createStatement().executeQuery("SELECT COUNT(*) FROM category_budgets").next()) {
            stmt.execute("MERGE INTO category_budgets (category, monthly_limit) KEY(category) VALUES ('Jedzenie', 2000.0)");
            stmt.execute("MERGE INTO category_budgets (category, monthly_limit) KEY(category) VALUES ('Paliwo', 800.0)");
            stmt.execute("MERGE INTO category_budgets (category, monthly_limit) KEY(category) VALUES ('Dom', 3000.0)");
        }
    }

    public static void clearAllData() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE transactions");
            stmt.execute("TRUNCATE TABLE tasks");
            stmt.execute("TRUNCATE TABLE goals");
            stmt.execute("TRUNCATE TABLE purses");
            stmt.execute("TRUNCATE TABLE recurring_transactions");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            initDatabase(); // Odtwórz strukturę i seed
        } catch (SQLException e) { e.printStackTrace(); }
    }
}