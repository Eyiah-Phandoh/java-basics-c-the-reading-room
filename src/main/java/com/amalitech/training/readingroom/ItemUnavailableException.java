package com.amalitech.training.readingroom;

/**
 * Thrown when an item cannot be lent because it is already on loan.
 */
public final class ItemUnavailableException extends RuntimeException {

    public ItemUnavailableException(String itemTitle, String borrowerName) {
        super("Cannot lend \"" + itemTitle + "\": already on loan to " + borrowerName);
    }
}
