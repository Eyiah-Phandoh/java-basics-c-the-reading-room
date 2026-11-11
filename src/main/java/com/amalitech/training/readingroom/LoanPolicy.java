package com.amalitech.training.readingroom;

import java.util.Objects;

/**
 * The library's lending rules, kept in one place so a new item type or member kind only
 * has to be described here.
 */
public final class LoanPolicy {

    public int daysFor(ItemType type, MemberKind kind) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(kind, "kind");

        return switch (type) {
            case BOOK -> switch (kind) {
                case STANDARD -> 14;
                case STUDENT -> 28;
            };
            case AUDIO_KIT -> switch (kind) {
                case STANDARD -> 7;
                case STUDENT -> 14;
            };
        };
    }
}
