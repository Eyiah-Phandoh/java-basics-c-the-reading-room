import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LendingTrackerTest {

    @Test
    void demoRefusesSecondLoanAndPrintsTheExpectedReport() {
        String output = runDemo();

        String nl = System.lineSeparator();
        String today = LocalDate.now().toString();
        String expected = "Lent \"Things Fall Apart\" to Kofi Mensah for 14 days" + nl
                + "Lent \"French Audio Course\" to Ama Owusu for 14 days" + nl
                + "Returned \"French Audio Course\"" + nl
                + "Cannot lend \"Things Fall Apart\": already on loan to Kofi Mensah" + nl
                + nl
                + "=== The Reading Room - end of day ===" + nl
                + "Items:" + nl
                + "  \"Things Fall Apart\" (BOOK): ON_LOAN - with Kofi Mensah" + nl
                + "  \"French Audio Course\" (AUDIO_KIT): AVAILABLE" + nl
                + "Active loans:" + nl
                + "  Loan #1: \"Things Fall Apart\" -> Kofi Mensah" + nl
                + nl
                + "=== The Reading Room - overdue items (as of " + today + ") ===" + nl
                + "No overdue loans." + nl;

        assertEquals(expected, output);
    }

    private String runDemo() {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try (PrintStream replacement = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(replacement);
            try {
                LendingTracker.main(new String[0]);
            } finally {
                System.setOut(original);
            }
        }

        return captured.toString(StandardCharsets.UTF_8);
    }
}
