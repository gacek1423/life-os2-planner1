package com.budget.model.goals;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private boolean isDone;
    private LocalDate dueDate;
    private String priority; // np. "HIGH", "MEDIUM", "LOW"

    public Task(int id, String title, boolean isDone, LocalDate dueDate, String priority) {
        this.id = id;
        this.title = title;
        this.isDone = isDone;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    // Gettery i Settery
    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
    public LocalDate getDueDate() { return dueDate; }
    public String getPriority() { return priority; }

    @Override
    public String toString() { return title; }
}