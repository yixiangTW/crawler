package github.com.yixiangtw;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    private static void mockData(SqlSessionFactory sqlSessionFactory, int howMany) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> currentNews = session.selectList("github.com.yixiangtw.MockMapper.selectNews");
            System.out.println("");
            int count = howMany - currentNews.size();
            Random random = new Random();
            try {
                while(count -- > 0) {
                    News news = currentNews.get(random.nextInt(currentNews.size()));
                    Instant currentTime = news.getCreatedTime().minusSeconds(random.nextInt(3600*24*365));
                    news.setCreatedTime(currentTime);
                    news.setModifiedTime(currentTime);
                    session.insert("github.com.yixiangtw.MockMapper.insertNews", news);
                    System.out.println("Left" + count);
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            mockData(sqlSessionFactory, 1000000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
