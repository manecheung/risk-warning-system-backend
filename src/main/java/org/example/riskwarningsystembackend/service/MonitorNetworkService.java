package org.example.riskwarningsystembackend.service;

import jakarta.annotation.PostConstruct;
import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.example.riskwarningsystembackend.repository.MonitoringArticleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网络信息监测服务，用于定时爬取指定网站的新闻信息并存入数据库。
 */
@Service
public class MonitorNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorNetworkService.class);
    private static final String BASE_URL = "https://fd.bjx.com.cn/yw/";
    private final MonitoringArticleRepository monitoringArticleRepository;

    /**
     * 构造函数，注入 MonitoringArticleRepository 实例。
     *
     * @param monitoringArticleRepository 用于操作 MonitoringArticle 实体的数据访问层组件
     */
    public MonitorNetworkService(MonitoringArticleRepository monitoringArticleRepository) {
        this.monitoringArticleRepository = monitoringArticleRepository;
    }

    /**
     * 项目启动后立即执行一次爬取任务，之后每天凌晨2点执行。
     * Cron Expression: second, minute, hour, day of month, month, day(s) of week
     * "0 0 2 * * ?" = 每天凌晨2点
     */
    @PostConstruct
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scrapeAndSaveArticles() {
        logger.info("开始执行网络信息爬取任务...");
        try {
            // 创建信任所有证书的SSL上下文
            SSLContext sslContext = createTrustAllSslContext();

            // 1. 获取新闻列表页面
            Document doc = Jsoup.connect(BASE_URL)
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .get();
            Elements newsItems = doc.select("div.cc-list-content ul li");

            // 2. 遍历新闻列表
            for (Element item : newsItems) {
                Element link = item.select("a").first();
                if (link == null) {
                    continue;
                }

                String articleUrl = link.attr("href");
                String title = link.attr("title");

                Element spanElement = item.select("span").first();
                if (spanElement == null) {
                    continue;
                }

                String dateString = spanElement.text();

                // 检查必要字段是否为空或空白
                if (articleUrl.isEmpty() || title.isEmpty() || dateString.isEmpty()) {
                    logger.warn("文章信息不完整，跳过处理");
                    continue;
                }

                LocalDate publishDate;
                try {
                    publishDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e) {
                    logger.warn("日期解析失败: {}", dateString);
                    continue;
                }

                // 检查数据库中是否已存在相同标题和发布日期的文章，避免重复插入
                if (monitoringArticleRepository.findByTitleAndDate(title, publishDate).isPresent()) {
                    logger.info("文章已存在，跳过: {} - {}", title, publishDate);
                    continue;
                }

                // 3. 二次爬取文章详情页
                logger.info("正在爬取文章: {}", articleUrl);
                Document articleDoc = Jsoup.connect(articleUrl)
                        .sslSocketFactory(sslContext.getSocketFactory())
                        .get();

                // 4. 解析文章详情
                String keywords = articleDoc.select("meta[name=Keywords]").attr("content");
                Element articleContentElement = articleDoc.select("div.cc-article").first();
                String articleContent = articleContentElement != null ? articleContentElement.html() : "";

                // 提取第一张图片作为文章封面
                String imageUrl = extractFirstImage(articleDoc);

                // 5. 创建并保存实体
                MonitoringArticle article = new MonitoringArticle();
                article.setTitle(title);
                article.setDate(publishDate);
                article.setUrl(articleUrl);
                article.setType("news");
                article.setAuthor("北极星风力发电网");
                article.setContent(articleContent);
                article.setImage(imageUrl); // 设置文章封面图片

                // 处理关键字
                if (!keywords.isEmpty()) {
                    List<String> tags = Arrays.stream(keywords.split("[，,]"))
                            .map(String::trim)
                            .filter(tag -> !tag.isEmpty())
                            .collect(Collectors.toList());
                    article.setTags(tags);
                } else {
                    article.setTags(Collections.emptyList());
                }

                monitoringArticleRepository.save(article);
                logger.info("成功保存文章: {}", title);

            }
        } catch (IOException e) {
            logger.error("网络信息爬取任务失败", e);
        } catch (Exception e) {
            logger.error("SSL配置错误", e);
        }
        logger.info("网络信息爬取任务执行完毕。");
    }

    /**
     * 从文章中提取第一张图片的URL
     * @param doc 文章页面Document对象
     * @return 第一张图片的URL，如果没有找到则返回null
     */
    private String extractFirstImage(Document doc) {
        try {
            // 查找文章内容中的第一张图片
            Element articleElement = doc.select("div.cc-article").first();
            if (articleElement != null) {
                Element firstImage = articleElement.select("img").first();
                if (firstImage != null) {
                    String imageUrl = firstImage.attr("src");
                    // 如果是相对路径，转换为绝对路径
                    if (imageUrl.startsWith("//")) {
                        imageUrl = "https:" + imageUrl;
                    } else if (imageUrl.startsWith("/")) {
                        // 处理相对路径
                        imageUrl = "https://news.bjx.com.cn" + imageUrl;
                    }
                    return imageUrl;
                }
            }
        } catch (Exception e) {
            logger.warn("提取文章图片时出错", e);
        }
        return null;
    }

    /**
     * 创建一个信任所有证书的 SSL 上下文，用于绕过 HTTPS 证书验证。
     *
     * @return SSLContext 实例
     * @throws NoSuchAlgorithmException 当请求的算法不存在时抛出
     * @throws KeyManagementException 当密钥管理异常时抛出
     */
    private SSLContext createTrustAllSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }
}
