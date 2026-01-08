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
public class GoalMilestone {
    private Long id;
    private Long goalId;
    private String name;
    private BigDecimal amount;
    private boolean achieved;
    private LocalDate achievedDate;
    private String reward;
}