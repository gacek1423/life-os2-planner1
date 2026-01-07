package com.budget.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private int id;
    private String title;
    private String priority; // "HIGH", "MEDIUM", "LOW"
    private boolean done;
    private LocalDate dueDate;
}