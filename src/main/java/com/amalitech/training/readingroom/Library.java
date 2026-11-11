package com.amalitech.training.readingroom;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final LoanPolicy loanPolicy;
    private final PrintStream out;
    private final ReportPrinter reportPrinter = new ReportPrinter();

    private int nextItemId = 1;
    private int nextMemberId = 1;
    private int nextLoanId = 1;

    public Library(String name) {
        this(name, new LoanPolicy(), System.out);
    }

    public Library(String name, PrintStream out) {
        this(name, new LoanPolicy(), out);
    }

    public Library(String name, LoanPolicy loanPolicy, PrintStream out) {
        this.name = Validation.requireText(name, "name");
        this.loanPolicy = Objects.requireNonNull(loanPolicy, "loanPolicy must not be null");
        this.out = Objects.requireNonNull(out, "out must not be null");
    }

    public String name() {
        return name;
    }

    public LendingItem addItem(String title, ItemType type) {
        LendingItem item = new LendingItem(nextItemId++, title, type);
        items.add(item);
        return item;
    }

    public Member addMember(String name, MemberKind kind) {
        Member member = new Member(nextMemberId++, name, kind);
        members.add(member);
        return member;
    }

    /**
     * Lends an item to a member.
     *
     * @throws UnregisteredMemberException if the member does not belong to this library
     * @throws UnregisteredItemException if the item does not belong to this library
     * @throws ItemUnavailableException if the item already has an active loan
     */
    public void borrow(Member member, LendingItem item) {
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(item, "item must not be null");
        requireRegisteredMember(member);
        requireRegisteredItem(item);

        Loan existingLoan = activeLoans.get(item);
        if (existingLoan != null) {
            throw new ItemUnavailableException(item.title(), existingLoan.member().name());
        }

        int loanDays = loanPolicy.daysFor(item.type(), member.kind());
        Loan loan = new Loan(nextLoanId++, member, item, loanDays);

        loans.add(loan);
        member.recordBorrow(loan);
        activeLoans.put(item, loan);

        out.println("Lent \"" + item.title() + "\" to " + member.name() + " for " + loanDays + " days");
    }

    /**
     * Returns an item and closes its active loan.
     *
     * @throws UnregisteredItemException if the item does not belong to this library
     * @throws ItemNotOnLoanException if the item has no active loan
     */
    public void returnItem(LendingItem item) {
        Objects.requireNonNull(item, "item must not be null");
        requireRegisteredItem(item);

        Loan loan = activeLoans.get(item);
        if (loan == null) {
            throw new ItemNotOnLoanException(item.title());
        }

        loan.markReturned();
        activeLoans.remove(item);

        out.println("Returned \"" + item.title() + "\"");
    }

    public Optional<Loan> activeLoanFor(LendingItem item) {
        Objects.requireNonNull(item, "item must not be null");
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
        reportPrinter.print(this, out);
    }

    private void requireRegisteredMember(Member member) {
        if (!members.contains(member)) {
            throw new UnregisteredMemberException(member.name());
        }
    }

    private void requireRegisteredItem(LendingItem item) {
        if (!items.contains(item)) {
            throw new UnregisteredItemException(item.title());
        }
    }
}
