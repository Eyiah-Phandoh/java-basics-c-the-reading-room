package com.amalitech.training.readingroom;

/**
 * Thrown when a member is not registered with the library that is asked to lend to them.
 */
public final class UnregisteredMemberException extends RuntimeException {

    public UnregisteredMemberException(String memberName) {
        super("Member \"" + memberName + "\" does not belong to this library");
    }
}
