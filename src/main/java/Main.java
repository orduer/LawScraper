import java.util.ArrayList;
public class Main {
    public static void main(String[] args) {
        LawsData laws = LawsData.getInstance();
        laws.lawsList.stream().forEach(law -> {
            System.out.println(law.title);
            law.votes.voteDetails.stream().forEach(vote -> {
                System.out.println(formatVoterResult(vote));
            });
        });
    }

    public static String formatVoterResult(VoteDetail vote){
        return vote.voterName + " " + vote.voterParty + " " + vote.voteValue;
    }
}



