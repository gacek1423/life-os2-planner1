package com.budget.infrastructure;

import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncRunner {

    // Wykonaj coś w tle (zwraca wynik), a potem zaktualizuj UI
    public static <T> void run(Supplier<T> backgroundAction, Consumer<T> uiAction) {
        CompletableFuture.supplyAsync(backgroundAction)
                .thenAccept(result -> Platform.runLater(() -> uiAction.accept(result)))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    // Wykonaj coś w tle (bez wyniku, np. INSERT/DELETE), a potem zrób coś w UI
    public static void run(Runnable backgroundAction, Runnable uiAction) {
        CompletableFuture.runAsync(backgroundAction)
                .thenRun(() -> Platform.runLater(uiAction))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }
}