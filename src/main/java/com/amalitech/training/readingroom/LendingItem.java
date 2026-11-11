package com.amalitech.training.readingroom;

import java.util.Objects;

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
    private final ItemType type;

    LendingItem(int id, String title, ItemType type) {
        this.id = id;
        this.title = Validation.requireText(title, "title");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    public ItemType type() {
        return type;
    }
}
