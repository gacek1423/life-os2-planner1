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
public class ReportChart {
    private String id;
    private String title;
    private String type; // "bar", "pie", "line", "doughnut"
    private String xAxisLabel;
    private String yAxisLabel;
    private List<Map<String, Object>> data;
    private Map<String, String> colors;
    private int width;
    private int height;
}