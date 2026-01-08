package com.budget.model.goals;

import com.budget.modules.finance.domain.PurseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Purse {
    private int id;
    private String name;
    private PurseType type;
    private double allocatedAmount;
    private double spentAmount;
    private boolean isLocked;
    private double bufferAllowance;

    // Obliczanie dostępnych środków
    public double getAvailable() {
        return allocatedAmount - spentAmount;
    }

    // Czy jesteśmy "pod kreską"?
    public double getProgress() {
        return (allocatedAmount > 0) ? spentAmount / allocatedAmount : 0;
    }
}