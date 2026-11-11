package com.raceauditor.util;

import com.raceauditor.model.Category;
import com.raceauditor.model.Expense;
import com.raceauditor.service.ReportService.MonthlyReport;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Exports a monthly report to CSV or PDF using the real expense/report data.
 * Dependency-free on purpose: CSV is plain UTF-8 text, PDF is a minimal
 * valid document written with the built-in Helvetica fonts (no new libraries).
 */
public final class ReportExporter {
    private ReportExporter() {}

    /** Indian-rupee amount rendered with plain ASCII ("Rs.") for PDF safety. */
    public static String pdfAmount(double amount) {
        return CurrencyUtil.inr(amount).replace("₹", "Rs. ").replace("Rs.  ", "Rs. ");
    }

    // ------------------------------------------------------------------ CSV

    /** Writes summary + category breakdown + every expense line to UTF-8 CSV. */
    public static void exportCsv(File f, String monthName, int year,
                                MonthlyReport r, List<Expense> items) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("﻿"); // UTF-8 BOM so Excel shows the rupee sign correctly
        sb.append("Race Auditor Monthly Report\n");
        sb.append("Month,").append(csv(monthName + " " + year)).append("\n");
        sb.append("Monthly Total,").append(csv(CurrencyUtil.inr(r.total))).append("\n");
        sb.append("Transactions,").append(r.transactions).append("\n");
        sb.append("Top Category,").append(csv(r.topCategory)).append("\n");
        sb.append("\nCategory,Transactions,Total\n");
        for (Category c : r.byCategory) {
            sb.append(csv(c.getCategory())).append(",")
              .append(c.getCount()).append(",")
              .append(csv(CurrencyUtil.inr(c.getTotal()))).append("\n");
        }
        sb.append("\nDate,Description,Category,Amount,Payment,Notes\n");
        for (Expense e : items) {
            sb.append(csv(dateOf(e))).append(",")
              .append(csv(e.getTitle())).append(",")
              .append(csv(e.getCategory())).append(",")
              .append(csv(String.format(Locale.US, "%.2f", e.getAmount()))).append(",")
              .append(csv(e.getPaymentMethod())).append(",")
              .append(csv(e.getNotes())).append("\n");
        }
        Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String dateOf(Expense e) {
        return e.getDate() == null ? "" : e.getDate().toString();
    }

    private static String csv(String s) {
        if (s == null) return "";
        boolean quote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String q = s.replace("\"", "\"\"");
        return quote ? "\"" + q + "\"" : q;
    }

    // ------------------------------------------------------------------ PDF

    private static final class Line {
        final String text;
        final boolean title;
        final boolean section;
        Line(String text, boolean title, boolean section) {
            this.text = text; this.title = title; this.section = section;
        }
    }

    /** Writes a minimal valid multi-page PDF with the real report data. */
    public static void exportPdf(File f, String monthName, int year,
                                MonthlyReport r, List<Expense> items) throws IOException {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line("Race Auditor - Monthly Report", true, false));
        lines.add(new Line(monthName + " " + year, false, true));
        lines.add(new Line("Monthly Total: " + pdfAmount(r.total), false, false));
        lines.add(new Line("Transactions: " + r.transactions, false, false));
        lines.add(new Line("Top Category: " + sanitize(r.topCategory), false, false));
        lines.add(new Line("", false, false));
        lines.add(new Line("Category Breakdown", false, true));
        if (r.byCategory.isEmpty()) {
            lines.add(new Line("No spending this month.", false, false));
        } else {
            for (Category c : r.byCategory) {
                lines.add(new Line(c.getCategory() + "  |  " + c.getCount()
                    + " transactions  |  " + pdfAmount(c.getTotal()), false, false));
            }
        }
        lines.add(new Line("", false, false));
        lines.add(new Line("Transactions", false, true));
        if (items.isEmpty()) {
            lines.add(new Line("No expenses recorded for this month.", false, false));
        } else {
            for (Expense e : items) {
                String row = dateOf(e) + "  |  " + safe(e.getTitle()) + "  |  "
                    + safe(e.getCategory()) + "  |  Rs. "
                    + String.format(Locale.US, "%.2f", e.getAmount()) + "  |  "
                    + safe(e.getPaymentMethod());
                if (e.getNotes() != null && !e.getNotes().isBlank()) row += "  |  " + safe(e.getNotes());
                for (String part : wrap(sanitize(row), 100)) lines.add(new Line(part, false, false));
            }
        }

        // Paginate (48 lines per A4 page).
        List<List<Line>> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += 48) pages.add(lines.subList(i, Math.min(i + 48, lines.size())));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        write(out, "%PDF-1.4\n");
        // 1: catalog, 2: pages, 3: regular font, 4: bold font.
        offsets.add(out.size()); ids.add(1);
        write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        StringBuilder kids = new StringBuilder();
        int nextId = 5;
        List<Integer> pageIds = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) { pageIds.add(nextId); kids.append(nextId).append(" 0 R "); nextId += 2; }
        offsets.add(out.size()); ids.add(2);
        write(out, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + pages.size() + " >>\nendobj\n");
        offsets.add(out.size()); ids.add(3);
        write(out, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        offsets.add(out.size()); ids.add(4);
        write(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");
        List<Integer> contentIds = new ArrayList<>();
        for (int p = 0; p < pages.size(); p++) {
            int pageId = pageIds.get(p);
            int contentId = pageId + 1;
            contentIds.add(contentId);
            offsets.add(out.size()); ids.add(pageId);
            write(out, pageId + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentId + " 0 R >>\nendobj\n");
            StringBuilder stream = new StringBuilder();
            List<Line> pl = pages.get(p);
            for (int i = 0; i < pl.size(); i++) {
                Line l = pl.get(i);
                int size = l.title ? 16 : (l.section ? 12 : 10);
                String font = (l.title || l.section) ? "F2" : "F1";
                int y = 780 - i * 15;
                stream.append("BT /").append(font).append(" ").append(size)
                      .append(" Tf 56 ").append(y).append(" Td (")
                      .append(pdfEscape(sanitize(l.text))).append(") Tj ET\n");
            }
            byte[] bytes = stream.toString().getBytes(StandardCharsets.US_ASCII);
            offsets.add(out.size()); ids.add(contentId);
            write(out, contentId + " 0 obj\n<< /Length " + bytes.length + " >>\nstream\n");
            out.write(bytes, 0, bytes.length);
            write(out, "\nendstream\nendobj\n");
        }
        int maxId = contentIds.isEmpty() ? 4 : contentIds.get(contentIds.size() - 1);
        int xrefPos = out.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (maxId + 1) + "\n");
        xref.append("0000000000 65535 f \n");
        // offsets were recorded in id order 1..maxId.
        for (int id = 1; id <= maxId; id++) {
            int idx = ids.indexOf(id);
            xref.append(String.format(Locale.US, "%010d 00000 n \n", offsets.get(idx)));
        }
        write(out, xref.toString());
        write(out, "trailer\n<< /Size " + (maxId + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefPos + "\n%%EOF");
        Files.write(f.toPath(), out.toByteArray());
    }

    private static void write(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /** Maps text to PDF-safe ASCII (Helvetica WinAnsi has no rupee/em-dash glyphs). */
    static String sanitize(String s) {
        if (s == null) return "";
        String t = s.replace("₹", "Rs. ").replace("—", "-").replace("–", "-")
                    .replace("“", "\"").replace("”", "\"").replace("‘", "'").replace("’", "'");
        StringBuilder sb = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char ch = t.charAt(i);
            if ((ch >= 32 && ch <= 126) || (ch >= 160 && ch <= 255)) sb.append(ch);
            else if (ch == '\n' || ch == '\r' || ch == '\t') sb.append(' ');
            else sb.append('?');
        }
        return sb.toString();
    }

    private static String pdfEscape(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static List<String> wrap(String s, int width) {
        List<String> out = new ArrayList<>();
        String rest = s;
        while (rest.length() > width) {
            int cut = rest.lastIndexOf(' ', width);
            if (cut <= 0) cut = width;
            out.add(rest.substring(0, cut));
            rest = rest.substring(cut).trim();
        }
        out.add(rest);
        return out;
    }
}
