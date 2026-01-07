package com.budget.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {
    private static final Map<Class<? extends DomainEvent>, List<Consumer<DomainEvent>>> subscribers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <E extends DomainEvent> void subscribe(Class<E> eventType, Consumer<E> action) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add((Consumer<DomainEvent>) action);
    }

    public static void publish(DomainEvent event) {
        List<Consumer<DomainEvent>> actions = subscribers.get(event.getClass());
        if (actions != null) {
            actions.forEach(action -> action.accept(event));
        }
    }
}