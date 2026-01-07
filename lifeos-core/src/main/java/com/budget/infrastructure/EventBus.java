package com.budget.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {

    public enum EventType {
        TRANSACTION_ADDED,
        TASK_UPDATED,
        GOAL_ADDED,
        DATA_CLEARED,
        REFRESH_ALL
    }

    private static final Map<EventType, List<Consumer<Object>>> subscribers = new HashMap<>();

    public static void subscribe(EventType type, Consumer<Object> action) {
        subscribers.computeIfAbsent(type, k -> new ArrayList<>()).add(action);
    }

    public static void publish(EventType type, Object data) {
        if (subscribers.containsKey(type)) {
            subscribers.get(type).forEach(action -> action.accept(data));
        }
    }

    public static void publish(EventType type) {
        publish(type, null);
    }
}