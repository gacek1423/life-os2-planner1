package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    private String title;
    private String description;
    private ReportType type;
    private ReportPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private Map<String, Object> data;
    private List<ReportSection> sections;
    private String filePath;
    private ReportFormat format;
    private boolean scheduled;
    private String scheduleExpression;
}