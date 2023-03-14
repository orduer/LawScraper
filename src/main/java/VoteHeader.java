import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class VoteHeader{
    @JsonProperty("VoteId")
    public int voteId;
    @JsonProperty("VoteProtocolNo")
    public int voteProtocolNo;
    @JsonProperty("VoteDate")
    public Date voteDate;
    @JsonProperty("VoteDateStr")
    public String voteDateStr;
    @JsonProperty("VoteTimeStr")
    public String voteTimeStr;
    @JsonProperty("VoteType")
    public String voteType;
    @JsonProperty("VoteTypeId")
    public int voteTypeId;
    @JsonProperty("ItemTitle")
    public String itemTitle;
    @JsonProperty("FK_ItemID")
    public int fK_ItemID;
    @JsonProperty("FK_AssemblyID")
    public int fK_AssemblyID;
    @JsonProperty("LU_ItemType")
    public int lU_ItemType;
    @JsonProperty("SessionNumber")
    public int sessionNumber;
    @JsonProperty("FK_Knesset")
    public int fK_Knesset;
    @JsonProperty("KnessetName")
    public String knessetName;
    @JsonProperty("Decision")
    public String decision;
    @JsonProperty("ChairmanName")
    public String chairmanName;
    @JsonProperty("DescreetVoteType")
    public Object descreetVoteType;
    @JsonProperty("IsForAccepted")
    public boolean isForAccepted;
    @JsonProperty("AcceptedText")
    public String acceptedText;
}

