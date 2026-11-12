package com.amalitech.training.readingroom;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * A record of one loan. Created and updated only by {@link Library}.
 */
public final class Loan {

    private final int id;
    private final Member member;
    private final LendingItem item;
    private final int loanDays;
    private final LocalDate borrowedOn;
    private final LocalDate dueDate;
    private LoanStatus status = LoanStatus.ACTIVE;

    Loan(int id, Member member, LendingItem item, int loanDays, LocalDate borrowedOn) {
        if (loanDays <= 0) {
            throw new IllegalArgumentException("loanDays must be positive, was " + loanDays);
        }
        this.id = id;
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.item = Objects.requireNonNull(item, "item must not be null");
        this.loanDays = loanDays;
        this.borrowedOn = Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");
        this.dueDate = borrowedOn.plusDays(loanDays);
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

    public LocalDate borrowedOn() {
        return borrowedOn;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public LoanStatus status() {
        return status;
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }

    /**
     * Returns whether this active loan was due before {@code asOf}. A loan due today is not
     * overdue until the next day.
     */
    public boolean isOverdue(LocalDate asOf) {
        Objects.requireNonNull(asOf, "asOf must not be null");
        return isActive() && dueDate.isBefore(asOf);
    }

    /**
     * Returns how many days past the due date {@code asOf} is, or zero when the loan is not
     * overdue.
     */
    public long daysOverdue(LocalDate asOf) {
        return isOverdue(asOf) ? ChronoUnit.DAYS.between(dueDate, asOf) : 0;
    }

    void markReturned() {
        if (status == LoanStatus.RETURNED) {
            throw new IllegalStateException("Loan #" + id + " is already returned");
        }
        this.status = LoanStatus.RETURNED;
    }
}
