package com.amalitech.training.readingroom;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The library aggregate. It owns items, members, and loans, and is the only
 * place from which their state is changed.
 */
public final class Library {

    private final String name;
    private final List<LendingItem> items = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private final Map<LendingItem, Loan> activeLoans = new IdentityHashMap<>();
    private final PrintStream out;

    private int nextItemId = 1;
    private int nextMemberId = 1;
    private int nextLoanId = 1;

    public Library(String name) {
        this(name, System.out);
    }

    public Library(String name, PrintStream out) {
        this.name = name;
        this.out = out;
    }

    public String name() {
        return name;
    }

    public LendingItem addItem(String title, String type) {
        LendingItem item = new LendingItem(nextItemId++, title, type);
        items.add(item);
        return item;
    }

    public Member addMember(String name, String kind) {
        Member member = new Member(nextMemberId++, name, kind);
        members.add(member);
        return member;
    }

    public void borrow(Member member, LendingItem item) {
        Loan existingLoan = activeLoans.get(item);
        if (existingLoan != null) {
            throw new ItemUnavailableException(item.title(), existingLoan.member().name());
        }

        int loanDays = 0;
        if (item.type().equals("BOOK")) {
            loanDays = 14;
            if (member.kind().equals("STUDENT")) {
                loanDays = 28; // students get longer loans
            }
            out.println("Lent \"" + item.title() + "\" to " + member.name() + " for " + loanDays + " days");
        } else if (item.type().equals("AUDIO_KIT")) {
            loanDays = 7;
            if (member.kind().equals("STUDENT")) {
                loanDays = 14; // students get longer loans
            }
            out.println("Lent \"" + item.title() + "\" to " + member.name() + " for " + loanDays + " days");
        }

        Loan loan = new Loan(nextLoanId++, member, item, loanDays);
        member.recordBorrow(loan);
        loans.add(loan);
        activeLoans.put(item, loan);
    }

    public void returnItem(LendingItem item) {
        Loan loan = activeLoans.remove(item);
        if (loan == null) {
            throw new ItemNotOnLoanException(item.title());
        }
        loan.markReturned();
        out.println("Returned \"" + item.title() + "\"");
    }

    public Optional<Loan> activeLoanFor(LendingItem item) {
        return Optional.ofNullable(activeLoans.get(item));
    }

    public List<LendingItem> items() {
        return List.copyOf(items);
    }

    public List<Member> members() {
        return List.copyOf(members);
    }

    public List<Loan> loans() {
        return List.copyOf(loans);
    }

    public void printReport() {
        out.println();
        out.println("=== " + name + " - end of day ===");
        out.println("Items:");
        for (LendingItem item : items) {
            Loan activeLoan = activeLoans.get(item);
            String status = activeLoan == null ? "AVAILABLE" : "ON_LOAN";
            out.println("  \"" + item.title() + "\" (" + item.type() + "): " + status
                    + (activeLoan != null ? " - with " + activeLoan.member().name() : ""));
        }
        out.println("Active loans:");
        for (Loan loan : loans) {
            if (loan.isActive()) {
                out.println("  Loan #" + loan.id() + ": \"" + loan.item().title() + "\" -> " + loan.member().name());
            }
        }
    }
}
