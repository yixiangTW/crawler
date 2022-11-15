package github.com.yixiangtw;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDAO implements ClawlerDao {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private final Connection connection;
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDAO() {
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:///Users/yixiang.liu/IdeaProjects/crawler/news", USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextLink(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }
    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink("select link from LINKS_TO_BE_PROCESSED limit 1");
        if (link != null) {
            updateDatabase("delete from LINKS_TO_BE_PROCESSED where link = ?", link);
        }
        return link;
    }
    public void updateDatabase(String sql, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into news(title, content, url, CREATED_TIME, MODIFIED_TIME) values(?,?,?,now(),now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, link);
            statement.executeUpdate();
        }
    }
    public boolean isLinkProcessed(String link) throws SQLException {
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
}
