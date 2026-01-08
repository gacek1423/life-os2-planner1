package com.budget.controller.modules;

import com.budget.dao.TaskDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Task;
import com.budget.modules.tasks.events.TaskUpdatedEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskController {

    // --- ELEMENTY UI Z FXML ---
    @FXML private ListView<Task> taskListView;
    @FXML private TextField taskTitleField;
    @FXML private ComboBox<String> priorityBox;
    @FXML private DatePicker taskDatePicker;

    // --- DAO ---
    private final TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        // 1. Konfiguracja Priorytetów (z emoji dla lepszego wyglądu)
        priorityBox.setItems(FXCollections.observableArrayList("🔥 HIGH", "⚡ MEDIUM", "☕ LOW"));
        priorityBox.getSelectionModel().select(1); // Domyślnie MEDIUM

        // 2. Domyślna data - dzisiaj
        taskDatePicker.setValue(LocalDate.now());

        // 3. Konfiguracja wyglądu listy (Custom Cell Factory)
        setupListView();

        // 4. Pierwsze ładowanie danych
        refreshTasks();
    }

    /**
     * Metoda wywoływana przyciskiem "DODAJ" w pliku tasks.fxml
     */
    @FXML
    public void addTask() {
        String title = taskTitleField.getText();
        LocalDate date = taskDatePicker.getValue();
        String priority = priorityBox.getValue();

        if (title == null || title.trim().isEmpty()) {
            return; // Walidacja: Pusty tytuł
        }
        if (date == null) date = LocalDate.now();

        // Tworzenie obiektu zadania
        Task newTask = new Task(0, title, false, date, priority);

        // Zapis w tle
        AsyncRunner.run(() -> {
            taskDAO.addTask(newTask);
            // Ważne: Informujemy resztę systemu (np. Kokpit), że doszło zadanie
            EventBus.publish(new TaskUpdatedEvent());
        }, () -> {
            // Po zapisaniu: czyścimy formularz i odświeżamy listę
            taskTitleField.clear();
            taskDatePicker.setValue(LocalDate.now());
            refreshTasks();
        });
    }

    /**
     * Pobiera zadania z bazy i odświeża widok
     */
    public void refreshTasks() {
        AsyncRunner.run(() -> {
            List<Task> tasks = taskDAO.getAllTasks();

            // Sortowanie: Najpierw niewykonane, potem wg daty, na końcu wykonane
            return tasks.stream()
                    .sorted(Comparator.comparing(Task::isDone)
                            .thenComparing(Task::getDueDate))
                    .collect(Collectors.toList());
        }, sortedTasks -> {
            taskListView.setItems(FXCollections.observableArrayList(sortedTasks));
        });
    }

    /**
     * Konfiguruje wygląd pojedynczego wiersza na liście (CheckBox + Tytuł + Data)
     */
    private void setupListView() {
        taskListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Kontener na treść
                    HBox container = new HBox(10);
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    // 1. CheckBox (Stan wykonania)
                    CheckBox cb = new CheckBox();
                    cb.setSelected(item.isDone());
                    cb.getStyleClass().add("task-checkbox"); // Można dodać styl w CSS

                    // Logika kliknięcia w CheckBox
                    cb.setOnAction(e -> {
                        item.setDone(cb.isSelected());
                        updateTaskStatus(item);
                    });

                    // 2. Tytuł i Priorytet
                    String priorityIcon = getPriorityIcon(item.getPriority());
                    Label titleLabel = new Label(priorityIcon + " " + item.getTitle());
                    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

                    // Przekreślenie, jeśli zrobione
                    if (item.isDone()) {
                        titleLabel.setStyle("-fx-text-fill: #6c7280; -fx-strikethrough: true;");
                    }

                    // 3. Data (wyrównana do prawej)
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    String dateStr = item.getDueDate() != null ? item.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM")) : "";
                    Label dateLabel = new Label(dateStr);

                    // Kolorowanie daty: Czerwony jeśli po terminie i niezrobione
                    if (!item.isDone() && item.getDueDate().isBefore(LocalDate.now())) {
                        dateLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;"); // Po terminie
                    } else {
                        dateLabel.setStyle("-fx-text-fill: #8b92a1;");
                    }

                    container.getChildren().addAll(cb, titleLabel, spacer, dateLabel);
                    setGraphic(container);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");
                }
            }
        });
    }

    private void updateTaskStatus(Task task) {
        AsyncRunner.run(() -> {
            taskDAO.updateTask(task);
            EventBus.publish(new TaskUpdatedEvent());
        }, this::refreshTasks); // Po aktualizacji przesuń wykonane na dół
    }

    private String getPriorityIcon(String priority) {
        if (priority == null) return "";
        if (priority.contains("HIGH")) return "🔥";
        if (priority.contains("MEDIUM")) return "⚡";
        if (priority.contains("LOW")) return "☕";
        return "▪";
    }
}