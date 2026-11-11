package com.amalitech.training.readingroom;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Renders the end-of-day report. Kept separate from {@link Library} so the console format
 * can be changed and tested without touching the lending rules.
 */
public final class ReportPrinter {

    public void print(Library library, PrintStream out) {
        out.println();
        out.println("=== " + library.name() + " - end of day ===");
        out.println("Items:");
        for (LendingItem item : library.items()) {
            Loan activeLoan = library.activeLoanFor(item).orElse(null);
            String status = activeLoan == null ? "AVAILABLE" : "ON_LOAN";
            out.println("  \"" + item.title() + "\" (" + item.type() + "): " + status
                    + (activeLoan != null ? " - with " + activeLoan.member().name() : ""));
        }
        out.println("Active loans:");
        for (Loan loan : library.loans()) {
            if (loan.isActive()) {
                out.println("  Loan #" + loan.id() + ": \"" + loan.item().title() + "\" -> " + loan.member().name());
            }
        }
    }

    /**
     * Prints the optional overdue section without changing the regular end-of-day report.
     */
    public void printOverdue(Library library, LocalDate asOf, PrintStream out) {
        List<Loan> overdue = library.overdueLoans(asOf);
        out.println();
        out.println("=== " + library.name() + " - overdue items (as of " + asOf + ") ===");
        if (overdue.isEmpty()) {
            out.println("No overdue loans.");
            return;
        }
        for (Loan loan : overdue) {
            long days = loan.daysOverdue(asOf);
            String unit = days == 1 ? "day" : "days";
            out.println("  Loan #" + loan.id() + ": \"" + loan.item().title() + "\" -> " + loan.member().name()
                    + " (due " + loan.dueDate() + ", " + days + " " + unit + " overdue)");
        }
    }
}
