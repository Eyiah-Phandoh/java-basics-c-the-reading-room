import com.amalitech.training.readingroom.ItemType;
import com.amalitech.training.readingroom.ItemUnavailableException;
import com.amalitech.training.readingroom.LendingItem;
import com.amalitech.training.readingroom.Library;
import com.amalitech.training.readingroom.Member;
import com.amalitech.training.readingroom.MemberKind;

import java.util.Arrays;

// The Reading Room - community library lending tracker
// Run it first to see the sort of thing they are worried about, and look closely
// at the items and active loans in the end-of-day report.

public class LendingTracker {

    public static void main(String[] args) {
        Library lib = new Library("The Reading Room");

        LendingItem novel = lib.addItem("Things Fall Apart", ItemType.BOOK);
        LendingItem audioCourse = lib.addItem("French Audio Course", ItemType.AUDIO_KIT);

        Member kofi = lib.addMember("Kofi Mensah", MemberKind.STANDARD);
        Member ama = lib.addMember("Ama Owusu", MemberKind.STUDENT);

        // a normal afternoon
        lib.borrow(kofi, novel);
        lib.borrow(ama, audioCourse);
        lib.returnItem(audioCourse);

        // ... and the sort of thing that worries them:
        try {
            lib.borrow(ama, novel); // the novel is already out with Kofi
        } catch (ItemUnavailableException e) {
            System.out.println(e.getMessage());
        }

        boolean showOverdue = Arrays.asList(args).contains("showOverdue=true");
        lib.printReport(showOverdue);
    }
}
