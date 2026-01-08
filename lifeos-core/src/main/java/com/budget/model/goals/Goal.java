package com.budget.model.goals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goal {
    private int id;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private LocalDate deadline;

    // Metoda pomocnicza do obliczania postępu (0.0 do 1.0 dla ProgressBar)
    public double getProgress() {
        if (targetAmount == 0) return 0;
        return currentAmount / targetAmount;
    }
}