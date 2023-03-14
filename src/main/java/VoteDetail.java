import com.fasterxml.jackson.annotation.JsonProperty;

public class VoteDetail{
    @JsonProperty("MkName")
    public String voterName;
    @JsonProperty("FactionName")
    public String voterParty;
    @JsonProperty("VoteResultId")
    public int voteResultId;
    @JsonProperty("Title")
    public String voteValue;
}