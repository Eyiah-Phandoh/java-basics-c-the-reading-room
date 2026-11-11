import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LendingTrackerTest {

    @Test
    void demoRefusesSecondLoanAndPrintsTheExpectedReport() throws Exception {
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

        assertEquals(expected, captured.toString(StandardCharsets.UTF_8));
    }
}
