package github.com.yixiangtw;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

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

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:///Users/yixiang.liu/IdeaProjects/crawler/news", USERNAME, PASSWORD);
        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {

            if (isLinkProcessed(connection, link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                if (link.startsWith("//")) {
                    link = "https:" + link;
                }
                Document doc = httpGetAndParseHtml(link);
                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);
                storeIntoDataBaseIfItisNewsPage(doc);
                updateDatabase(connection, "insert into LINKS_ALREADY_PROCESSED (link) values(?)", link);
            }
        }

    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            updateDatabase(connection, "insert into LINKS_TO_BE_PROCESSED (link) values(?)", href);
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static void updateDatabase(Connection connection, String sql, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static String getNextLink(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }
    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection, "select link from LINKS_TO_BE_PROCESSED limit 1");
        if(link != null) {
            updateDatabase(connection, "delete from LINKS_TO_BE_PROCESSED where link = ?", link);
        }
        return link;
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
