package com.amalitech.training.readingroom;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Renders the end-of-day report. Kept separate from {@link Library} so the console format
 * can be changed and tested without touching the lending rules.
 */
public final class ReportPrinter {

    /**
     * Prints the full end-of-day report, including the overdue section as of the library's
     * current date.
     */
    public void print(Library library, PrintStream out) {
        print(library, library.today(), out);
    }

    /**
     * Prints the full end-of-day report as of {@code asOf}. The date is a parameter so tests
     * can pin the overdue section without depending on the wall clock.
     */
    public void print(Library library, LocalDate asOf, PrintStream out) {
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
        printOverdueSection(library, asOf, out);
    }

    private void printOverdueSection(Library library, LocalDate asOf, PrintStream out) {
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
