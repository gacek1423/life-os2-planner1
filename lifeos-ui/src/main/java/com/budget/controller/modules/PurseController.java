package com.budget.controller.modules;

import com.budget.dao.PurseDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.modules.finance.domain.Purse;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PurseController {
    private final PurseDAO purseDAO = new PurseDAO();
    private final HBox container; // Kontener na kokpicie

    public PurseController(HBox container) {
        this.container = container;
    }

    public void refreshPurses() {
        if (container == null) return;
        AsyncRunner.run(purseDAO::getAllPurses, purses -> {
            container.getChildren().clear();
            for (Purse p : purses) {
                container.getChildren().add(createCard(p));
            }
        });
    }

    private VBox createCard(Purse p) {
        VBox card = new VBox();
        card.setPrefSize(220, 130); // Zwiększ nieco wysokość
        card.setMinSize(220, 130);

        String bgStyle = switch (p.getType()) {
            case LOCKED -> "-fx-background-color: linear-gradient(to bottom right, #8a3c3c, #5c2b2b);";
            case EMERGENCY -> "-fx-background-color: linear-gradient(to bottom right, #fcc419, #d9a507);";
            default -> "-fx-background-color: linear-gradient(to bottom right, #2a3d4d, #1a2630);";
        };

        card.setStyle(bgStyle + "-fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        // Header
        HBox top = new HBox(); top.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(p.getName().toUpperCase());
        name.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold; -fx-font-size: 10px;");
        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Label icon = new Label(p.isLocked() ? "🔒" : "💳");
        icon.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.5);");
        top.getChildren().addAll(name, r, icon);

        // Body
        VBox center = new VBox(5); center.setAlignment(Pos.CENTER_LEFT); VBox.setVgrow(center, Priority.ALWAYS);
        Label amount = new Label(String.format("%.2f PLN", p.getAvailable()));
        amount.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        ProgressBar pb = new ProgressBar(p.getProgress()); pb.setPrefWidth(190); pb.setPrefHeight(4);
        pb.setStyle("-fx-accent: rgba(255,255,255,0.8); -fx-control-inner-background: rgba(0,0,0,0.2); -fx-text-box-border: transparent;");
        center.getChildren().addAll(amount, pb);

        // Footer
        Label typeLabel = new Label(p.getType().name());
        typeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 9px;");
        card.getChildren().addAll(top, center, typeLabel);

        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }
}