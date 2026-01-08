package com.budget.ui;

import com.budget.controller.DashboardController;
import com.budget.controller.HabitsController;
import com.budget.controller.GoalsController;
import com.budget.controller.ReportsController;
import com.budget.controller.EnhancedDashboardController;
import com.budget.modules.dashboard.DashboardService;
import com.budget.modules.dashboard.DashboardServiceImpl;
import com.budget.modules.reports.ReportService;
import com.budget.modules.reports.ReportServiceImpl;
import com.budget.modules.habits.HabitService;
import com.budget.modules.habits.HabitServiceImpl;
import com.budget.modules.goals.GoalService;
import com.budget.modules.goals.GoalServiceImpl;
import com.budget.modules.finance.domain.PurseService;
import com.budget.modules.finance.domain.PurseServiceImpl;
import com.budget.modules.tasks.TaskService;
import com.budget.modules.tasks.TaskServiceImpl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

public class App extends Application {

    private HabitService habitService;
    private DashboardService dashboardService;
    private GoalService goalService;
    private PurseService purseService;
    private TaskService taskService;
    private ReportService reportService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicjalizacja serwisów
        initializeServices();

        primaryStage.setTitle("Life OS Planner - System Zarządzania Życiem");

        // Utwórz główne okno z zakładkami
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Zakładka Dashboard (ulepszony)
        Tab dashboardTab = new Tab("📊 Dashboard");
        dashboardTab.setContent(createEnhancedDashboardView());
        dashboardTab.setClosable(false);

        // Zakładka Nawyki
        Tab habitsTab = new Tab("Nawyki");
        habitsTab.setContent(createHabitsView());
        habitsTab.setClosable(false);

        // Zakładka Cele
        Tab goalsTab = new Tab("🎯 Cele");
        goalsTab.setContent(createGoalsView());
        goalsTab.setClosable(false);

        // Zakładka Nawyki
        Tab habitsTab = new Tab("🔄 Nawyki");
        habitsTab.setContent(createHabitsView());
        habitsTab.setClosable(false);

        // Zakładka Raporty
        Tab reportsTab = new Tab("📈 Raporty");
        reportsTab.setContent(createReportsView());
        reportsTab.setClosable(false);

        // Zakładka Finanse (istniejąca)
        Tab financeTab = new Tab("💰 Finanse");
        financeTab.setContent(createFinanceView());
        financeTab.setClosable(false);

        // Zakładka Zadania (istniejąca)
        Tab tasksTab = new Tab("📝 Zadania");
        tasksTab.setContent(createTasksView());
        tasksTab.setClosable(false);

        // Dodaj zakładki
        tabPane.getTabs().addAll(dashboardTab, habitsTab, goalsTab, reportsTab, financeTab, tasksTab);

        // Ustaw scene
        Scene scene = new Scene(tabPane, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    private void initializeServices() {
        // Inicjalizacja serwisów
        this.habitService = new HabitServiceImpl();
        this.purseService = new PurseServiceImpl();
        this.goalService = new GoalServiceImpl();
        this.taskService = new TaskServiceImpl();
        this.dashboardService = new DashboardServiceImpl(habitService, purseService, goalService, taskService);
        this.reportService = new ReportServiceImpl(habitService, dashboardService, goalService, taskService, purseService);

        // Dodaj przykładowe dane
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Przykładowe nawyki
        if (habitService.getAllHabits().isEmpty()) {
            Habit morningExercise = new Habit();
            morningExercise.setName("Poranna gimnastyka");
            morningExercise.setDescription("15 minut ćwiczeń rozciągających");
            morningExercise.setCategory(HabitCategory.HEALTH);
            morningExercise.setFrequency(HabitFrequency.DAILY);
            morningExercise.setStartDate(LocalDate.now().minusDays(7));
            morningExercise.setTargetStreak(30);
            morningExercise.setActive(true);
            habitService.createHabit(morningExercise);

            Habit reading = new Habit();
            reading.setName("Czytanie książki");
            reading.setDescription("Minimum 30 minut czytania dziennie");
            reading.setCategory(HabitCategory.LEARNING);
            reading.setFrequency(HabitFrequency.DAILY);
            reading.setStartDate(LocalDate.now().minusDays(3));
            reading.setTargetStreak(21);
            reading.setActive(true);
            habitService.createHabit(reading);

            Habit meditation = new Habit();
            meditation.setName("Meditacja");
            meditation.setDescription("10 minut mindfulness");
            meditation.setCategory(HabitCategory.MENTAL_WELLBEING);
            meditation.setFrequency(HabitFrequency.DAILY);
            meditation.setStartDate(LocalDate.now().minusDays(5));
            meditation.setTargetStreak(30);
            meditation.setActive(true);
            habitService.createHabit(meditation);
        }

        // Przykładowe cele
        if (goalService.getAllGoals().isEmpty()) {
            Goal vacationGoal = new Goal();
            vacationGoal.setName("Wakacje w Hiszpanii");
            vacationGoal.setDescription("Tygodniowy wyjazd nad morze");
            vacationGoal.setTargetAmount(new BigDecimal("5000"));
            vacationGoal.setCurrentAmount(new BigDecimal("1200"));
            vacationGoal.setTargetDate(LocalDate.now().plusMonths(6));
            vacationGoal.setCategory(GoalCategory.TRAVEL);
            vacationGoal.setPriority(Priority.HIGH);
            vacationGoal.setStatus(GoalStatus.ACTIVE);
            goalService.createGoal(vacationGoal);

            Goal bikeGoal = new Goal();
            bikeGoal.setName("Nowy rower");
            bikeGoal.setDescription("Rower górski do jazdy po lesie");
            bikeGoal.setTargetAmount(new BigDecimal("3000"));
            bikeGoal.setCurrentAmount(new BigDecimal("800"));
            bikeGoal.setTargetDate(LocalDate.now().plusMonths(4));
            bikeGoal.setCategory(GoalCategory.PERSONAL);
            bikeGoal.setPriority(Priority.MEDIUM);
            bikeGoal.setStatus(GoalStatus.ACTIVE);
            goalService.createGoal(bikeGoal);
        }
    }

    private VBox createEnhancedDashboardView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            InputStream fxmlStream = getClass().getResourceAsStream("/com/budget/enhanced-dashboard.fxml");
            VBox dashboardView = loader.load(fxmlStream);

            EnhancedDashboardController controller = loader.getController();
            controller.setServices(dashboardService, reportService);

            return dashboardView;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox(new Label("Błąd ładowania dashboard"));
        }
    }

    private VBox createHabitsView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            InputStream fxmlStream = getClass().getResourceAsStream("/com/budget/habits.fxml");
            VBox habitsView = (VBox) loader.load(fxmlStream);

            HabitsController controller = loader.getController();
            controller.setHabitService(habitService);

            return habitsView;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox(new Label("Błąd ładowania nawyków"));
        }
    }

    private VBox createGoalsView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            InputStream fxmlStream = getClass().getResourceAsStream("/com/budget/goals.fxml");
            VBox goalsView = (VBox) loader.load(fxmlStream);

            GoalsController controller = loader.getController();
            controller.setGoalService(goalService);

            return goalsView;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox(new Label("Błąd ładowania celów"));
        }
    }

    private VBox createReportsView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            InputStream fxmlStream = getClass().getResourceAsStream("/com/budget/reports.fxml");
            VBox reportsView = (VBox) loader.load(fxmlStream);

            ReportsController controller = loader.getController();
            controller.setReportService(reportService);

            return reportsView;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox(new Label("Błąd ładowania raportów"));
        }
    }

    private VBox createFinanceView() {
        // Tu możesz dodać istniejący widok finansowy
        VBox financeView = new VBox();
        financeView.setSpacing(10);
        financeView.setPadding(new javafx.geometry.Insets(10));
        financeView.getChildren().add(new Label("Widok finansowy - do implementacji"));
        return financeView;
    }

    private VBox createTasksView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            // Zakładam, że plik tasks.fxml znajduje się w tym samym katalogu co inne widoki
            InputStream fxmlStream = getClass().getResourceAsStream("/com/budget/views/tasks.fxml");

            // Jeśli plik jest w głównym katalogu resources/com/budget/, użyj:
            // InputStream fxmlStream = getClass().getResourceAsStream("/com/budget/tasks.fxml");

            if (fxmlStream == null) {
                return new VBox(new javafx.scene.control.Label("⚠️ Nie znaleziono pliku tasks.fxml"));
            }

            VBox tasksView = (VBox) loader.load(fxmlStream);

            // Jeśli Twój TaskController wymaga wstrzyknięcia serwisu (zależy od implementacji):
            // TaskController controller = loader.getController();
            // controller.setTaskService(taskService);

            return tasksView;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox(new javafx.scene.control.Label("❌ Błąd ładowania widoku zadań: " + e.getMessage()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}