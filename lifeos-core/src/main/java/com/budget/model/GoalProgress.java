package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgress {
    private Long id;
    private Long goalId;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private LocalDate date;
    private String notes;
}