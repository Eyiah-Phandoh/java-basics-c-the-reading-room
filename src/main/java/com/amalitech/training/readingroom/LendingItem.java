package com.amalitech.training.readingroom;

/**
 * A physical item that can be lent by a library.
 *
 * <p>Identity is assigned by the owning {@link Library}; callers never choose it. An item
 * has no custody flag of its own: whether it is available is derived from the library's
 * active loans.
 */
public final class LendingItem {

    private final int id;
    private final String title;
    private final String type;

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
}
