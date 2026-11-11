package com.amalitech.training.readingroom;

/**
 * A physical item that can be lent by a library.
 *
 * <p>Identity is assigned by the owning {@link Library}; callers never choose it.
 */
public final class LendingItem {

    private final int id;
    private final String title;
    private final String type;
    private String status = "AVAILABLE";
    private String currentBorrower;

    LendingItem(int id, String title, String type) {
        this.id = id;
        this.title = title;
        this.type = type;
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String type() {
        return type;
    }

    public String status() {
        return status;
    }

    public String currentBorrower() {
        return currentBorrower;
    }

    public boolean isOnLoan() {
        return "ON_LOAN".equals(status);
    }

    void markOnLoan(String borrower) {
        this.status = "ON_LOAN";
        this.currentBorrower = borrower;
    }

    void markAvailable() {
        this.status = "AVAILABLE";
        this.currentBorrower = null;
    }
}
