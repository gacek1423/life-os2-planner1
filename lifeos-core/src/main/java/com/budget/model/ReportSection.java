package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSection {
    private String title;
    private String type;
    private String description;
    private Map<String, Object> data;
    private List<ReportChart> charts;
    private List<ReportTable> tables;
    private List<String> insights;
    private int order;
}