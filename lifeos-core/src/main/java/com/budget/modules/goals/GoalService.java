package com.budget.modules.goals;

import com.budget.infrastructure.EventBus;
import com.budget.model.Goal;
import com.budget.modules.finance.events.TransactionAddedEvent;
import java.util.List;

public class GoalService {

    // Zmieniamy typ pola na Interfejs!
    private final GoalRepository repository;

    // Wstrzykujemy implementację przez konstruktor
    public GoalService(GoalRepository repository) {
        this.repository = repository;
        EventBus.subscribe(TransactionAddedEvent.class, this::onTransactionAdded);
    }

    private void onTransactionAdded(TransactionAddedEvent event) {
        if (!"PRZYCHÓD".equals(event.transaction().getType())) {
            return;
        }

        // Używamy metod z interfejsu
        List<Goal> goals = repository.getAllGoals();

        goals.stream()
                .filter(g -> g.getCurrentAmount() < g.getTargetAmount())
                .findFirst()
                .ifPresent(targetGoal -> {
                    double autoSaveAmount = event.transaction().getAmount() * 0.10;
                    double newAmount = targetGoal.getCurrentAmount() + autoSaveAmount;

                    System.out.println("Auto-save: " + autoSaveAmount + " PLN na cel: " + targetGoal.getName());

                    // Wywołujemy metodę z interfejsu
                    repository.updateCurrentAmount(targetGoal.getId(), newAmount);
                });
    }
}