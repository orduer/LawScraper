import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
@JsonIgnoreProperties(ignoreUnknown = true)
public class VoteResult {
    @JsonProperty("VoteHeader")
    public ArrayList<VoteHeader> voteHeader;
    @JsonProperty("VoteDetails")
    public ArrayList<VoteDetail> voteDetails;
    @JsonProperty("DescreetVoteResults")
    public ArrayList<VoteDetail> descreetVoteResults;
    @JsonProperty("HandsWithoutCountersAccepted")
    public ArrayList<VoteDetail> handsWithoutCountersAccepted;
}


