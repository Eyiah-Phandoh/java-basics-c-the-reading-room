package com.amalitech.training.readingroom;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A library member. Identity is assigned by the owning {@link Library}.
 */
public final class Member {

    private final int id;
    private final String name;
    private final MemberKind kind;
    private final List<Loan> history = new ArrayList<>();

    Member(int id, String name, MemberKind kind) {
        this.id = id;
        this.name = Validation.requireText(name, "name");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public MemberKind kind() {
        return kind;
    }

    public List<Loan> history() {
        return List.copyOf(history);
    }

    void recordBorrow(Loan loan) {
        history.add(loan);
    }
}
