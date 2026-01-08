package com.budget.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class SplashController {
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    public void updateProgress(String message, double progress) {
        // Ponieważ ta metoda będzie wołana z innego wątku, używamy Platform.runLater
        javafx.application.Platform.runLater(() -> {
            statusLabel.setText(message);
            progressBar.setProgress(progress);
        });
    }
}