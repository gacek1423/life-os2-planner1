package com.budget.model.goals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private int id;
    private String type; // "INCOME" lub "EXPENSE"
    private String category;
    private double amount;
    private LocalDate date;
    private String description;
}