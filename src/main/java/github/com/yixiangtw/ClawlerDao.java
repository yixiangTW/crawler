package github.com.yixiangtw;

import java.sql.SQLException;

public interface ClawlerDao {
    String getNextLinkThenDelete() throws SQLException;
    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;
    boolean isLinkProcessed(String link) throws SQLException;

    void insertLinkToBeProcessed(String href);

    void insertProcessedLink(String link);
}
