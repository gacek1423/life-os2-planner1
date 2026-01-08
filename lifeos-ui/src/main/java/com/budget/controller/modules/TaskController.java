package com.budget.controller.modules;

import com.budget.dao.TaskDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.model.Priority;
import com.budget.model.Task;
import com.budget.model.TaskStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority as LayoutPriority; // Alias, bo konflikt nazw z modelem
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TaskController {

    @FXML private TextField taskTitleField;
    @FXML private ComboBox<String> priorityBox;
    @FXML private DatePicker taskDatePicker;
    @FXML private ListView<Task> taskListView;

    private final TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        // Konfiguracja inputów
        priorityBox.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityBox.getSelectionModel().select("MEDIUM");
        taskDatePicker.setValue(LocalDate.now());

        // Konfiguracja listy
        setupListView();

        // Załaduj dane
        refreshTasks();
    }

    public void refreshTasks() {
        AsyncRunner.run(taskDAO::getAllTasks, tasks -> {
            taskListView.getItems().setAll(tasks);
        });
    }

    @FXML
    public void addTask() {
        String title = taskTitleField.getText();
        String priorityStr = priorityBox.getValue();
        LocalDate date = taskDatePicker.getValue();

        if (title.isEmpty() || date == null) return;

        // NAPRAWA: Tworzenie obiektu Task zgodnie z nowym modelem Enterprise
        Task t = new Task();
        t.setTitle(title);
        t.setDueDate(date);
        t.setStatus(TaskStatus.PENDING); // Domyślnie niewykonane

        // Bezpieczna konwersja String -> Enum Priority
        try {
            t.setPriority(Priority.valueOf(priorityStr));
        } catch (Exception e) {
            t.setPriority(Priority.MEDIUM);
        }

        AsyncRunner.run(() -> taskDAO.addTask(t), () -> {
            taskTitleField.clear();
            refreshTasks();
        });
    }

    private void setupListView() {
        taskListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox root = new HBox(10);
                    root.setAlignment(Pos.CENTER_LEFT);
                    root.setStyle("-fx-padding: 8; -fx-border-color: #323642; -fx-border-width: 0 0 1 0;");

                    // 1. Checkbox (Status)
                    CheckBox checkBox = new CheckBox();
                    // NAPRAWA: Sprawdzanie statusu przez Enum
                    checkBox.setSelected(item.getStatus() == TaskStatus.COMPLETED);

                    checkBox.setOnAction(e -> {
                        // NAPRAWA: Użycie toggleTaskStatus z DAO
                        AsyncRunner.run(
                                () -> taskDAO.toggleTaskStatus(item.getId(), checkBox.isSelected()),
                                () -> refreshTasks()
                        );
                    });

                    // 2. Tytuł
                    Label titleLbl = new Label(item.getTitle());
                    titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

                    if (item.getStatus() == TaskStatus.COMPLETED) {
                        titleLbl.setStyle("-fx-text-fill: #6c7280; -fx-strikethrough: true;");
                    }

                    // 3. Priorytet (Label)
                    String priorityName = (item.getPriority() != null) ? item.getPriority().name() : "MEDIUM";
                    Label priorityLbl = new Label(priorityName);

                    String pColor = switch (priorityName) {
                        case "HIGH" -> "#ff6b6b"; // Czerwony
                        case "LOW" -> "#51cf66";  // Zielony
                        default -> "#fcc419";     // Żółty
                    };
                    priorityLbl.setStyle("-fx-text-fill: " + pColor + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-border-color: " + pColor + "; -fx-border-radius: 3; -fx-padding: 2 4;");

                    // 4. Data
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, LayoutPriority.ALWAYS);

                    String dateStr = "";
                    if (item.getDueDate() != null) {
                        dateStr = item.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM"));
                    }

                    Label dateLbl = new Label(dateStr);
                    // Logika przeterminowania
                    if (item.getStatus() != TaskStatus.COMPLETED
                            && item.getDueDate() != null
                            && item.getDueDate().isBefore(LocalDate.now())) {
                        dateLbl.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                        dateLbl.setText(dateStr + " (!)");
                    } else {
                        dateLbl.setStyle("-fx-text-fill: #8b92a1;");
                    }

                    // 5. Przycisk Usuń
                    Button delBtn = new Button("✕");
                    delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c7280; -fx-cursor: hand;");
                    delBtn.setOnAction(e -> {
                        AsyncRunner.run(() -> taskDAO.deleteTask(item.getId()), () -> refreshTasks());
                    });

                    root.getChildren().addAll(checkBox, titleLbl, priorityLbl, spacer, dateLbl, delBtn);
                    setGraphic(root);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }
}