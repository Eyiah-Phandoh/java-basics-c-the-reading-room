package com.amalitech.training.readingroom;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanTest {
    private final Member member = new Member(1, "M", MemberKind.STANDARD);
    private final LendingItem item = new LendingItem(1, "B", ItemType.BOOK);
    private final LocalDate day = LocalDate.of(2026, 1, 1);

    @Test
    void rejectsNonPositiveLoanDays() {
        assertThrows(IllegalArgumentException.class, () -> new Loan(1, member, item, 0, day));
        assertThrows(IllegalArgumentException.class, () -> new Loan(1, member, item, -3, day));
    }
}