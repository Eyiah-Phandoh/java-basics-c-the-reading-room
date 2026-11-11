package com.amalitech.training.readingroom;

/**
 * A record of one loan. Created and updated only by {@link Library}.
 */
public final class Loan {

    private final int id;
    private final Member member;
    private final LendingItem item;
    private final int loanDays;
    private LoanStatus status = LoanStatus.ACTIVE;

    Loan(int id, Member member, LendingItem item, int loanDays) {
        this.id = id;
        this.member = member;
        this.item = item;
        this.loanDays = loanDays;
    }

    public int id() {
        return id;
    }

    public Member member() {
        return member;
    }

    public LendingItem item() {
        return item;
    }

    public int loanDays() {
        return loanDays;
    }

    public LoanStatus status() {
        return status;
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }

    void markReturned() {
        if (status == LoanStatus.RETURNED) {
            throw new IllegalStateException("Loan #" + id + " is already returned");
        }
        this.status = LoanStatus.RETURNED;
    }
}
