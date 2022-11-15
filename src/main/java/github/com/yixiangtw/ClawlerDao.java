package github.com.yixiangtw;

import java.sql.SQLException;

public interface ClawlerDao {
    String getNextLink(String sql) throws SQLException;
    String getNextLinkThenDelete() throws SQLException;
    void updateDatabase(String sql, String link) throws SQLException;
    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;
    boolean isLinkProcessed(String link) throws SQLException;
}
