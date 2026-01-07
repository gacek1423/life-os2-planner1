package com.budget.controller.managers;

import com.budget.dao.TaskDAO;
import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.model.Task;
import com.budget.model.Transaction;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CalendarManager {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    private final GridPane calendarGrid;
    private final Label monthLabel;
    private YearMonth currentYearMonth;

    // Callback do kontrolera głównego (żeby przełączyć widok)
    private Consumer<LocalDate> onDateSelected;

    public CalendarManager(GridPane calendarGrid, Label monthLabel, Consumer<LocalDate> onDateSelected) {
        this.calendarGrid = calendarGrid;
        this.monthLabel = monthLabel;
        this.onDateSelected = onDateSelected;
        this.currentYearMonth = YearMonth.now();
    }

    public void prevMonth() { currentYearMonth = currentYearMonth.minusMonths(1); refreshCalendar(); }
    public void nextMonth() { currentYearMonth = currentYearMonth.plusMonths(1); refreshCalendar(); }

    public void refreshCalendar() {
        // Pobieramy dane
        AsyncRunner.run(() -> {
            var transactions = transactionDAO.getTransactionsForMonth(currentYearMonth.getYear(), currentYearMonth.getMonthValue());
            var tasks = taskDAO.getAllTasks().stream()
                    .filter(t -> t.getDueDate() != null && YearMonth.from(t.getDueDate()).equals(currentYearMonth))
                    .collect(Collectors.toList());
            return new DataPackage(transactions, tasks);
        }, data -> {
            updateHeader(data);
            buildGrid(data);
        });
    }

    private void updateHeader(DataPackage data) {
        // Obliczamy bilans miesiąca do nagłówka
        double inc = data.transactions.stream().filter(t -> "PRZYCHÓD".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double exp = data.transactions.stream().filter(t -> "WYDATEK".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();

        String monthName = (currentYearMonth.getMonth().name() + " " + currentYearMonth.getYear()).toUpperCase();
        String summary = String.format(" (In: +%.0f / Out: -%.0f)", inc, exp);

        monthLabel.setText(monthName + summary);
    }

    private void buildGrid(DataPackage data) {
        calendarGrid.getChildren().clear();

        // Nagłówki (PON, WT...)
        String[] days = {"PON", "WT", "ŚR", "CZW", "PT", "SOB", "NDZ"};
        for (int i = 0; i < days.length; i++) {
            Label l = new Label(days[i]);
            l.getStyleClass().add("calendar-day-header");
            l.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(l, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date, data);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private VBox createDayCell(LocalDate date, DataPackage data) {
        VBox cell = new VBox(2); // Mały odstęp
        cell.getStyleClass().add("calendar-day");

        if (date.equals(LocalDate.now())) cell.getStyleClass().add("calendar-today");

        // Kliknięcie w dzień -> Dodaj coś
        cell.setOnMouseClicked(e -> handleDayClick(date));

        // Numer dnia
        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.getStyleClass().add("calendar-date-label");
        cell.getChildren().add(dateLbl);

        // --- RYSOWANIE ZDARZEŃ (MAX 3) ---
        int itemsShown = 0;
        int maxItems = 3;

        // 1. Zadania
        List<Task> dayTasks = data.tasks.stream().filter(t -> t.getDueDate().equals(date) && !t.isDone()).toList();
        for (Task t : dayTasks) {
            if (itemsShown >= maxItems) break;
            cell.getChildren().add(createPill(t.getTitle(), "event-task", "event-pill"));
            itemsShown++;
        }

        // 2. Transakcje
        List<Transaction> dayTrans = data.transactions.stream().filter(t -> t.getDate().equals(date)).toList();
        for (Transaction t : dayTrans) {
            if (itemsShown >= maxItems) break;
            String text = String.format("%.0f %s", t.getAmount(), t.getCategory());
            String style = "WYDATEK".equals(t.getType()) ? "event-expense" : "event-income";
            cell.getChildren().add(createPill(text, style, "event-pill"));
            itemsShown++;
        }

        // 3. Etykieta "+X więcej"
        int totalItems = dayTasks.size() + dayTrans.size();
        if (totalItems > maxItems) {
            Label more = new Label("+" + (totalItems - maxItems) + " więcej...");
            more.getStyleClass().add("event-more");
            cell.getChildren().add(more);
        }

        VBox.setVgrow(cell, Priority.ALWAYS);
        return cell;
    }

    private Label createPill(String text, String colorClass, String baseClass) {
        Label l = new Label(text);
        l.getStyleClass().addAll(baseClass, colorClass);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setTooltip(new Tooltip(text)); // Pełny tekst po najechaniu
        return l;
    }

    private void handleDayClick(LocalDate date) {
        // Wywołujemy callback w kontrolerze
        if (onDateSelected != null) {
            onDateSelected.accept(date);
        }
    }

    private record DataPackage(List<Transaction> transactions, List<Task> tasks) {}
}