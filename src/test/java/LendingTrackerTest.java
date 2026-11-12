import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LendingTrackerTest {

    @Test
    void demoRefusesSecondLoanAndPrintsTheExpectedReport() {
        String output = runDemo();

        String nl = System.lineSeparator();
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
                + "  Loan #1: \"Things Fall Apart\" -> Kofi Mensah" + nl;

        assertEquals(expected, output);
    }

    @Test
    void showOverdueArgumentAddsTheOverdueSection() {
        String output = runDemo("showOverdue=true");

        assertTrue(output.contains("=== The Reading Room - overdue items (as of "));
        assertTrue(output.contains("No overdue loans."));
    }

    private String runDemo(String... args) {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try (PrintStream replacement = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(replacement);
            try {
                LendingTracker.main(args);
            } finally {
                System.setOut(original);
            }
        }

        return captured.toString(StandardCharsets.UTF_8);
    }
}