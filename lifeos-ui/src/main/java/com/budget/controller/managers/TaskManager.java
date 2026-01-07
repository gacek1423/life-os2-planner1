package com.budget.controller.managers;

import com.budget.dao.TaskDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Task;
import com.budget.modules.tasks.events.TaskUpdatedEvent;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class TaskManager {
    private final TaskDAO taskDAO = new TaskDAO();

    // UI Elements
    private final ListView<Task> taskListView;
    private final ListView<Task> urgentTasksList; // To lista na Kokpicie
    private final TextField titleField;
    private final ComboBox<String> priorityBox;
    private final DatePicker datePicker;
    private final Label dashTasksLabel; // Licznik na kokpicie

    public TaskManager(ListView<Task> taskListView, ListView<Task> urgentTasksList,
                       TextField titleField, ComboBox<String> priorityBox,
                       DatePicker datePicker, Label dashTasksLabel) {
        this.taskListView = taskListView;
        this.urgentTasksList = urgentTasksList;
        this.titleField = titleField;
        this.priorityBox = priorityBox;
        this.datePicker = datePicker;
        this.dashTasksLabel = dashTasksLabel;
    }

    public void setup() {
        priorityBox.setItems(FXCollections.observableArrayList("HIGH", "MEDIUM", "LOW"));
        priorityBox.getSelectionModel().select("MEDIUM");
        datePicker.setValue(LocalDate.now());

        setupListView(taskListView);
        if (urgentTasksList != null) setupListView(urgentTasksList); // Kokpit może być nullem przy starcie, ale tu jest ok

        refreshTasks();
    }

    private void setupListView(ListView<Task> list) {
        list.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Task t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setGraphic(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    HBox c = new HBox(10); c.setAlignment(Pos.CENTER_LEFT);
                    CheckBox cb = new CheckBox(); cb.setSelected(t.isDone());
                    cb.setOnAction(e -> AsyncRunner.run(() -> {
                        taskDAO.toggleStatus(t.getId(), cb.isSelected());
                        EventBus.publish(new TaskUpdatedEvent());
                    }, () -> {}));

                    Label l = new Label(t.getTitle());
                    l.setStyle("-fx-text-fill: #e1e4e8;");
                    if ("HIGH".equals(t.getPriority())) l.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                    if (t.isDone()) l.setStyle("-fx-text-fill: #8b92a1; -fx-strikethrough: true;");

                    c.getChildren().addAll(cb, l);
                    setGraphic(c);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
    }

    public void addTask() {
        if(titleField.getText().isEmpty()) return;
        Task t = new Task(0, titleField.getText(), priorityBox.getValue(), false, datePicker.getValue());
        AsyncRunner.run(() -> {
            taskDAO.addTask(t);
            EventBus.publish(new TaskUpdatedEvent());
        }, () -> titleField.clear());
    }

    public void refreshTasks() {
        AsyncRunner.run(taskDAO::getAllTasks, list -> {
            // 1. Główna lista
            taskListView.setItems(FXCollections.observableArrayList(list));

            // 2. Kokpit (Licznik + Pilne)
            long count = list.stream().filter(t -> !t.isDone()).count();
            if (dashTasksLabel != null) dashTasksLabel.setText(count + " zadań");

            if (urgentTasksList != null) {
                List<Task> urgent = list.stream()
                        .filter(t -> !t.isDone())
                        .sorted(Comparator.comparing(Task::getDueDate))
                        .limit(6)
                        .toList();
                urgentTasksList.setItems(FXCollections.observableArrayList(urgent));
            }
        });
    }
}