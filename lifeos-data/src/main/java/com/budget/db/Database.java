package com.budget.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    // Ścieżka do bazy danych: Zapisujemy ją w folderze domowym użytkownika (C:\Users\Ty\lifeos_db)
    // AUTO_SERVER=TRUE pozwala otwierać bazę w kilku procesach (np. aplikacja + konsola H2)
    private static final String DB_URL = "jdbc:h2:" + System.getProperty("user.home") + "/lifeos_db;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    /**
     * Pobiera połączenie do bazy danych.
     * Używane przez wszystkie klasy DAO.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Metoda inicjalizująca - sprawdza połączenie i tworzy tabele, jeśli nie istnieją.
     * Wywołamy ją przy starcie aplikacji.
     */
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Tabela Transakcji
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT,
                    category TEXT,
                    amount DOUBLE,
                    date DATE,
                    description TEXT
                );
            """);

            // 2. Tabela Zadań (To naprawi Twój TaskDAO)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    is_done BOOLEAN DEFAULT 0,
                    due_date DATE,
                    priority TEXT
                );
            """);

            // 3. Tabela Celów
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    target_amount DOUBLE,
                    current_amount DOUBLE,
                    deadline DATE
                );
            """);

            System.out.println("✅ Baza danych zainicjalizowana poprawnie: " + DB_URL);

        } catch (SQLException e) {
            System.err.println("❌ Błąd inicjalizacji bazy danych!");
            e.printStackTrace();
        }
    }
}