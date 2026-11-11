package com.amalitech.training.readingroom;

import java.util.Objects;

/**
 * Small validation helpers shared by the domain types.
 */
final class Validation {

    private Validation() {
    }

    static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
