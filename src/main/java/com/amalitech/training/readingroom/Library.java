package com.amalitech.training.readingroom;

import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDate;
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
    private final Map<LendingItem, List<Loan>> activeLoans = new IdentityHashMap<>();
    private final LoanPolicy loanPolicy;
    private final PrintStream out;
    private final Clock clock;
    private final ReportPrinter reportPrinter = new ReportPrinter();

    private int nextItemId = 1;
    private int nextMemberId = 1;
    private int nextLoanId = 1;

    public Library(String name) {
        this(name, new LoanPolicy(), System.out, Clock.systemDefaultZone());
    }

    public Library(String name, PrintStream out) {
        this(name, new LoanPolicy(), out, Clock.systemDefaultZone());
    }

    public Library(String name, LoanPolicy loanPolicy, PrintStream out) {
        this(name, loanPolicy, out, Clock.systemDefaultZone());
    }

    public Library(String name, LoanPolicy loanPolicy, PrintStream out, Clock clock) {
        this.name = Validation.requireText(name, "name");
        this.loanPolicy = Objects.requireNonNull(loanPolicy, "loanPolicy must not be null");
        this.out = Objects.requireNonNull(out, "out must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public String name() {
        return name;
    }

    public LendingItem addItem(String title, ItemType type) {
        LendingItem item = new LendingItem(nextItemId, title, type);
        nextItemId++;
        items.add(item);
        return item;
    }

    public Member addMember(String name, MemberKind kind) {
        Member member = new Member(nextMemberId, name, kind);
        nextMemberId++;
        members.add(member);
        return member;
    }

    /**
     * Lends an item to a member.
     *
     * @throws UnregisteredMemberException if the member does not belong to this library
     * @throws UnregisteredItemException if the item does not belong to this library
     * @throws ItemUnavailableException already held by as many members as the policy allows
     */
    public void borrow(Member member, LendingItem item) {
        borrow(member, item, LocalDate.now(clock));
    }

    public void borrow(Member member, LendingItem item, LocalDate borrowedOn) {
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(item, "item must not be null");
        Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");

        requireRegisteredMember(member);
        requireRegisteredItem(item);

        List<Loan> active = activeLoans.getOrDefault(item, List.of());
        if (active.size() >= loanPolicy.maxConcurrentLoans(item.type())) {
            throw new ItemUnavailableException(item.title(), active.get(0).member().name());
        }

        int loanDays = loanPolicy.daysFor(item.type(), member.kind());
        Loan loan = new Loan(nextLoanId++, member, item, loanDays, borrowedOn);

        loans.add(loan);
        member.recordBorrow(loan);
        activeLoans.computeIfAbsent(item, k -> new ArrayList<>()).add(loan);

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
        List<Loan> active = activeLoans.getOrDefault(item, List.of());
        if (active.isEmpty()) {
            throw new ItemNotOnLoanException(item.title());
        }
        if (active.size() > 1) {
            throw new IllegalStateException(
                    "\"" + item.title() + "\" has several active loans; use returnItem(member, item)");
        }
        closeLoan(item, active.get(0));
    }

    public void returnItem(Member member, LendingItem item) {
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(item, "item must not be null");
        requireRegisteredMember(member);
        requireRegisteredItem(item);
        Loan loan = activeLoans.getOrDefault(item, List.of()).stream()
                .filter(l -> l.member() == member).findFirst()
                .orElseThrow(() -> new ItemNotOnLoanException(item.title()));
        closeLoan(item, loan);
    }

    private void closeLoan(LendingItem item, Loan loan) {
        loan.markReturned();
        List<Loan> active = activeLoans.get(item);
        active.remove(loan);
        if (active.isEmpty()) {
            activeLoans.remove(item);
        }
        out.println("Returned \"" + item.title() + "\"");
    }

    public Optional<Loan> activeLoanFor(LendingItem item) {
        return activeLoansFor(item).stream().findFirst();
    }

    public List<Loan> activeLoansFor(LendingItem item) {
        Objects.requireNonNull(item, "item must not be null");
        return List.copyOf(activeLoans.getOrDefault(item, List.of()));
    }

    /**
     * Returns active loans whose due date is before {@code asOf}, in loan order.
     */
    public List<Loan> overdueLoans(LocalDate asOf) {
        Objects.requireNonNull(asOf, "asOf must not be null");
        return loans.stream().filter(loan -> loan.isOverdue(asOf)).toList();
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

    /**
     * The library's current date. Exposed so reports can date themselves from the same clock
     * the loans used.
     */
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public void printReport() {
        printReport(false);
    }

    public void printReport(boolean showOverdue) {
        reportPrinter.print(this, showOverdue, out);
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
