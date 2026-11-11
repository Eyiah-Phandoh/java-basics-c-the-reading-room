package com.amalitech.training.readingroom;

/**
 * A record of one loan. Created and updated only by {@link Library}.
 */
public final class Loan {

    private final int id;
    private final String memberName;
    private final String itemTitle;
    private final int loanDays;
    private String status = "ACTIVE";

    Loan(int id, String memberName, String itemTitle, int loanDays) {
        this.id = id;
        this.memberName = memberName;
        this.itemTitle = itemTitle;
        this.loanDays = loanDays;
    }

    public int id() {
        return id;
    }

    public String memberName() {
        return memberName;
    }

    public String itemTitle() {
        return itemTitle;
    }

    public int loanDays() {
        return loanDays;
    }

    public String status() {
        return status;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    void markReturned() {
        this.status = "RETURNED";
    }
}
