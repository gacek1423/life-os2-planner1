package com.budget.modules.tasks;

import com.budget.model.Task;
import java.util.List;

public interface TaskService {
    Task createTask(Task task);
    Task getTaskById(Long id);
    List<Task> getAllTasks();
    Task updateTask(Task task);
    void deleteTask(Long id);
}