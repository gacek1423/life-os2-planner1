package com.budget.modules.goals;

import com.budget.model.Goal;
import java.util.List;

public interface GoalService {
    Goal createGoal(Goal goal);
    Goal getGoalById(Long id);
    List<Goal> getAllGoals();
    Goal updateGoal(Goal goal);
    void deleteGoal(Long id);
}