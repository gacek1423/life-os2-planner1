package com.budget;

import com.budget.db.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Inicjalizacja bazy
        DatabaseService.initDatabase();

        // Ładowanie GŁÓWNEGO DASHBOARDU (bez logowania)
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/budget/dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        stage.setTitle("Life OS 2.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}