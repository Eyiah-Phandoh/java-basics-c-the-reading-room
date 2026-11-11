package com.amalitech.training.readingroom;

/**
 * Thrown when an item is not registered with the library that is asked to lend or return
 * it.
 */
public final class UnregisteredItemException extends RuntimeException {

    public UnregisteredItemException(String itemTitle) {
        super("Item \"" + itemTitle + "\" does not belong to this library");
    }
}
