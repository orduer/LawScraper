import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

public class LawScraper {
    private static final String lawsUrl = "https://main.knesset.gov.il/Activity/Legislation/Laws/Pages/LawLaws.aspx?t=lawlaws&st=lawlaws&fd=01/01/2018%2000:00:00&td=14/03/2023%2000:00:00";

    private static BrowserType.LaunchOptions getLaunchOptions() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(false);
        launchOptions.args = new ArrayList<>();

        // playwright chromium args to hide from bot detection, nice try knesset
        launchOptions.args.add("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36");
        launchOptions.args.add("--disable-popup-blocking");
        launchOptions.args.add("--disable-blink-features=AutomationControlled");
        launchOptions.args.add("--disable-features=CrossSiteDocumentBlockingIfIsolating,CrossSiteDocumentBlockingAlways,IsolateOrigins,site-per-process");

        return launchOptions;
    }

    public static ArrayList<Law> scrapeLaws() {
        ArrayList<Law> laws = new ArrayList<>();
        boolean hasNextPage = true;

        try (Playwright playwright = Playwright.create()) {
            BrowserType chromium = playwright.chromium();
            Browser browser = chromium.launch(getLaunchOptions());
            Page page = browser.newPage();
            page.navigate(lawsUrl);

            while (hasNextPage) {
                page.waitForSelector(".rgRow");

                List<Locator> locators = page.locator(".rgRow").all();
                for (Locator locator : locators) {
                    laws.add(new Law(locator));
                }

                hasNextPage = page.locator("#ctl00_ctl61_g_10258392_438f_4341_ac25_276ee5f53aba_ctl00_aNextPage").getAttribute("disabled") == null;
                if (hasNextPage) {
                    page.locator("#ctl00_ctl61_g_10258392_438f_4341_ac25_276ee5f53aba_ctl00_aNextPage").click();
                }
            }

            page.close();
        }
        return laws;
    }

    private static final String voteSearchUrl = "https://knesset.gov.il/WebSiteApi/knessetapi/Votes/GetVotesHeaders";
    private static final String getVoteUrl = "https://www.knesset.gov.il/WebSiteApi/knessetapi/Votes/GetVoteDetails/";

    public static String scrapeLawVotes(Law law) {

        JsonObject json = new JsonObject();
        json.addProperty("SearchType", 2);
        json.addProperty("SessionTitle", law.title);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(voteSearchUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(s -> {
                    // jsonObject is an array of objects with a Table property. Table is an array of objects with a VoteId property.
                    // we want to get the VoteId property of an element with the exact same ItemTitle as the law, and the latest VoteDate.
                    JsonArray jsonArray;

                    try {
                        jsonArray = JsonParser.parseString(s).getAsJsonObject().get("Table").getAsJsonArray();
                    }
                    catch (Exception e) {
                        System.out.println("No votes found for law " + law.title);
                        return null;
                    }

                    String voteId = "";
                    Date voteDate = new Date(0);
                    for (JsonElement jsonElement : jsonArray) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject.get("ItemTitle").getAsString().equals(law.title)) {
                            try {
                                Date voteDateTmp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(jsonObject.get("VoteDate").getAsString());
                                if (voteDateTmp.after(voteDate)) {
                                    voteId = jsonObject.get("VoteId").getAsString();
                                    voteDate = voteDateTmp;
                                }
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }
                    law.voteId = voteId;
                    return voteId;
                })
                .join();

    }

    public static void getVotes(Law law) {
        // use the getVoteUrl to send an async get request to get the vote data. add the voteId to the end of the url.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getVoteUrl + law.voteId))
                .GET()
                .build();

        // send a get request to the getVoteUrl with the voteId, parse the json response and get the vote data.
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(s -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        VoteResult result = mapper.readValue(s, VoteResult.class);
                        law.votes = result;
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    return law.votes;
                })
                .join();
    }

    // get a list of law votes using async, based on a list of laws
    public static void scrapeLawsWithVotes(List<Law> laws) {
        Executor executor = Executors.newFixedThreadPool(10);

        List<CompletableFuture<String>> futures = laws.stream()
                .map(law -> CompletableFuture.supplyAsync(() -> scrapeLawVotes(law), executor))
                .collect(toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<String>> allPageContentsFuture = allFutures.thenApply(v ->
                futures.stream().
                        map(pageContentFuture -> pageContentFuture.join()).
                        collect(toList()));

        allPageContentsFuture.join();

        for (Law law : laws.stream().filter(law -> law.voteId != null).collect(toList())) {
            getVotes(law);
        }

    }
}
