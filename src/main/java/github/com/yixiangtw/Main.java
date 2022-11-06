package github.com.yixiangtw;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static boolean isInterestingLink(String link) {
        return (isIndexPage(link) || isNewsPage(link)) && isNotLoginPage(link);
    }

    public static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn");
    }

    public static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("login.sina.cn");
    }

    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        Set<String> processedLinks = new HashSet<>();
        linkPool.add("https://sina.cn");
        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }
            String link = linkPool.remove(linkPool.size() - 1);
            if (processedLinks.contains(link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                if (link.startsWith("//")) {
                    link = "https:" + link;
                }

                Document doc = httpGetAndParseHtml(link);
                doc.select("a").stream().map(aTag->aTag.attr("href")).forEach(linkPool::add);
                storeIntoDataBaseIfItisNewsPage(doc);
                processedLinks.add(link);

            }

        }

    }

    private static void storeIntoDataBaseIfItisNewsPage(Document doc) {
        Elements articles = doc.select("article");
        if (!articles.isEmpty()) {
            for (Element article : articles) {
                String title = article.child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(link);
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }
}
