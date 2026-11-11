package com.amalitech.training.readingroom;

import java.util.ArrayList;
import java.util.List;

/**
 * A library member. Identity is assigned by the owning {@link Library}.
 */
public final class Member {

    private final int id;
    private final String name;
    private final String kind;
    private final List<Loan> history = new ArrayList<>();

    Member(int id, String name, String kind) {
        this.id = id;
        this.name = name;
        this.kind = kind;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String kind() {
        return kind;
    }

    public List<Loan> history() {
        return List.copyOf(history);
    }

    void recordBorrow(Loan loan) {
        history.add(loan);
    }
}
