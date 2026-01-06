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
        // 1. Upewnij się, że baza danych istnieje (tworzy tabelę przy pierwszym starcie)
        DatabaseService.initDatabase();

        // 2. Załaduj widok z FXML
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        stage.setTitle("Budget App - JDBC & JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}