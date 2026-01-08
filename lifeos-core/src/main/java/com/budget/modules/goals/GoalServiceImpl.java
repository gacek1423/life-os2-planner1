package com.budget.modules.goals;

import com.budget.model.Goal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalServiceImpl implements GoalService {
    
    private final Map<Long, Goal> goals = new HashMap<>();
    private long goalIdCounter = 1;
    
    @Override
    public Goal createGoal(Goal goal) {
        goal.setId(goalIdCounter++);
        goals.put(goal.getId(), goal);
        return goal;
    }
    
    @Override
    public Goal getGoalById(Long id) {
        return goals.get(id);
    }
    
    @Override
    public List<Goal> getAllGoals() {
        return new ArrayList<>(goals.values());
    }
    
    @Override
    public Goal updateGoal(Goal goal) {
        if (goals.containsKey(goal.getId())) {
            goals.put(goal.getId(), goal);
            return goal;
        }
        return null;
    }
    
    @Override
    public void deleteGoal(Long id) {
        goals.remove(id);
    }
}