package com.budget.controller.modules;

import com.budget.dao.GoalDAO; // Zakładam istnienie
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Goal;
import com.budget.modules.goals.events.GoalAddedEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class GoalController {
    @FXML private ListView<Goal> goalListView;
    @FXML private TextField goalNameField, goalTargetField, goalCurrentField;
    @FXML private DatePicker goalDatePicker;

    // Użyj symulacji lub prawdziwego DAO
    // private final GoalDAO goalDAO = new GoalDAO();

    @FXML
    public void initialize() {
        goalDatePicker.setValue(LocalDate.now().plusMonths(6));
        refreshGoals();
    }

    @FXML
    public void addGoal() {
        // Logika dodawania celu...
        // Goal g = new Goal(...);
        // goalDAO.addGoal(g);
        EventBus.publish(new GoalAddedEvent()); // null dla uproszczenia
        refreshGoals();
    }

    public void refreshGoals() {
        // Pobieranie celów z bazy...
        // goalListView.setItems(...);
    }
}