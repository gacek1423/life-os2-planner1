package com.budget.controller;

import com.budget.controller.modules.*; // Importuje wszystkie kontrolery modułów
import com.budget.infrastructure.EventBus;
import com.budget.model.Command;
import com.budget.modules.finance.events.TransactionAddedEvent;
import com.budget.modules.goals.events.GoalAddedEvent;
import com.budget.modules.tasks.events.TaskUpdatedEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;

import java.util.Arrays;
import java.util.List;

public class DashboardController {

    // --- NAV UI ---
    @FXML private Button btnKokpit, btnFinanse, btnZadania, btnCele, btnRaporty, btnUstawienia, btnKalendarz;
    @FXML private StackPane rootPane;
    @FXML private VBox sidebar, logoContainer;

    // --- WSTRZYKNIĘTE MODUŁY (Widoki + Kontrolery) ---
    // Nazwa zmiennej musi brzmieć: fx:id + "Controller"

    @FXML private Parent kokpitView;
    @FXML private KokpitController kokpitViewController;

    @FXML private Parent financeView;
    @FXML private FinanceController financeViewController;

    @FXML private Parent taskView;
    @FXML private TaskController taskViewController;

    @FXML private Parent goalView;
    @FXML private GoalController goalViewController;

    @FXML private Parent reportView;
    @FXML private ReportController reportViewController;

    @FXML private Parent calendarView;
    @FXML private CalendarController calendarViewController;

    @FXML private Parent settingsView;
    @FXML private SettingsController settingsViewController;

    // --- COMMAND PALETTE ---
    @FXML private StackPane commandPaletteOverlay;
    @FXML private TextField commandInput;
    @FXML private ListView<Command> commandList;
    private final javafx.collections.ObservableList<Command> allCommands = javafx.collections.FXCollections.observableArrayList();
    private boolean isSidebarCollapsed = false;

    @FXML
    public void initialize() {
        // Event Bus - Centralna Magistrala Danych
        // Gdy coś się zmieni, informujemy odpowiednie kontrolery, by się odświeżyły

        EventBus.subscribe(TransactionAddedEvent.class, e -> Platform.runLater(() -> {
            financeViewController.refreshFinances();
            kokpitViewController.refresh(); // Kokpit musi zaktualizować saldo
            reportViewController.refreshReports();
            calendarViewController.refreshCalendar();
        }));

        EventBus.subscribe(TaskUpdatedEvent.class, e -> Platform.runLater(() -> {
            taskViewController.refreshTasks();
            kokpitViewController.refresh(); // Kokpit musi zaktualizować licznik zadań
            calendarViewController.refreshCalendar();
        }));

        EventBus.subscribe(GoalAddedEvent.class, e -> Platform.runLater(() -> goalViewController.refreshGoals()));

        setupCommandPalette();
        showKokpit(); // Domyślny widok
    }

    // --- NAWIGACJA ---
    @FXML public void showKokpit() { switchView(kokpitView, btnKokpit); kokpitViewController.refresh(); }
    @FXML public void showFinanse() { switchView(financeView, btnFinanse); financeViewController.refreshFinances(); }
    @FXML public void showZadania() { switchView(taskView, btnZadania); taskViewController.refreshTasks(); }
    @FXML public void showCele() { switchView(goalView, btnCele); goalViewController.refreshGoals(); }
    @FXML public void showRaporty() { switchView(reportView, btnRaporty); reportViewController.refreshReports(); }
    @FXML public void showKalendarz() { switchView(calendarView, btnKalendarz); calendarViewController.refreshCalendar(); }
    @FXML public void showUstawienia() { switchView(settingsView, btnUstawienia); }

    private void switchView(Parent view, Button activeButton) {
        // Ukrywamy wszystko
        List<Parent> views = Arrays.asList(kokpitView, financeView, taskView, goalView, reportView, calendarView, settingsView);
        views.forEach(v -> v.setVisible(false));

        List<Button> btns = Arrays.asList(btnKokpit, btnFinanse, btnZadania, btnCele, btnRaporty, btnUstawienia, btnKalendarz);
        btns.forEach(b -> b.getStyleClass().remove("sidebar-button-active"));

        // Pokazujemy wybrane
        view.setVisible(true);
        activeButton.getStyleClass().add("sidebar-button-active");
    }

    @FXML
    private void toggleSidebar() {
        if (isSidebarCollapsed) {
            sidebar.setPrefWidth(240);
            logoContainer.setVisible(true);
            sidebar.getChildren().forEach(n -> { if(n instanceof Button b && !"☰".equals(b.getText())) { b.setContentDisplay(ContentDisplay.LEFT); b.setAlignment(Pos.CENTER_LEFT); }});
            isSidebarCollapsed = false;
        } else {
            sidebar.setPrefWidth(60);
            logoContainer.setVisible(false);
            sidebar.getChildren().forEach(n -> { if(n instanceof Button b && !"☰".equals(b.getText())) { b.setContentDisplay(ContentDisplay.TEXT_ONLY); b.setAlignment(Pos.CENTER); }});
            isSidebarCollapsed = true;
        }
    }

    private void setupCommandPalette() {
        // ... (Twój kod palety, używający metod powyżej, np. this::showFinanse) ...
        // Możesz tu też dodać komendy globalne
        allCommands.add(new Command("Zamknij", "Wyjście", () -> Platform.exit()));

        // ... obsługa klawiszy (CTRL+K) ...
    }

    // Metody pomocnicze dla Palety (żeby nie pisać logiki 2 razy)
    private void toggleCommandPalette() {
        commandPaletteOverlay.setVisible(!commandPaletteOverlay.isVisible());
        if(commandPaletteOverlay.isVisible()) { commandInput.clear(); commandInput.requestFocus(); }
    }
    private void executeSelectedCommand() { /* ... */ }
}