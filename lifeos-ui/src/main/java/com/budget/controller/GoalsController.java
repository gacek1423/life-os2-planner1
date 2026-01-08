package com.budget.controller;

import com.budget.model.*;
import com.budget.modules.goals.GoalService;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GoalsController {
    
    @FXML private TableView<Goal> goalsTable;
    @FXML private TableColumn<Goal, String> nameColumn;
    @FXML private TableColumn<Goal, GoalCategory> categoryColumn;
    @FXML private TableColumn<Goal, BigDecimal> targetAmountColumn;
    @FXML private TableColumn<Goal, BigDecimal> currentAmountColumn;
    @FXML private TableColumn<Goal, LocalDate> targetDateColumn;
    @FXML private TableColumn<Goal, GoalStatus> statusColumn;
    @FXML private TableColumn<Goal, Priority> priorityColumn;
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField targetAmountField;
    @FXML private TextField currentAmountField;
    @FXML private DatePicker targetDatePicker;
    @FXML private ComboBox<GoalCategory> categoryComboBox;
    @FXML private ComboBox<Priority> priorityComboBox;
    @FXML private ComboBox<GoalStatus> statusComboBox;
    
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label daysRemainingLabel;
    @FXML private Label remainingAmountLabel;
    
    @FXML private ListView<String> milestonesList;
    @FXML private TextField milestoneNameField;
    @FXML private TextField milestoneAmountField;
    @FXML private Button addMilestoneButton;
    
    @FXML private PieChart goalsPieChart;
    @FXML private VBox progressChartContainer;
    
    private GoalService goalService;
    private Goal selectedGoal;
    
    public void setGoalService(GoalService goalService) {
        this.goalService = goalService;
        initialize();
    }
    
    private void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadGoals();
        setupEventHandlers();
    }
    
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        categoryColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCategory()));
        targetAmountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTargetAmount()));
        currentAmountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCurrentAmount()));
        targetDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTargetDate()));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStatus()));
        priorityColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPriority()));
    }
    
    private void setupComboBoxes() {
        categoryComboBox.getItems().setAll(GoalCategory.values());
        priorityComboBox.getItems().setAll(Priority.values());
        statusComboBox.getItems().setAll(GoalStatus.values());
        
        // Konwertery dla wyświetlania polskich nazw
        setupComboBoxConverters();
    }
    
    private void setupComboBoxConverters() {
        categoryComboBox.setConverter(new javafx.util.StringConverter<GoalCategory>() {
            @Override
            public String toString(GoalCategory category) {
                return category != null ? category.getDisplayName() : "";
            }
            @Override
            public GoalCategory fromString(String string) { return null; }
        });
        
        priorityComboBox.setConverter(new javafx.util.StringConverter<Priority>() {
            @Override
            public String toString(Priority priority) {
                return priority != null ? priority.getDisplayName() : "";
            }
            @Override
            public Priority fromString(String string) { return null; }
        });
        
        statusComboBox.setConverter(new javafx.util.StringConverter<GoalStatus>() {
            @Override
            public String toString(GoalStatus status) {
                return status != null ? status.getDisplayName() : "";
            }
            @Override
            public GoalStatus fromString(String string) { return null; }
        });
    }
    
    private void loadGoals() {
        goalsTable.getItems().clear();
        List<Goal> goals = goalService.getAllGoals();
        goalsTable.getItems().addAll(goals);
        
        updateGoalsChart();
    }
    
    private void setupEventHandlers() {
        goalsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedGoal = newValue;
                showGoalDetails(newValue);
            }
        );
    }
    
    private void showGoalDetails(Goal goal) {
        if (goal == null) return;
        
        nameField.setText(goal.getName());
        descriptionField.setText(goal.getDescription());
        targetAmountField.setText(goal.getTargetAmount() != null ? goal.getTargetAmount().toString() : "");
        currentAmountField.setText(goal.getCurrentAmount() != null ? goal.getCurrentAmount().toString() : "0");
        targetDatePicker.setValue(goal.getTargetDate());
        categoryComboBox.setValue(goal.getCategory());
        priorityComboBox.setValue(goal.getPriority());
        statusComboBox.setValue(goal.getStatus());
        
        updateProgressDisplay(goal);
        loadMilestones(goal);
    }
    
    private void updateProgressDisplay(Goal goal) {
        double progress = goal.getProgressPercentage();
        progressBar.setProgress(progress / 100.0);
        progressLabel.setText(String.format("%.1f%%", progress));
        
        daysRemainingLabel.setText(String.valueOf(goal.getDaysRemaining()));
        remainingAmountLabel.setText(formatCurrency(goal.getRemainingAmount()));
        
        // Zmień kolor progress baru
        if (progress >= 100) {
            progressBar.setStyle("-fx-accent: #4CAF50;"); // Zielony
        } else if (progress >= 75) {
            progressBar.setStyle("-fx-accent: #8BC34A;"); // Jasnozielony
        } else if (progress >= 50) {
            progressBar.setStyle("-fx-accent: #FFC107;"); // Żółty
        } else if (progress >= 25) {
            progressBar.setStyle("-fx-accent: #FF9800;"); // Pomarańczowy
        } else {
            progressBar.setStyle("-fx-accent: #F44336;"); // Czerwony
        }
    }
    
    private void loadMilestones(Goal goal) {
        milestonesList.getItems().clear();
        if (goal.getMilestones() != null) {
            for (GoalMilestone milestone : goal.getMilestones()) {
                String status = milestone.isAchieved() ? "✓ " : "☐ ";
                String milestoneText = String.format("%s%s - %s", 
                    status, milestone.getName(), formatCurrency(milestone.getAmount()));
                milestonesList.getItems().add(milestoneText);
            }
        }
    }
    
    private void updateGoalsChart() {
        goalsPieChart.getData().clear();
        
        Map<GoalStatus, Long> goalsByStatus = goalsTable.getItems().stream()
            .collect(Collectors.groupingBy(Goal::getStatus, Collectors.counting()));
        
        for (Map.Entry<GoalStatus, Long> entry : goalsByStatus.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                entry.getKey().getDisplayName(), 
                entry.getValue()
            );
            goalsPieChart.getData().add(slice);
        }
    }
    
    @FXML
    private void handleAddGoal() {
        Goal goal = new Goal();
        goal.setName(nameField.getText());
        goal.setDescription(descriptionField.getText());
        goal.setTargetAmount(new BigDecimal(targetAmountField.getText()));
        goal.setCurrentAmount(new BigDecimal(currentAmountField.getText()));
        goal.setTargetDate(targetDatePicker.getValue());
        goal.setCategory(categoryComboBox.getValue());
        goal.setPriority(priorityComboBox.getValue());
        goal.setStatus(statusComboBox.getValue() != null ? statusComboBox.getValue() : GoalStatus.ACTIVE);
        
        goalService.createGoal(goal);
        loadGoals();
        clearForm();
    }
    
    @FXML
    private void handleUpdateGoal() {
        if (selectedGoal == null) return;
        
        selectedGoal.setName(nameField.getText());
        selectedGoal.setDescription(descriptionField.getText());
        selectedGoal.setTargetAmount(new BigDecimal(targetAmountField.getText()));
        selectedGoal.setCurrentAmount(new BigDecimal(currentAmountField.getText()));
        selectedGoal.setTargetDate(targetDatePicker.getValue());
        selectedGoal.setCategory(categoryComboBox.getValue());
        selectedGoal.setPriority(priorityComboBox.getValue());
        selectedGoal.setStatus(statusComboBox.getValue());
        
        goalService.updateGoal(selectedGoal);
        loadGoals();
    }
    
    @FXML
    private void handleDeleteGoal() {
        if (selectedGoal == null) return;
        
        goalService.deleteGoal(selectedGoal.getId());
        loadGoals();
        clearForm();
    }
    
    @FXML
    private void handleAddProgress() {
        if (selectedGoal == null) return;
        
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Dodaj postęp");
        dialog.setHeaderText("Dodaj kwotę do celu: " + selectedGoal.getName());
        dialog.setContentText("Kwota:");
        
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                selectedGoal.addProgress(amount);
                goalService.updateGoal(selectedGoal);
                showGoalDetails(selectedGoal);
                loadGoals();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Nieprawidłowa kwota!");
                alert.showAndWait();
            }
        });
    }
    
    @FXML
    private void handleAddMilestone() {
        if (selectedGoal == null) return;
        
        try {
            GoalMilestone milestone = new GoalMilestone();
            milestone.setName(milestoneNameField.getText());
            milestone.setAmount(new BigDecimal(milestoneAmountField.getText()));
            milestone.setAchieved(false);
            
            if (selectedGoal.getMilestones() == null) {
                selectedGoal.setMilestones(new ArrayList<>());
            }
            selectedGoal.getMilestones().add(milestone);
            
            goalService.updateGoal(selectedGoal);
            loadMilestones(selectedGoal);
            
            milestoneNameField.clear();
            milestoneAmountField.clear();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nieprawidłowa kwota!");
            alert.showAndWait();
        }
    }
    
    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        targetAmountField.clear();
        currentAmountField.clear();
        targetDatePicker.setValue(null);
        categoryComboBox.setValue(null);
        priorityComboBox.setValue(null);
        statusComboBox.setValue(null);
        progressBar.setProgress(0);
        progressLabel.setText("0%");
        milestonesList.getItems().clear();
    }
    
    private String formatCurrency(BigDecimal amount) {
        return String.format("%.2f zł", amount.doubleValue());
    }
    
    @FXML
    private void handleRefresh() {
        loadGoals();
        if (selectedGoal != null) {
            showGoalDetails(selectedGoal);
        }
    }
}