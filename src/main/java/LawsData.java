import java.util.ArrayList;
import java.util.List;

public class LawsData {
    private static LawsData instance = null;
    public ArrayList<Law> lawsList;

    private LawsData() {
        ArrayList<Law> laws = LawScraper.scrapeLaws();
        LawScraper.scrapeLawsWithVotes(laws);
        this.lawsList = laws;
    }

    public static LawsData getInstance() {
        if (instance == null) {
            System.out.println("Creating LawsData instance");
            instance = new LawsData();
            System.out.println("Created LawsData instance");
        }
        return instance;
    }


}


