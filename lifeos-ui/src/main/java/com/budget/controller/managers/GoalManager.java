package com.budget.controller.managers;

import com.budget.dao.GoalDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Goal;
import com.budget.modules.goals.events.GoalAddedEvent;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class GoalManager {
    private final GoalDAO goalDAO = new GoalDAO();

    private final ListView<Goal> goalListView;
    private final TextField nameField, targetField, currentField;
    private final DatePicker datePicker;

    public GoalManager(ListView<Goal> goalListView, TextField nameField, TextField targetField, TextField currentField, DatePicker datePicker) {
        this.goalListView = goalListView;
        this.nameField = nameField;
        this.targetField = targetField;
        this.currentField = currentField;
        this.datePicker = datePicker;
    }

    public void setup() {
        goalListView.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Goal g, boolean empty) {
                super.updateItem(g, empty);
                if(empty||g==null) { setGraphic(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    VBox c = new VBox(5);
                    Label n = new Label(g.getName() + " " + String.format("(%.0f%%)", g.getProgress()*100));
                    n.setStyle("-fx-text-fill: white; font-weight: bold;");
                    ProgressBar pb = new ProgressBar(g.getProgress());
                    pb.setMaxWidth(Double.MAX_VALUE);
                    pb.setStyle("-fx-accent: #4fa3c7;");
                    c.getChildren().addAll(n, pb);
                    setGraphic(c);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }
        });
        refreshGoals();
    }

    public void addGoal() {
        try {
            double target = Double.parseDouble(targetField.getText());
            double current = currentField.getText().isEmpty() ? 0 : Double.parseDouble(currentField.getText());
            Goal g = new Goal(0, nameField.getText(), target, current, datePicker.getValue());

            AsyncRunner.run(() -> {
                goalDAO.addGoal(g);
                EventBus.publish(new GoalAddedEvent());
            }, () -> {
                nameField.clear(); targetField.clear(); currentField.clear();
                refreshGoals();
            });
        } catch(Exception e){}
    }

    public void refreshGoals() {
        AsyncRunner.run(goalDAO::getAllGoals, l -> goalListView.setItems(FXCollections.observableArrayList(l)));
    }
}