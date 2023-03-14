import com.google.gson.JsonObject;
import com.microsoft.playwright.Locator;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

enum LawValidity {
    NOT_YET_IN_FORCE, IN_FORCE, CANCELLED, OLD, EXPIRED
}

public class Law {
    public String id;
    public String title;
    public String minister;
    public Date firstPublished;
    public Date lastAmended;
    public LawValidity validity;
    public VoteResult votes;
    public String voteId = null;

    public Law() {
        this.votes = null;
    }

    public Law(Locator locator) {
        this();

        this.id = UriComponentsBuilder.fromUriString(locator.locator("td:nth-child(2) a").getAttribute("href")).build().getQueryParams().getFirst("lawitemid");
        this.title = locator.locator("td:nth-child(2)").innerText();
        this.minister = locator.locator("td:nth-child(3)").innerText();
        String validity = locator.locator("td:nth-child(4)").innerText();

        LawValidity lawValidity;
        switch (validity) {
            case "בתוקף":
            default:
                lawValidity = LawValidity.IN_FORCE;
                break;
            case "בטל":
                lawValidity = LawValidity.CANCELLED;
                break;
            case "פקע":
                lawValidity = LawValidity.EXPIRED;
                break;
            case "נושן":
                lawValidity = LawValidity.OLD;
                break;
            case "טרם נכנס לתוקף":
                lawValidity = LawValidity.NOT_YET_IN_FORCE;
                break;
        }
        this.validity = lawValidity;


        try {
            this.firstPublished =  new SimpleDateFormat("dd/MM/yyyy").parse(locator.locator("td:nth-child(6)").innerText());
            this.lastAmended = new SimpleDateFormat("dd/MM/yyyy").parse(locator.locator("td:nth-child(5)").innerText());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Law law = (Law) o;
        return id == law.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Law{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", minister='" + minister + '\'' +
                ", firstPublished=" + firstPublished +
                ", lastAmended=" + lastAmended +
                ", validity=" + validity +
                ", votes=" + votes +
                '}';
    }
}
