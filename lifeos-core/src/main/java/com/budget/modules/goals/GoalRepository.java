package com.budget.modules.goals;

import com.budget.model.Goal;
import java.util.List;

// To jest kontrakt. Core mówi: "Potrzebuję kogoś, kto potrafi zapisać cel".
public interface GoalRepository {
    List<Goal> getAllGoals();
    void updateCurrentAmount(int id, double newAmount); // Metoda, której nam brakowało
    void addGoal(Goal g);
}