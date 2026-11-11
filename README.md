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
javac -d target/classes $(find src/main/java -name '*.java')
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

## Extending it

- **New item type:** add a constant to `ItemType` and a branch to `LoanPolicy`. The
  exhaustive `switch` will fail to compile until the rule is supplied.
- **New member kind:** the same pattern in `MemberKind` and `LoanPolicy`.
- **New report section:** change `ReportPrinter` only; domain rules are untouched.
- **New lending rule:** keep it inside `Library` (or a small policy type) and cover it with
  a test.

Out of scope for this rebuild: persistence, due dates, fines, concurrency, and borrowing
limits. The current program is intentionally an in-memory, single-run tracker.
