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
public class ReportTable {
    private String id;
    private String title;
    private List<String> headers;
    private List<Map<String, Object>> rows;
    private String[] columnTypes; // "number", "text", "currency", "date", "percentage"
    private boolean sortable;
    private boolean filterable;
    private int pageSize;
}