package com.amalitech.training.readingroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OverdueTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);

    private Library library;

    @BeforeEach
    void setUp() {
        library = new Library("The Reading Room", new LoanPolicy(),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8), CLOCK);
    }

    @Test
    void recordsBorrowDateAndDerivesDueDateFromTheLoanPeriod() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member member = library.addMember("Kofi Mensah", MemberKind.STANDARD);

        library.borrow(member, book);
        Loan loan = library.activeLoanFor(book).orElseThrow();

        assertEquals(LocalDate.of(2026, 1, 1), loan.borrowedOn());
        assertEquals(LocalDate.of(2026, 1, 15), loan.dueDate());
    }

    @Test
    void treatsALoanAsOverdueOnlyAfterItsDueDate() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member member = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        library.borrow(member, book);
        Loan loan = library.activeLoanFor(book).orElseThrow();

        assertFalse(loan.isOverdue(LocalDate.of(2026, 1, 15)));
        assertTrue(loan.isOverdue(LocalDate.of(2026, 1, 16)));
        assertEquals(1, loan.daysOverdue(LocalDate.of(2026, 1, 16)));
        assertTrue(library.overdueLoans(LocalDate.of(2026, 1, 15)).isEmpty());
        assertEquals(1, library.overdueLoans(LocalDate.of(2026, 1, 16)).size());
    }

    @Test
    void returnedLoansAreNeverOverdue() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member member = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        library.borrow(member, book);
        library.returnItem(book);

        assertTrue(library.overdueLoans(LocalDate.of(2026, 2, 1)).isEmpty());
    }

    @Test
    void printsTheOverdueSectionAsPartOfTheEndOfDayReport() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member member = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        library.borrow(member, book);

        String output = report(LocalDate.of(2026, 1, 17));

        String nl = System.lineSeparator();
        String expected = nl
                + "=== The Reading Room - end of day ===" + nl
                + "Items:" + nl
                + "  \"Things Fall Apart\" (BOOK): ON_LOAN - with Kofi Mensah" + nl
                + "Active loans:" + nl
                + "  Loan #1: \"Things Fall Apart\" -> Kofi Mensah" + nl
                + nl
                + "=== The Reading Room - overdue items (as of 2026-01-17) ===" + nl
                + "  Loan #1: \"Things Fall Apart\" -> Kofi Mensah (due 2026-01-15, 2 days overdue)" + nl;
        assertEquals(expected, output);
    }

    @Test
    void printsNoOverdueMessageWhenNothingIsLate() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member member = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        library.borrow(member, book);

        String output = report(LocalDate.of(2026, 1, 10));

        String nl = System.lineSeparator();
        String expected = nl
                + "=== The Reading Room - end of day ===" + nl
                + "Items:" + nl
                + "  \"Things Fall Apart\" (BOOK): ON_LOAN - with Kofi Mensah" + nl
                + "Active loans:" + nl
                + "  Loan #1: \"Things Fall Apart\" -> Kofi Mensah" + nl
                + nl
                + "=== The Reading Room - overdue items (as of 2026-01-10) ===" + nl
                + "No overdue loans." + nl;
        assertEquals(expected, output);
    }

    private String report(LocalDate asOf) {
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        new ReportPrinter().print(library, asOf,
                new PrintStream(report, true, StandardCharsets.UTF_8));
        return report.toString(StandardCharsets.UTF_8);
    }
}
