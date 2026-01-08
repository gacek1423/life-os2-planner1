package com.budget.controller;

import com.budget.model.Habit;
import com.budget.model.HabitCategory;
import com.budget.model.HabitFrequency;
import com.budget.model.HabitRecord;
import com.budget.modules.habits.HabitService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class HabitsController {
    
    @FXML private TableView<Habit> habitsTable;
    @FXML private TableColumn<Habit, String> nameColumn;
    @FXML private TableColumn<Habit, HabitCategory> categoryColumn;
    @FXML private TableColumn<Habit, Integer> streakColumn;
    @FXML private TableColumn<Habit, Double> completionRateColumn;
    @FXML private TableColumn<Habit, Boolean> activeColumn;
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<HabitCategory> categoryComboBox;
    @FXML private ComboBox<HabitFrequency> frequencyComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Spinner<Integer> targetStreakSpinner;
    @FXML private CheckBox activeCheckBox;
    
    @FXML private ListView<String> todayHabitsList;
    @FXML private Button completeHabitButton;
    @FXML private Button uncompleteHabitButton;
    
    @FXML private LineChart<String, Number> streakChart;
    @FXML private NumberAxis yAxis;
    
    @FXML private Label currentStreakLabel;
    @FXML private Label bestStreakLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label totalRecordsLabel;
    
    private HabitService habitService;
    private Habit selectedHabit;
    
    public void setHabitService(HabitService habitService) {
        this.habitService = habitService;
        initialize();
    }
    
    private void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupSpinners();
        loadHabits();
        loadTodayHabits();
        setupEventHandlers();
    }
    
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        categoryColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCategory()));
        streakColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCurrentStreak()).asObject());
        completionRateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCompletionRate()).asObject());
        activeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isActive()).asObject());
    }
    
    private void setupComboBoxes() {
        categoryComboBox.getItems().setAll(HabitCategory.values());
        frequencyComboBox.getItems().setAll(HabitFrequency.values());
        
        categoryComboBox.setConverter(new javafx.util.StringConverter<HabitCategory>() {
            @Override
            public String toString(HabitCategory category) {
                return category != null ? category.getDisplayName() : "";
            }
            
            @Override
            public HabitCategory fromString(String string) {
                return HabitCategory.valueOf(string);
            }
        });
        
        frequencyComboBox.setConverter(new javafx.util.StringConverter<HabitFrequency>() {
            @Override
            public String toString(HabitFrequency frequency) {
                return frequency != null ? frequency.getDisplayName() : "";
            }
            
            @Override
            public HabitFrequency fromString(String string) {
                return HabitFrequency.valueOf(string);
            }
        });
    }
    
    private void setupSpinners() {
        targetStreakSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));
    }
    
    private void loadHabits() {
        habitsTable.getItems().clear();
        List<Habit> habits = habitService.getAllHabits();
        habitsTable.getItems().addAll(habits);
    }
    
    private void loadTodayHabits() {
        todayHabitsList.getItems().clear();
        List<Habit> todayHabits = habitService.getHabitsForToday();
        
        for (Habit habit : todayHabits) {
            HabitRecord todayRecord = habitService.getRecordForHabitAndDate(habit.getId(), LocalDate.now());
            String status = (todayRecord != null && todayRecord.isCompleted()) ? "✓ " : "☐ ";
            todayHabitsList.getItems().add(status + habit.getName());
        }
    }
    
    private void setupEventHandlers() {
        habitsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedHabit = newValue;
                showHabitDetails(newValue);
            }
        );
    }
    
    private void showHabitDetails(Habit habit) {
        if (habit == null) return;
        
        nameField.setText(habit.getName());
        descriptionField.setText(habit.getDescription());
        categoryComboBox.setValue(habit.getCategory());
        frequencyComboBox.setValue(habit.getFrequency());
        startDatePicker.setValue(habit.getStartDate());
        endDatePicker.setValue(habit.getEndDate());
        targetStreakSpinner.getValueFactory().setValue(habit.getTargetStreak());
        activeCheckBox.setSelected(habit.isActive());
        
        updateHabitStats(habit);
        createStreakChart(habit);
    }
    
    private void updateHabitStats(Habit habit) {
        currentStreakLabel.setText(String.valueOf(habit.getCurrentStreak()));
        bestStreakLabel.setText(String.valueOf(habitService.getBestStreak(habit.getId())));
        completionRateLabel.setText(String.format("%.1f%%", habit.getCompletionRate()));
        totalRecordsLabel.setText(String.valueOf(habit.getRecords() != null ? habit.getRecords().size() : 0));
    }
    
    private void createStreakChart(Habit habit) {
        streakChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Streak History");
        
        // Uproszczona wersja - pokazuje ostatnie 30 dni
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        
        Map<LocalDate, Boolean> completionCalendar = habitService.getCompletionCalendar(
            habit.getId(), startDate, endDate);
        
        int currentStreak = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Boolean completed = completionCalendar.getOrDefault(date, false);
            if (completed) {
                currentStreak++;
            } else {
                currentStreak = 0;
            }
            
            series.getData().add(new XYChart.Data<>(
                date.toString(), 
                currentStreak
            ));
        }
        
        streakChart.getData().add(series);
    }
    
    @FXML
    private void handleAddHabit() {
        Habit habit = new Habit();
        habit.setName(nameField.getText());
        habit.setDescription(descriptionField.getText());
        habit.setCategory(categoryComboBox.getValue());
        habit.setFrequency(frequencyComboBox.getValue());
        habit.setStartDate(startDatePicker.getValue());
        habit.setEndDate(endDatePicker.getValue());
        habit.setTargetStreak(targetStreakSpinner.getValue());
        habit.setActive(activeCheckBox.isSelected());
        
        habitService.createHabit(habit);
        loadHabits();
        loadTodayHabits();
        clearForm();
    }
    
    @FXML
    private void handleUpdateHabit() {
        if (selectedHabit == null) return;
        
        selectedHabit.setName(nameField.getText());
        selectedHabit.setDescription(descriptionField.getText());
        selectedHabit.setCategory(categoryComboBox.getValue());
        selectedHabit.setFrequency(frequencyComboBox.getValue());
        selectedHabit.setStartDate(startDatePicker.getValue());
        selectedHabit.setEndDate(endDatePicker.getValue());
        selectedHabit.setTargetStreak(targetStreakSpinner.getValue());
        selectedHabit.setActive(activeCheckBox.isSelected());
        
        habitService.updateHabit(selectedHabit);
        loadHabits();
        loadTodayHabits();
    }
    
    @FXML
    private void handleDeleteHabit() {
        if (selectedHabit == null) return;
        
        habitService.deleteHabit(selectedHabit.getId());
        loadHabits();
        loadTodayHabits();
        clearForm();
    }
    
    @FXML
    private void handleCompleteHabit() {
        String selectedItem = todayHabitsList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        
        // Znajdź habit po nazwie
        String habitName = selectedItem.replace("✓ ", "").replace("☐ ", "");
        List<Habit> habits = habitService.getAllHabits();
        Habit habitToComplete = habits.stream()
            .filter(h -> h.getName().equals(habitName))
            .findFirst()
            .orElse(null);
            
        if (habitToComplete != null) {
            habitService.completeHabitForToday(habitToComplete.getId(), "", 3);
            loadTodayHabits();
        }
    }
    
    @FXML
    private void handleUncompleteHabit() {
        String selectedItem = todayHabitsList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        
        String habitName = selectedItem.replace("✓ ", "").replace("☐ ", "");
        List<Habit> habits = habitService.getAllHabits();
        Habit habitToUncomplete = habits.stream()
            .filter(h -> h.getName().equals(habitName))
            .findFirst()
            .orElse(null);
            
        if (habitToUncomplete != null) {
            habitService.uncompleteHabitForToday(habitToUncomplete.getId());
            loadTodayHabits();
        }
    }
    
    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        categoryComboBox.setValue(null);
        frequencyComboBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        targetStreakSpinner.getValueFactory().setValue(30);
        activeCheckBox.setSelected(true);
    }
    
    @FXML
    private void handleRefresh() {
        loadHabits();
        loadTodayHabits();
        if (selectedHabit != null) {
            showHabitDetails(selectedHabit);
        }
    }
}