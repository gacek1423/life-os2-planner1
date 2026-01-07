package com.budget.ui;

import com.budget.db.DatabaseService; // <--- UPEWNIJ SIĘ, ŻE MASZ TEN IMPORT
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.budget.dao.GoalDAO;
import com.budget.modules.goals.GoalService;

import java.io.IOException;

public class App extends Application {
    private GoalService goalService;
    @Override
    public void start(Stage stage) throws IOException {
        // 1. NAJPIERW TWORZYMY TABELE W BAZIE
        DatabaseService.initDatabase(); // <--- DODAJ TĘ LINIĘ TUTAJ!
        goalService = new GoalService(new GoalDAO());
        // 2. POTEM ŁADUJEMY UI
        var location = getClass().getResource("/com/budget/dashboard.fxml");

        if (location == null) {
            throw new IllegalStateException("FATAL ERROR: Nie znaleziono pliku dashboard.fxml!");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("Life OS 2.0 Enterprise");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}