package com.budget.modules.tasks;

import com.budget.model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskServiceImpl implements TaskService {
    
    private final Map<Long, Task> tasks = new HashMap<>();
    private long taskIdCounter = 1;
    
    @Override
    public Task createTask(Task task) {
        task.setId(taskIdCounter++);
        tasks.put(task.getId(), task);
        return task;
    }
    
    @Override
    public Task getTaskById(Long id) {
        return tasks.get(id);
    }
    
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
    
    @Override
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            return task;
        }
        return null;
    }
    
    @Override
    public void deleteTask(Long id) {
        tasks.remove(id);
    }
}