package com.raceauditor.ui.reports;

import com.raceauditor.model.Category;
import com.raceauditor.model.Expense;
import com.raceauditor.model.User;
import com.raceauditor.service.ReportService.MonthlyReport;
import com.raceauditor.util.ReportExporter;
import java.awt.GraphicsEnvironment;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Verifies report exports carry the real data (PDF + CSV) and that the
 * Reports screen keeps a clear, visible Export option next to Generate.
 */
class ReportExportTest {

    private static MonthlyReport sampleReport() {
        List<Category> cats = List.of(
            new Category("Bills", 2, 500.00),
            new Category("Food", 3, 389.00));
        return new MonthlyReport(889.00, 5, "Bills", cats);
    }

    private static List<Expense> sampleItems() {
        Expense a = new Expense();
        a.setDate(LocalDate.of(2026, 10, 2));
        a.setTitle("Electricity Bill");
        a.setCategory("Bills");
        a.setAmount(300.00);
        a.setPaymentMethod("UPI");
        a.setNotes("October bill");
        Expense b = new Expense();
        b.setDate(LocalDate.of(2026, 10, 5));
        b.setTitle("Groceries");
        b.setCategory("Food");
        b.setAmount(589.00);
        b.setPaymentMethod("Card");
        b.setNotes(null);
        return List.of(a, b);
    }

    @Test
    void exporterCsvContainsRealData() throws Exception {
        Path dir = Files.createTempDirectory("race-csv");
        try {
            Path csv = dir.resolve("report.csv");
            ReportExporter.exportCsv(csv.toFile(), "October", 2026, sampleReport(), sampleItems());
            String text = Files.readString(csv, StandardCharsets.UTF_8);
            assertTrue(text.contains("Race Auditor Monthly Report"), "missing title");
            assertTrue(text.contains("October 2026"), "missing month");
            assertTrue(text.contains("889"), "missing total");
            assertTrue(text.contains("Bills"), "missing category");
            assertTrue(text.contains("Electricity Bill"), "missing expense title");
            assertTrue(text.contains("589.00"), "missing expense amount");
            assertTrue(text.contains("Date,Description,Category,Amount,Payment,Notes"), "missing item header");
        } finally {
            deleteQuietly(dir);
        }
    }

    @Test
    void exporterPdfIsValidAndContainsRealData() throws Exception {
        Path dir = Files.createTempDirectory("race-pdf");
        try {
            Path pdf = dir.resolve("report.pdf");
            ReportExporter.exportPdf(pdf.toFile(), "October", 2026, sampleReport(), sampleItems());
            byte[] bytes = Files.readAllBytes(pdf);
            String head = new String(bytes, 0, Math.min(bytes.length, 8), StandardCharsets.US_ASCII);
            assertTrue(head.startsWith("%PDF-"), "not a PDF document, got: " + head);
            String text = new String(bytes, StandardCharsets.US_ASCII);
            assertTrue(text.contains("Race Auditor - Monthly Report"), "missing title");
            assertTrue(text.contains("October 2026"), "missing month");
            assertTrue(text.contains("889.00"), "missing total");
            assertTrue(text.contains("Electricity Bill"), "missing expense line");
            assertTrue(text.trim().endsWith("%%EOF"), "PDF trailer missing");
        } finally {
            deleteQuietly(dir);
        }
    }

    @Test
    void exportButtonsPresentAndVisible() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "No display available");
        ReportsPanel panel = new ReportsPanel(new User());
        JButton pdf = panel.getExportPdfButton();
        JButton csv = panel.getExportCsvButton();
        assertNotNull(pdf, "Export PDF button missing");
        assertNotNull(csv, "Export CSV button missing");
        assertEquals("Export PDF", pdf.getText());
        assertEquals("Export CSV", csv.getText());
        assertTrue(pdf.isVisible() && pdf.isEnabled(), "Export PDF must stay visible/enabled");
        assertTrue(csv.isVisible() && csv.isEnabled(), "Export CSV must stay visible/enabled");
        assertTrue(pdf.getParent() == csv.getParent()
            && pdf.getParent() != null, "Export buttons must sit in the control bar");
    }

    private static void deleteQuietly(Path dir) {
        try {
            Files.walk(dir).sorted(Comparator.reverseOrder())
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
        } catch (Exception ignored) {}
    }
}
