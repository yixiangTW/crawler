package github.com.yixiangtw;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements ClawlerDao{
    private SqlSessionFactory sqlSessionFactory;
    public MyBatisCrawlerDao() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public synchronized String getNextLinkThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne("github.com.yixiangtw.MyMapper.selectNextAvailableLink");
            if(url != null) {
                session.delete("github.com.yixiangtw.MyMapper.deleteLink", url);
            }
            return url;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("github.com.yixiangtw.MyMapper.insertNews", new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("github.com.yixiangtw.MyMapper.countLink", link);
            return count != 0;
        }
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "links_to_be_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("github.com.yixiangtw.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "links_already_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("github.com.yixiangtw.MyMapper.insertLink", param);
        }
    }
}
