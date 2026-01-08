package com.budget.controller.modules;

import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.model.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class CalendarController {
    @FXML private GridPane calendarGrid;
    @FXML private Label calMonthLabel;

    private YearMonth currentYearMonth = YearMonth.now();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    public void initialize() {
        refreshCalendar();
    }

    @FXML public void prevMonth() { currentYearMonth = currentYearMonth.minusMonths(1); refreshCalendar(); }
    @FXML public void nextMonth() { currentYearMonth = currentYearMonth.plusMonths(1); refreshCalendar(); }

    public void refreshCalendar() {
        calMonthLabel.setText((currentYearMonth.getMonth().name() + " " + currentYearMonth.getYear()).toUpperCase());

        AsyncRunner.run(() -> transactionDAO.getTransactionsForMonth(currentYearMonth.getYear(), currentYearMonth.getMonthValue()),
                transactions -> {
                    calendarGrid.getChildren().clear();

                    // Nagłówki
                    String[] days = {"PON", "WT", "ŚR", "CZW", "PT", "SOB", "NDZ"};
                    for(int i=0; i<7; i++) {
                        Label l = new Label(days[i]);
                        l.getStyleClass().add("calendar-day-header");
                        calendarGrid.add(l, i, 0);
                    }

                    LocalDate firstOfMonth = currentYearMonth.atDay(1);
                    int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1;
                    int daysInMonth = currentYearMonth.lengthOfMonth();

                    int row = 1;
                    int col = dayOfWeek;

                    for (int day = 1; day <= daysInMonth; day++) {
                        LocalDate date = currentYearMonth.atDay(day);
                        VBox cell = new VBox(2);
                        cell.getStyleClass().add("calendar-day");
                        if (date.equals(LocalDate.now())) cell.getStyleClass().add("calendar-today");

                        Label dateLbl = new Label(String.valueOf(day));
                        dateLbl.getStyleClass().add("calendar-date-label");
                        cell.getChildren().add(dateLbl);

                        // Dodaj kropki/pigułki dla transakcji w tym dniu
                        long count = transactions.stream().filter(t -> t.getDate().equals(date)).count();
                        if (count > 0) {
                            Label dot = new Label(count + " transakcji");
                            dot.setStyle("-fx-font-size: 9px; -fx-text-fill: #4fa3c7;");
                            cell.getChildren().add(dot);
                        }

                        // Kliknięcie (dla uproszczenia: wysyłamy zdarzenie lub wywołujemy metodę w DashboardController)
                        // Tutaj w wersji modułowej najlepiej użyć EventBus do komunikacji z rodzicem

                        calendarGrid.add(cell, col, row);
                        col++;
                        if (col > 6) { col = 0; row++; }
                    }
                });
    }
}