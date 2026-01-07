package com.budget.service;

import com.budget.dao.PurseDAO;
import com.budget.dao.TransactionDAO;
import com.budget.model.Transaction;
import com.budget.modules.finance.domain.Purse;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final PurseDAO purseDAO = new PurseDAO();

    // Główna metoda generująca treść HTML
    public String generateMonthlyReportHtml(YearMonth month) {
        // 1. POBIERANIE DANYCH
        List<Transaction> transactions = transactionDAO.getTransactionsForMonth(month.getYear(), month.getMonthValue());
        List<Purse> purses = purseDAO.getAllPurses();

        double totalIncome = transactions.stream().filter(t -> "PRZYCHÓD".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = transactions.stream().filter(t -> "WYDATEK".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double savings = totalIncome - totalExpense;
        double savingsRate = (totalIncome > 0) ? (savings / totalIncome) * 100 : 0;

        // 2. ANALIZA ANOMALII (AI INSIGHTS)
        List<String> insights = detectAnomalies(month, transactions);

        // 3. BUDOWANIE HTML
        StringBuilder html = new StringBuilder();
        html.append("""
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', sans-serif; color: #333; padding: 40px; }
                    /* ... inne style ... */
                    
                    /* TUTAJ BYŁ BŁĄD - dodajemy podwójny procent %% */
                    table { width: 100%%; border-collapse: collapse; margin-bottom: 20px; font-size: 14px; }
                    
                    /* ... reszta stylów ... */
                    .kpi h3 { margin: 0; font-size: 12px; color: #666; text-transform: uppercase; }
                    /* W Savings Rate też musi być %% jeśli jest w formatted stringu */
                    
            """.formatted(month.toString(), java.time.LocalDate.now()));

        // KPI
        html.append("""
            <div class='kpi-box'>
                <div class='kpi'><h3>Przychody</h3><p class='positive'>+%.2f PLN</p></div>
                <div class='kpi'><h3>Wydatki</h3><p class='negative'>-%.2f PLN</p></div>
                <div class='kpi'><h3>Oszczędności</h3><p style='color: %s'>%.2f PLN</p></div>
                <div class='kpi'><h3>Savings Rate</h3><p>%.1f%%</p></div>
            </div>
            """.formatted(
                totalIncome,
                totalExpense,
                savings >= 0 ? "#28a745" : "#dc3545", savings,
                savingsRate));

        // AI INSIGHTS
        if (!insights.isEmpty()) {
            html.append("<div class='section-title'>🔍 Analiza i Wskazówki (AI)</div>");
            for (String insight : insights) {
                html.append("<div class='insight'>%s</div>".formatted(insight));
            }
        }

        // PURSES
        html.append("<div class='section-title'>💳 Status Portfeli (Purses)</div>");
        html.append("<table><tr><th>Portfel</th><th>Typ</th><th>Alokacja</th><th>Wydano</th><th>Dostępne</th></tr>");
        for (Purse p : purses) {
            html.append("<tr><td>%s</td><td>%s</td><td>%.2f</td><td>%.2f</td><td><b>%.2f</b></td></tr>"
                    .formatted(p.getName(), p.getType(), p.getAllocatedAmount(), p.getSpentAmount(), p.getAvailable()));
        }
        html.append("</table>");

        // TRANSAKCJE
        html.append("<div class='section-title'>📄 Szczegółowy Wykaz Transakcji</div>");
        html.append("<table><tr><th>Data</th><th>Kategoria</th><th>Opis</th><th>Kwota</th><th>Typ</th></tr>");
        for (Transaction t : transactions) {
            html.append("<tr><td>%s</td><td>%s</td><td>%s</td><td class='%s'>%.2f</td><td>%s</td></tr>"
                    .formatted(t.getDate(), t.getCategory(), t.getDescription(),
                            t.getType().equals("PRZYCHÓD") ? "positive" : "negative",
                            t.getAmount(), t.getType()));
        }
        html.append("</table>");

        html.append("<div class='footer'>Wygenerowano automatycznie przez Life OS 2.0. Dokument gotowy do celów podatkowych.</div>");
        html.append("</body></html>");

        return html.toString();
    }

    // Prosta logika wykrywania anomalii (porównanie z poprzednim miesiącem by było lepsze, tu uproszczone)
    private List<String> detectAnomalies(YearMonth currentMonth, List<Transaction> transactions) {
        List<String> tips = new ArrayList<>();

        // 1. Analiza kategorii wydatków
        Map<String, Double> categorySpend = transactions.stream()
                .filter(t -> "WYDATEK".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));

        // Sprawdzamy czy jakaś kategoria przekracza np. 1000 zł (prosty próg na start)
        for (Map.Entry<String, Double> entry : categorySpend.entrySet()) {
            if (entry.getValue() > 1000) {
                tips.add("⚠️ <b>Wysokie wydatki:</b> Kategoria '" + entry.getKey() + "' pochłonęła aż " + String.format("%.2f", entry.getValue()) + " PLN w tym miesiącu.");
            }
        }

        // 2. Savings Rate check
        double income = transactions.stream().filter(t -> "PRZYCHÓD".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double expense = transactions.stream().filter(t -> "WYDATEK".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();

        if (income > 0 && (income - expense) < 0) {
            tips.add("🚨 <b>Burn Rate Alert:</b> Wydajesz więcej niż zarabiasz! Bilans ujemny.");
        }

        if (tips.isEmpty()) {
            tips.add("✅ <b>Dobra robota:</b> Nie wykryto znaczących anomalii w tym miesiącu.");
        }
        return tips;
    }
}