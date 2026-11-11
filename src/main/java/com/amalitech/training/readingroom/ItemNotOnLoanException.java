package com.amalitech.training.readingroom;

/**
 * Thrown when an item is returned but the library has no active loan for it.
 */
public final class ItemNotOnLoanException extends RuntimeException {

    public ItemNotOnLoanException(String itemTitle) {
        super("Cannot return \"" + itemTitle + "\": item is not on loan");
    }
}
