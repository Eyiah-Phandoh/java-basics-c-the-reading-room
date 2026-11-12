# The Reading Room — Community Library Lending Tracker

A rebuilt version of the community library lending tracker. It preserves the original loan
periods, console messages, and ordering, while enforcing the rule the business depends on:
**a physical item can only be on one loan at a time**. The end-of-day report keeps its existing sections. An optional overdue section is added
when the tracker is run with `showOverdue=true`.

The defects found in the starter and the reasoning behind the new structure are recorded in
[`DIAGNOSIS.md`](DIAGNOSIS.md).

## Running it

You need JDK 21. The Maven wrapper is included, so Maven does not need to be installed.

```bash
./mvnw compile
./mvnw exec:java
```

Or build an executable jar:

```bash
./mvnw package
java -jar target/the-reading-room.jar
```

Or use the JDK directly:

```bash
javac -sourcepath src/main/java -d target/classes src/main/java/LendingTracker.java
java -cp target/classes LendingTracker
```

## Expected output

```text
Lent "Things Fall Apart" to Kofi Mensah for 14 days
Lent "French Audio Course" to Ama Owusu for 14 days
Returned "French Audio Course"
Cannot lend "Things Fall Apart": already on loan to Kofi Mensah

=== The Reading Room - end of day ===
Items:
  "Things Fall Apart" (BOOK): ON_LOAN - with Kofi Mensah
  "French Audio Course" (AUDIO_KIT): AVAILABLE
Active loans:
  Loan #1: "Things Fall Apart" -> Kofi Mensah
```

The demo's last action is deliberately invalid. It used to create a second active loan for
the same novel. It is now refused, and the report correctly keeps the novel with Kofi.

### Running with overdue tracking

```bash
./mvnw exec:java -Dexec.args="showOverdue=true"
```

The demo then adds one extra loan, back-dated 30 days, and the report ends with the overdue
section (dates depend on the day you run it):

```text
=== The Reading Room - overdue items (as of 2026-11-12) ===
  Loan #3: "World Atlas" -> Kofi Mensah (due 2026-10-27, 16 days overdue)
```

## Tests

```bash
./mvnw test
```

The suite covers every loan period, duplicate-title copies, returns by identity, refusal of
a second loan, rejection of unregistered or invalid entities, read-only views, due dates and
overdue calculations, and the exact report formats.

## Structure

| Type | Responsibility |
|---|---|
| `LendingTracker` | Thin entry point that runs the demo and reports an expected refusal. |
| `Library` | Aggregate root: owns items, members, loans, IDs, and the one-active-loan-per-item map. |
| `LendingItem` | An immutable item identity: id, title, type. It stores no custody flag. |
| `Member` | A member identity, kind, and read-only loan history. |
| `Loan` | A loan linking a member and an item, with its period, status, borrow date, and due date. |
| `ItemType`, `MemberKind`, `LoanStatus` | Closed value sets replacing raw strings. |
| `LoanPolicy` | The loan-period rules in one place. |
| `ReportPrinter` | Renders the end-of-day report, with an optional overdue section. |
| `*Exception` | Clear refusals for unavailable items, invalid returns, and foreign entities. |

Design rules that keep it safe to change:

- `Library` is the only mutator. There are no public setters or leaked mutable collections.
- Availability is derived from the active-loan map, so item and loan state cannot disagree.
- `Loan` holds real `LendingItem` and `Member` references, never display strings.
- Validation happens before any state change; a rejected operation leaves the library
  untouched.
- IDs belong to a `Library` and start at 1, which keeps tests independent.

## Key design decisions

**`Library` owns custody instead of each item owning status flags.** The original stored
`status` and `currentBorrower` on the item *and* `status` on the loan, so the two records
could disagree. I kept one private `activeLoans` map and derive availability and the report
from it. The trade-off is one extra lookup when printing; the gain is that "one loan per
item" has a single place to be true.

**Loans point at the real member and item, rather than copying names and titles.** The
original matched returns by `itemTitle`, which made duplicate copies indistinguishable and
broke when a title changed. References are slightly more work to serialise later, but they
make identity explicit and make a return affect exactly one loan.

**Enums and a `LoanPolicy` instead of strings and nested `if`s.** Strings made typos
possible and allowed an unknown type to create a zero-day loan. The enums are closed, and
the policy holds every period in one place. A new type still requires a code change, but
the compiler refuses to build until its rule is supplied; a data-driven rule table would be
more machinery than the current rules warrant.

**Validate first, mutate second.** Every check runs before the library changes anything, so
a refused borrow or return leaves the records exactly as they were. The cost is that callers
must handle a domain exception; only the entry point has to do that for the expected demo
refusal.

**Library-owned IDs and read-only views instead of static counters and public lists.** IDs
are now local to a library and start at 1, so tests do not interfere with each other. Two
libraries may both have item 1, which is correct for a local identity. Returning
`List.copyOf(...)` costs a small copy, but no caller can mutate the library's records behind
its back.

**A separate `ReportPrinter`.** Report changes no longer risk touching lending rules, and
the exact console format can be locked by a golden-output test. The trade-off is one more
class and an injected `PrintStream`.

**One item object is one physical copy.** This preserves the existing model: two copies of
*Dune* are two `LendingItem` instances, so returning one cannot close the other's loan. A
quantity field would be a different model and would change how the report is read; it is
deliberately out of scope.

## The change that is coming: e-loans

The library plans to add **e-loans**: digital items with their own borrowing rules. The
tracker has to stay correct and grow to a new kind of item without a rewrite.

Adding e-loans is a contained change:

1. Add the constant to `ItemType`.
2. Add its loan periods to `LoanPolicy.daysFor`.
3. Add how many members may hold it at once to `LoanPolicy.maxConcurrentLoans`. Physical
   items return 1, and an e-loan can return more.
4. Add the rules to `LoanPolicyTest`.

Both `switch` expressions are exhaustive, so the project will not build until steps 2 and 3
are done. `Library` never branches on item type: it asks the policy how long a loan lasts and
how many holders are allowed. `ReportPrinter` lists every holder, and a return by one member
closes only that member's loan.

`LibraryTest.aPolicyThatAllowsSeveralHoldersLetsThemShareAnItem` is the evidence. With a
policy allowing two holders, two members share one item, a third is refused, the report shows
both holders, and one member's return leaves the other's loan open. Physical items are
unchanged: BOOK and AUDIO_KIT return 1, so the one-loan-at-a-time rule holds exactly as before.

The old version would have forced changes in several places:

- `LendingItem`'s allowed-values comment and constructor data;
- a new copy of the `if/else` block in `Library.borrow`, including its separate student-rate
  branch and the status/borrower writes;
- the demo setup;
- and there was no compiler check or test to catch a mistake.

A typo in the old type string produced an active loan with 0 days while leaving the item
`AVAILABLE`; a forgotten student branch silently used the standard period. Adding a kind of
item also meant working out which of the scattered status fields to update, with nothing to
warn you if you missed one.

The rebuild does not remove the need to decide what an e-loan's rules are. It puts that
decision in `ItemType` and `LoanPolicy`, lets the compiler and tests point at it, and keeps
`Library`, the entities, and the report from drifting while it is made.

## Improvement beyond the brief: overdue tracking

The brief only requires loan periods; it does not ask the tracker to know which loans are
late. That is the one substantive extra I added.

Every `Loan` now records `borrowedOn` and derives `dueDate` from its loan period.
`Library.overdueLoans(asOf)` returns the active loans past that date, and
`Library.borrow(member, item, borrowedOn)` lets staff enter a loan that started earlier. The
overdue section is **off by default**, so the standard report is unchanged. Run with
`showOverdue=true` to add it (see "Running with overdue tracking" above).

**Why it is worth adding:** the original stored only `loanDays`, so it could never say when
anything was due. With dates in the model, the report can name late loans, and later work
(reminders, fines, automatic expiry for e-loans) has a due date to build on.

**Limits today:** the tracker is in-memory and single-run, so loans are lost when it exits.
Persistence is the natural next step.

**Why the implementation is safe:** `Library` takes an injected `Clock`, so due-date logic
is tested deterministically instead of depending on the wall clock. The overdue query is a
read-only projection of the same active loans, so it cannot drift from custody. The overdue
section is rendered by `ReportPrinter` from that query, so the console format stays out of
the lending aggregate.

**For the next person:** fines, reminders, or a "due this week" view can be built on
`Loan.dueDate()` and `Library.overdueLoans(...)` without changing the lending rules. A new
item type automatically inherits due-date support because the period still comes from
`LoanPolicy`.

