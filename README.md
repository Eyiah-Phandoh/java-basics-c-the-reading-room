# The Reading Room — Community Library Lending Tracker

A rebuilt version of the community library lending tracker. It preserves the original loan
periods, console messages, ordering, and end-of-day report, while enforcing the rule the
business depends on: **a physical item can only be on one loan at a time**.

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

## Tests

```bash
./mvnw test
```

The suite covers every loan period, duplicate-title copies, returns by identity, refusal of
a second loan, rejection of unregistered or invalid entities, read-only views, and the exact
report format.

## Structure

| Type | Responsibility |
|---|---|
| `LendingTracker` | Thin entry point that runs the demo and reports an expected refusal. |
| `Library` | Aggregate root: owns items, members, loans, IDs, and the one-active-loan-per-item map. |
| `LendingItem` | An immutable item identity: id, title, type. It stores no custody flag. |
| `Member` | A member identity, kind, and read-only loan history. |
| `Loan` | A loan linking a member and an item, with its period and status. |
| `ItemType`, `MemberKind`, `LoanStatus` | Closed value sets replacing raw strings. |
| `LoanPolicy` | The four loan-period rules in one place. |
| `ReportPrinter` | Renders the end-of-day report from the library state. |
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
the policy holds all four periods in one place. A new type still requires a code change, but
the compiler refuses to build until its rule is supplied; a data-driven rule table would be
more machinery than four rules warrant.

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

## The change that is coming

The brief's next change is a new item type (a new member kind follows the same route).

In the rebuilt version, adding it is three deliberate steps:

1. Add the constant to `ItemType`.
2. Add its loan period to `LoanPolicy`. The nested `switch` is exhaustive, so the project
   will not compile until the new rule exists.
3. Add the period to `LoanPolicyTest`.

Nothing else needs to change. `Library` asks the policy for the number of days and never
branches on type. `Loan`, `Member`, and `ReportPrinter` use the enum generically, so the
report shows the new type automatically. A new member kind is the same: one enum constant,
one policy case, one test.

The old version forced changes in several places:

- `LendingItem`'s allowed-values comment and constructor data;
- a new copy of the `if/else` block in `Library.borrow`, including its separate student-rate
  branch and the status/borrower writes;
- the demo setup;
- and there was no compiler check or test to catch a mistake.

A typo in the type string produced an active loan with 0 days while leaving the item
`AVAILABLE`; a forgotten student branch silently used the standard period. Adding a member
kind was worse: both the `BOOK` and `AUDIO_KIT` branches in `Library.borrow` needed editing.

The rebuild does not remove the need to decide the new period. It makes the compiler and
tests point at that one decision, and keeps `Library`, the entities, and the report from
drifting while it is made.

## Other extension points

- **New report section:** change `ReportPrinter` only; domain rules are untouched.
- **New lending rule:** keep it inside `Library` (or a small policy type) and cover it with
  a test.
- **New storage or UI:** add it around `Library`; the domain does not depend on the console.

Out of scope for this rebuild: persistence, due dates, fines, concurrency, and borrowing
limits. The current program is intentionally an in-memory, single-run tracker.
