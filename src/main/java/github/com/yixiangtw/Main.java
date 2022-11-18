package github.com.yixiangtw;

public class Main {
    public static void main(String[] args) {
        ClawlerDao dao = new MyBatisCrawlerDao();
        for(int i = 0; i < 9; i++) {
            new Crawler(dao).start();
        }
    }
}

// 数据库是线程安全的