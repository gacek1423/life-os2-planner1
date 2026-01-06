module com.budget {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires com.h2database;

    exports com.budget;

    // Otwórz pakiet z kontrolerem dla JavaFX FXML
    opens com.budget.controller to javafx.fxml;

    // Otwórz model dla JavaFX Base (TableView tego potrzebuje!)
    opens com.budget.model to javafx.base;
}