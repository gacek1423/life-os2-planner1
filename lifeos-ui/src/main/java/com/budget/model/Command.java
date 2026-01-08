package com.budget.model;

public record Command(String name, String description, Runnable action) {
    @Override
    public String toString() {
        return name; // To wyświetli się na liście
    }
}