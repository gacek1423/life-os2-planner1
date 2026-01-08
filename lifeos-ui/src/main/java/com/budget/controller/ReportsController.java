package com.budget.controller;

import com.budget.model.*;
import com.budget.modules.reports.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportsController {
    
    @FXML private ComboBox<ReportType> reportTypeComboBox;
    @FXML private ComboBox<ReportPeriod> periodComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<ReportFormat> formatComboBox;
    @FXML private TextField reportTitleField;
    @FXML private TextArea reportDescriptionArea;
    
    @FXML private Button generateButton;
    @FXML private Button saveTemplateButton;
    @FXML private Button exportButton;
    @FXML private Button scheduleButton;
    @FXML private Button refreshButton;
    
    @FXML private TableView<Report> reportsTable;
    @FXML private TableColumn<Report, String> titleColumn;
    @FXML private TableColumn<Report, ReportType> typeColumn;
    @FXML private TableColumn<Report, LocalDate> dateColumn;
    @FXML private TableColumn<Report, ReportFormat> formatColumn;
    
    @FXML private VBox reportPreviewContainer;
    @FXML private WebView reportWebView;
    @FXML private Label statusLabel;
    
    private ReportService reportService;
    private Report selectedReport;
    
    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
        initialize();
    }
    
    private void initialize() {
        setupComboBoxes();
        setupTableColumns();
        loadReports();
        setupEventHandlers();
    }
    
    private void setupComboBoxes() {
        reportTypeComboBox.getItems().setAll(ReportType.values());
        periodComboBox.getItems().setAll(ReportPeriod.values());
        formatComboBox.getItems().setAll(ReportFormat.values());
        
        // Konwertery dla wyświetlania polskich nazw
        setupComboBoxConverters();
        
        // Ustaw wartości domyślne
        reportTypeComboBox.setValue(ReportType.DASHBOARD_OVERVIEW);
        periodComboBox.setValue(ReportPeriod.THIS_MONTH);
        formatComboBox.setValue(ReportFormat.HTML);
        
        // Ustaw dzisiejsze daty
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
    }
    
    private void setupComboBoxConverters() {
        reportTypeComboBox.setConverter(new javafx.util.StringConverter<ReportType>() {
            @Override
            public String toString(ReportType type) {
                return type != null ? type.getDisplayName() : "";
            }
            @Override
            public ReportType fromString(String string) { return null; }
        });
        
        periodComboBox.setConverter(new javafx.util.StringConverter<ReportPeriod>() {
            @Override
            public String toString(ReportPeriod period) {
                return period != null ? period.getDisplayName() : "";
            }
            @Override
            public ReportPeriod fromString(String string) { return null; }
        });
        
        formatComboBox.setConverter(new javafx.util.StringConverter<ReportFormat>() {
            @Override
            public String toString(ReportFormat format) {
                return format != null ? format.getDisplayName() : "";
            }
            @Override
            public ReportFormat fromString(String string) { return null; }
        });
    }
    
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        typeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getType()));
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getGeneratedAt().toLocalDate()));
        formatColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getFormat()));
    }
    
    private void loadReports() {
        reportsTable.getItems().clear();
        List<Report> reports = reportService.getAllReports();
        reportsTable.getItems().addAll(reports);
    }
    
    private void setupEventHandlers() {
        reportsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedReport = newValue;
                if (newValue != null) {
                    displayReport(newValue);
                }
            }
        );
        
        // Obsługa zmian w comboboxach
        reportTypeComboBox.setOnAction(event -> updateReportDescription());
        periodComboBox.setOnAction(event -> updateDatePickers());
    }
    
    private void updateReportDescription() {
        ReportType type = reportTypeComboBox.getValue();
        if (type != null) {
            String description = getDefaultDescription(type);
            reportDescriptionArea.setText(description);
        }
    }
    
    private String getDefaultDescription(ReportType type) {
        switch (type) {
            case FINANCIAL_SUMMARY:
                return "Podsumowanie sytuacji finansowej z podziałem na kategorie";
            case HABIT_ANALYSIS:
                return "Analiza realizacji nawyków i utrzymywania serii";
            case GOAL_PROGRESS:
                return "Postęp w realizacji celów życiowych";
            case TASK_PRODUCTIVITY:
                return "Analiza produktywności na podstawie zadań";
            case DASHBOARD_OVERVIEW:
                return "Kompletny przegląd wszystkich obszarów życia";
            case WEEKLY_SUMMARY:
                return "Tygodniowe podsumowanie aktywności";
            case MONTHLY_SUMMARY:
                return "Miesięczny raport podsumowujący";
            default:
                return "Raport niestandardowy";
        }
    }
    
    private void updateDatePickers() {
        ReportPeriod period = periodComboBox.getValue();
        if (period != null && period != ReportPeriod.CUSTOM) {
            LocalDate[] dates = calculatePeriodDates(period);
            startDatePicker.setValue(dates[0]);
            endDatePicker.setValue(dates[1]);
        }
    }
    
    @FXML
    private void handleGenerateReport() {
        try {
            ReportType type = reportTypeComboBox.getValue();
            ReportPeriod period = periodComboBox.getValue();
            ReportFormat format = formatComboBox.getValue();
            
            if (type == null || period == null || format == null) {
                showAlert("Błąd", "Wypełnij wszystkie wymagane pola!", Alert.AlertType.ERROR);
                return;
            }
            
            Report report;
            if (period == ReportPeriod.CUSTOM) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                if (startDate == null || endDate == null) {
                    showAlert("Błąd", "Wybierz datę początkową i końcową!", Alert.AlertType.ERROR);
                    return;
                }
                report = reportService.generateReport(type, startDate, endDate);
            } else {
                report = reportService.generateReport(type, period);
            }
            
            // Ustaw tytuł i opis
            String title = reportTitleField.getText();
            if (title != null && !title.trim().isEmpty()) {
                report.setTitle(title);
            }
            
            String description = reportDescriptionArea.getText();
            if (description != null && !description.trim().isEmpty()) {
                report.setDescription(description);
            }
            
            report.setFormat(format);
            
            // Zapisz raport
            Report savedReport = reportService.saveReport(report);
            
            // Odśwież tabelę
            loadReports();
            
            // Wyświetl raport
            displayReport(savedReport);
            
            // Zaznacz nowo wygenerowany raport
            reportsTable.getSelectionModel().select(savedReport);
            
            statusLabel.setText("Raport wygenerowany pomyślnie!");
            
        } catch (Exception e) {
            showAlert("Błąd generowania", "Nie udało się wygenerować raportu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleSaveTemplate() {
        if (selectedReport == null) {
            showAlert("Błąd", "Najpierw wybierz lub wygeneruj raport!", Alert.AlertType.ERROR);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Zapisz szablon");
        dialog.setHeaderText("Podaj nazwę szablonu:");
        dialog.setContentText("Nazwa:");
        
        dialog.showAndWait().ifPresent(templateName -> {
            Map<String, Object> configuration = new HashMap<>();
            configuration.put("type", selectedReport.getType());
            configuration.put("period", selectedReport.getPeriod());
            configuration.put("format", selectedReport.getFormat());
            
            Report template = reportService.createTemplate(
                selectedReport.getType(), 
                templateName, 
                configuration
            );
            
            statusLabel.setText("Szablon zapisany: " + template.getTitle());
        });
    }
    
    @FXML
    private void handleExportReport() {
        if (selectedReport == null) {
            showAlert("Błąd", "Najpierw wybierz raport!", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            String filePath;
            switch (selectedReport.getFormat()) {
                case PDF:
                    filePath = reportService.exportToPDF(selectedReport);
                    break;
                case CSV:
                    filePath = reportService.exportToCSV(selectedReport);
                    break;
                case EXCEL:
                    filePath = reportService.exportToExcel(selectedReport);
                    break;
                case HTML:
                default:
                    filePath = reportService.exportToHTML(selectedReport);
                    break;
            }
            
            statusLabel.setText("Raport wyeksportowany: " + filePath);
            
        } catch (Exception e) {
            showAlert("Błąd eksportu", "Nie udało się wyeksportować raportu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleScheduleReport() {
        if (selectedReport == null) {
            showAlert("Błąd", "Najpierw wybierz raport!", Alert.AlertType.ERROR);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog("0 0 1 * *");
        dialog.setTitle("Harmonogram raportu");
        dialog.setHeaderText("Podaj wyrażenie cron (np. 0 0 1 * * dla co miesiąca):");
        dialog.setContentText("Cron expression:");
        
        dialog.showAndWait().ifPresent(cronExpression -> {
            try {
                reportService.scheduleReport(selectedReport, cronExpression);
                statusLabel.setText("Raport zaplanowany: " + cronExpression);
            } catch (Exception e) {
                showAlert("Błąd harmonogramowania", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadReports();
        statusLabel.setText("Lista raportów odświeżona");
    }
    
    private void displayReport(Report report) {
        try {
            String htmlContent = generateHTMLReport(report);
            reportWebView.getEngine().loadContent(htmlContent);
        } catch (Exception e) {
            statusLabel.setText("Błąd wyświetlania raportu: " + e.getMessage());
        }
    }
    
    private String generateHTMLReport(Report report) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        html.append(".report-container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".report-title { color: #667eea; font-size: 24px; margin-bottom: 10px; }");
        html.append(".report-description { color: #666; margin-bottom: 30px; }");
        html.append(".section { margin-bottom: 30px; }");
        html.append(".section-title { color: #333; font-size: 18px; margin-bottom: 15px; border-bottom: 2px solid #667eea; padding-bottom: 5px; }");
        html.append(".metric { display: inline-block; margin: 10px 20px 10px 0; }");
        html.append(".metric-label { font-size: 12px; color: #666; }");
        html.append(".metric-value { font-size: 24px; font-weight: bold; color: #667eea; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("th { background-color: #667eea; color: white; }");
        html.append("tr:hover { background-color: #f5f5f5; }");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<div class='report-container'>");
        html.append("<h1 class='report-title'>").append(report.getTitle()).append("</h1>");
        html.append("<p class='report-description'>").append(report.getDescription()).append("</p>");
        
        if (report.getSections() != null) {
            for (ReportSection section : report.getSections()) {
                html.append("<div class='section'>");
                html.append("<h2 class='section-title'>").append(section.getTitle()).append("</h2>");
                
                if (section.getData() != null) {
                    for (Map.Entry<String, Object> entry : section.getData().entrySet()) {
                        html.append("<div class='metric'>");
                        html.append("<div class='metric-label'>").append(entry.getKey()).append("</div>");
                        html.append("<div class='metric-value'>").append(entry.getValue()).append("</div>");
                        html.append("</div>");
                    }
                }
                
                html.append("</div>");
            }
        }
        
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private LocalDate[] calculatePeriodDates(ReportPeriod period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (period) {
            case TODAY:
                startDate = endDate;
                break;
            case THIS_WEEK:
                startDate = endDate.minusDays(endDate.getDayOfWeek().getValue() - 1);
                break;
            case THIS_MONTH:
                startDate = endDate.withDayOfMonth(1);
                break;
            case THIS_QUARTER:
                int month = endDate.getMonthValue();
                int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
                startDate = endDate.withMonth(quarterStartMonth).withDayOfMonth(1);
                break;
            case THIS_YEAR:
                startDate = endDate.withDayOfYear(1);
                break;
            case LAST_7_DAYS:
                startDate = endDate.minusDays(7);
                break;
            case LAST_30_DAYS:
                startDate = endDate.minusDays(30);
                break;
            case LAST_90_DAYS:
                startDate = endDate.minusDays(90);
                break;
            default:
                startDate = endDate.minusDays(30);
        }
        
        return new LocalDate[]{startDate, endDate};
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}