package com.budget.ui;

import com.budget.controller.SplashController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Główna klasa uruchomieniowa JavaFX
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Ładowanie Splash Screena
        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("/com/budget/splash.fxml"));
        Parent splashRoot = splashLoader.load();
        SplashController splashController = splashLoader.getController();

        // 2. Konfiguracja okna Splash
        Scene splashScene = new Scene(splashRoot);
        splashScene.setFill(Color.TRANSPARENT);

        primaryStage.setScene(splashScene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();

        // 3. Wątek w tle (Symulacja ładowania systemu)
        new Thread(() -> {
            try {
                // Symulacja kroków ładowania
                Thread.sleep(500);
                splashController.updateProgress("Łączenie z bazą danych...", 0.2);
                com.budget.db.DatabaseService.initDatabase();

                Thread.sleep(600);
                splashController.updateProgress("Wczytywanie modułu Finanse...", 0.4);

                Thread.sleep(400);
                splashController.updateProgress("Inicjalizacja zadań i celów...", 0.6);

                Thread.sleep(500);
                splashController.updateProgress("Generowanie raportów...", 0.8);

                Thread.sleep(400);
                splashController.updateProgress("System gotowy.", 1.0);
                Thread.sleep(300);

                // 4. Przełączenie na główne okno (wątek UI)
                Platform.runLater(() -> {
                    try {
                        showMainDashboard(new Stage());
                        primaryStage.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            } catch (InterruptedException e) {
                // ZMIANA: Usunięto "| IOException", bo IOException jest obsłużony wyżej (wewnątrz runLater)
                e.printStackTrace();
            }
        }).start();
    }

    private void showMainDashboard(Stage mainStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/budget/dashboard.fxml"));
        Parent root = loader.load();

        mainStage.setMinWidth(1000);
        mainStage.setMinHeight(700);

        Scene scene = new Scene(root, 1280, 800);
        mainStage.setScene(scene);
        mainStage.setTitle("Life OS 2.0 Enterprise");
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}