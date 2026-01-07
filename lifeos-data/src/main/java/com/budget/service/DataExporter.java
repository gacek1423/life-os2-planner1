package com.budget.service;

import com.budget.model.Transaction;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class DataExporter {

    public void exportTransactionsToCSV(List<Transaction> transactions, Stage stage) {
        // Okno wyboru gdzie zapisać plik
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz raport finansowy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("finanse_raport.csv");

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                // Nagłówek Excela
                writer.println("ID;Data;Typ;Kategoria;Opis;Kwota");

                // Dane
                for (Transaction t : transactions) {
                    writer.printf("%d;%s;%s;%s;%s;%.2f%n",
                            t.getId(),
                            t.getDate(),
                            t.getType(),
                            t.getCategory(),
                            t.getDescription(),
                            t.getAmount());
                }
                System.out.println("Eksport zakończony sukcesem: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void exportHtmlReport(String htmlContent, String defaultFileName, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz Raport Finansowy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki HTML (*.html)", "*.html"));
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.write(htmlContent);
                // Opcjonalnie: Spróbuj otworzyć plik w domyślnej przeglądarce
                java.awt.Desktop.getDesktop().browse(file.toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}