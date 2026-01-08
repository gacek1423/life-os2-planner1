package com.budget.modules.reports;

import com.budget.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    
    // Generowanie raportów
    Report generateReport(ReportType type, ReportPeriod period);
    Report generateReport(ReportType type, LocalDate startDate, LocalDate endDate);
    Report generateCustomReport(Map<String, Object> parameters);
    
    // Zarządzanie raportami
    Report saveReport(Report report);
    Report getReportById(Long id);
    List<Report> getAllReports();
    List<Report> getReportsByType(ReportType type);
    void deleteReport(Long id);
    
    // Raporty specjalistyczne
    Report generateFinancialReport(ReportPeriod period);
    Report generateHabitReport(ReportPeriod period, Long habitId);
    Report generateGoalReport(Long goalId);
    Report generateTaskProductivityReport(ReportPeriod period);
    Report generateDashboardReport();
    
    // Eksport raportów
    String exportToPDF(Report report);
    String exportToCSV(Report report);
    String exportToExcel(Report report);
    String exportToHTML(Report report);
    
    // Harmonogramowanie
    void scheduleReport(Report report, String cronExpression);
    void cancelScheduledReport(Long reportId);
    List<Report> getScheduledReports();
    
    // Szablony
    Report createTemplate(ReportType type, String name, Map<String, Object> configuration);
    List<Report> getTemplates();
    Report generateFromTemplate(Long templateId, ReportPeriod period);
}