package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitRecord {
    private Long id;
    private Long habitId;
    private LocalDate date;
    private boolean completed;
    private String notes;
    private int difficulty; // 1-5 scale

}