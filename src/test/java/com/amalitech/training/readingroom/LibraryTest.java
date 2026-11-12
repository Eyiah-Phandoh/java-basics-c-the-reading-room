package com.amalitech.training.readingroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibraryTest {

    private ByteArrayOutputStream output;
    private Library library;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8);
        library = new Library("The Reading Room", out);
    }

    @Test
    void borrowsItemForTheCorrectPeriodAndRecordsHistory() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member student = library.addMember("Ama Owusu", MemberKind.STUDENT);

        library.borrow(student, book);

        Loan loan = library.activeLoanFor(book).orElseThrow();
        assertEquals(28, loan.loanDays());
        assertSame(student, loan.member());
        assertEquals(1, student.history().size());
        assertTrue(output().contains("Lent \"Things Fall Apart\" to Ama Owusu for 28 days"));
    }

    @Test
    void refusesSecondBorrowAndLeavesCustodyWithTheFirstMember() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member kofi = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        Member ama = library.addMember("Ama Owusu", MemberKind.STUDENT);
        library.borrow(kofi, book);

        ItemUnavailableException exception =
                assertThrows(ItemUnavailableException.class, () -> library.borrow(ama, book));

        assertEquals("Cannot lend \"Things Fall Apart\": already on loan to Kofi Mensah",
                exception.getMessage());
        Loan activeLoan = library.activeLoanFor(book).orElseThrow();
        assertSame(kofi, activeLoan.member());
        assertEquals(1, library.loans().size());
        assertEquals(0, ama.history().size());
        assertFalse(output().contains("to Ama Owusu"));
    }

    @Test
    void refusedBorrowDoesNotConsumeALoanId() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        LendingItem audio = library.addItem("French Audio Course", ItemType.AUDIO_KIT);
        Member kofi = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        Member ama = library.addMember("Ama Owusu", MemberKind.STUDENT);

        library.borrow(kofi, book);
        assertThrows(ItemUnavailableException.class, () -> library.borrow(ama, book));
        library.borrow(ama, audio);

        assertEquals(2, library.activeLoanFor(audio).orElseThrow().id());
    }

    @Test
    void rejectedAddDoesNotConsumeAnId() {
        assertThrows(IllegalArgumentException.class, () -> library.addItem("  ", ItemType.BOOK));
        assertThrows(NullPointerException.class, () -> library.addMember(null, MemberKind.STANDARD));

        assertEquals(1, library.addItem("Book", ItemType.BOOK).id());
        assertEquals(1, library.addMember("Member", MemberKind.STANDARD).id());
    }

    @Test
    void returnsOnlyTheCopyThatWasReturnedWhenTitlesRepeat() {
        LendingItem copyA = library.addItem("Dune", ItemType.BOOK);
        LendingItem copyB = library.addItem("Dune", ItemType.BOOK);
        Member alice = library.addMember("Alice", MemberKind.STANDARD);
        Member bob = library.addMember("Bob", MemberKind.STANDARD);
        library.borrow(alice, copyA);
        library.borrow(bob, copyB);

        library.returnItem(copyA);

        assertTrue(library.activeLoanFor(copyA).isEmpty());
        assertTrue(library.activeLoanFor(copyB).isPresent());
        assertEquals(LoanStatus.RETURNED, library.loans().get(0).status());
        assertEquals(LoanStatus.ACTIVE, library.loans().get(1).status());
    }

    @Test
    void returnsItemAndClearsCustody() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);
        Member member = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        library.borrow(member, book);

        library.returnItem(book);

        assertTrue(library.activeLoanFor(book).isEmpty());
        assertEquals(LoanStatus.RETURNED, library.loans().get(0).status());
        assertTrue(output().contains("Returned \"Things Fall Apart\""));
    }

    @Test
    void refusesReturnWhenItemIsNotOnLoan() {
        LendingItem book = library.addItem("Things Fall Apart", ItemType.BOOK);

        ItemNotOnLoanException exception =
                assertThrows(ItemNotOnLoanException.class, () -> library.returnItem(book));

        assertEquals("Cannot return \"Things Fall Apart\": item is not on loan",
                exception.getMessage());
        assertTrue(library.loans().isEmpty());
    }

    @Test
    void refusesEntitiesBelongingToAnotherLibrary() {
        Library other = new Library("Other Library", new PrintStream(output, true, StandardCharsets.UTF_8));
        LendingItem foreignItem = other.addItem("Foreign Book", ItemType.BOOK);
        LendingItem localItem = library.addItem("Local Book", ItemType.BOOK);
        Member localMember = library.addMember("Local Member", MemberKind.STANDARD);
        Member foreignMember = other.addMember("Foreign Member", MemberKind.STANDARD);

        assertThrows(UnregisteredItemException.class, () -> library.borrow(localMember, foreignItem));
        assertThrows(UnregisteredMemberException.class, () -> library.borrow(foreignMember, localItem));
        assertTrue(library.loans().isEmpty());
    }

    @Test
    void rejectsInvalidValues() {
        LendingItem item = library.addItem("Book", ItemType.BOOK);
        Member member = library.addMember("Member", MemberKind.STANDARD);

        assertThrows(NullPointerException.class, () -> library.addItem(null, ItemType.BOOK));
        assertThrows(NullPointerException.class, () -> library.addItem("Book", null));
        assertThrows(IllegalArgumentException.class, () -> library.addItem("  ", ItemType.BOOK));
        assertThrows(NullPointerException.class, () -> library.addMember(null, MemberKind.STANDARD));
        assertThrows(NullPointerException.class, () -> library.addMember("Member", null));
        assertThrows(IllegalArgumentException.class, () -> library.addMember("  ", MemberKind.STANDARD));
        assertThrows(NullPointerException.class, () -> library.borrow(null, item));
        assertThrows(NullPointerException.class, () -> library.borrow(member, null));
        assertThrows(NullPointerException.class, () -> library.returnItem(null));
        assertThrows(IllegalArgumentException.class, () -> new Library("  "));
    }

    @Test
    void exposesReadOnlyViews() {
        library.addItem("Book", ItemType.BOOK);
        Member member = library.addMember("Member", MemberKind.STANDARD);

        assertThrows(UnsupportedOperationException.class, () -> library.items().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.members().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.loans().clear());
        assertThrows(UnsupportedOperationException.class, () -> member.history().clear());
    }

    @Test
    void reportMatchesTheOriginalFormat() {
        LendingItem novel = library.addItem("Things Fall Apart", ItemType.BOOK);
        LendingItem audio = library.addItem("French Audio Course", ItemType.AUDIO_KIT);
        Member kofi = library.addMember("Kofi Mensah", MemberKind.STANDARD);
        Member ama = library.addMember("Ama Owusu", MemberKind.STUDENT);
        library.borrow(kofi, novel);
        library.borrow(ama, audio);
        library.returnItem(audio);

        ByteArrayOutputStream report = new ByteArrayOutputStream();
        new ReportPrinter().print(library, new PrintStream(report, true, StandardCharsets.UTF_8));

        String nl = System.lineSeparator();
        String expected = nl
                + "=== The Reading Room - end of day ===" + nl
                + "Items:" + nl
                + "  \"Things Fall Apart\" (BOOK): ON_LOAN - with Kofi Mensah" + nl
                + "  \"French Audio Course\" (AUDIO_KIT): AVAILABLE" + nl
                + "Active loans:" + nl
                + "  Loan #1: \"Things Fall Apart\" -> Kofi Mensah" + nl;
        assertEquals(expected, report.toString(StandardCharsets.UTF_8));
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
