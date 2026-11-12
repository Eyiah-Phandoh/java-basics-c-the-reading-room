package com.amalitech.training.readingroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoanPolicyTest {

    private final LoanPolicy policy = new LoanPolicy();

    @Test
    void standardBookLoanIsFourteenDays() {
        assertEquals(14, policy.daysFor(ItemType.BOOK, MemberKind.STANDARD));
    }

    @Test
    void studentBookLoanIsTwentyEightDays() {
        assertEquals(28, policy.daysFor(ItemType.BOOK, MemberKind.STUDENT));
    }

    @Test
    void standardAudioKitLoanIsSevenDays() {
        assertEquals(7, policy.daysFor(ItemType.AUDIO_KIT, MemberKind.STANDARD));
    }

    @Test
    void studentAudioKitLoanIsFourteenDays() {
        assertEquals(14, policy.daysFor(ItemType.AUDIO_KIT, MemberKind.STUDENT));
    }

    @Test
    void rejectsMissingValues() {
        assertThrows(NullPointerException.class, () -> policy.daysFor(null, MemberKind.STANDARD));
        assertThrows(NullPointerException.class, () -> policy.daysFor(ItemType.BOOK, null));
    }
    @Test
    void everyItemTypeAndMemberKindHasAPositiveLoanPeriod() {
        for (ItemType type : ItemType.values()) {
            for (MemberKind kind : MemberKind.values()) {
                assertTrue(policy.daysFor(type, kind) > 0, type + " / " + kind);
            }
        }
    }
}
