package org.example.riskwarningsystembackend.service;

import jakarta.annotation.PostConstruct;
import org.example.riskwarningsystembackend.dto.monitoring.MonitorRiskIdentificationResult;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.example.riskwarningsystembackend.entity.ProductNode;
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
 * 网络信息监测服务，用于定时爬取指定网站的新闻信息，进行风险识别并存入数据库。
 */
@Service
public class MonitorNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorNetworkService.class);
    private final MonitoringArticleRepository monitoringArticleRepository;
    private final MonitorRiskIdentificationService monitorRiskIdentificationService;

    // 定义三个网站的爬取配置
    private static final ScrapeConfig BJX_CONFIG = new ScrapeConfig(
            "北极星风力发电网",
            "https://fd.bjx.com.cn/yw/",
            "div.cc-list-content ul li",
            "a",
            "span",
            "div.cc-article",
            "yyyy-MM-dd",
            true
    );

    private static final ScrapeConfig CWEEA_CONFIG = new ScrapeConfig(
            "风能产业网",
            "https://www.cweea.com.cn/xwdt/hyyw/",
            "ul.cementlist.newslist li",
            "a",
            "span",
            "div.info_con.news_info div.info",
            "MM-dd",
            false
    );

    private static final ScrapeConfig IN_EN_CONFIG = new ScrapeConfig(
            "国际风电网",
            "https://wind.in-en.com/windnews/",
            "ul.infoList li",
            "div.listTxt h5 a",
            "div.prompt i",
            "div#article",
            "yyyy-MM-dd",
            true
    );

    /**
     * 构造函数，注入所需的服务实例。
     *
     * @param monitoringArticleRepository 监测文章数据访问接口
     * @param monitorRiskIdentificationService 风险识别服务
     */
    public MonitorNetworkService(MonitoringArticleRepository monitoringArticleRepository, MonitorRiskIdentificationService monitorRiskIdentificationService) {
        this.monitoringArticleRepository = monitoringArticleRepository;
        this.monitorRiskIdentificationService = monitorRiskIdentificationService;
    }

    /**
     * 爬取规则的配置类
     */
    private record ScrapeConfig(
            String sourceName,
            String listUrl,
            String itemSelector,
            String linkSelector,
            String dateSelector,
            String contentSelector,
            String datePattern,
            boolean needsSslBypass
    ) {}

    /**
     * 服务启动时，执行所有站点的初始爬取任务。
     */
    @PostConstruct
    @Transactional
    public void runInitialScrapeTasks() {
        logger.info("执行所有站点的启动时爬取任务...");
        scrapeSite(BJX_CONFIG);
        scrapeSite(CWEEA_CONFIG);
        scrapeSite(IN_EN_CONFIG);
        logger.info("所有站点的启动时爬取任务执行完毕。");
    }

    /**
     * 定时爬取北极星风力发电网新闻
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scrapeBjxNews() {
        scrapeSite(BJX_CONFIG);
    }

    /**
     * 定时爬取风能产业网新闻
     */
    @Scheduled(cron = "0 5 2 * * ?")
    @Transactional
    public void scrapeCweeaNews() {
        scrapeSite(CWEEA_CONFIG);
    }

    /**
     * 定时爬取国际风电网新闻
     */
    @Scheduled(cron = "0 10 2 * * ?")
    @Transactional
    public void scrapeInEnNews() {
        scrapeSite(IN_EN_CONFIG);
    }

    /**
     * 通用的网站爬取方法
     *
     * @param config 网站的爬取规则配置
     */
    private void scrapeSite(ScrapeConfig config) {
        logger.info("开始执行【{}】网络信息爬取和风险识别任务...", config.sourceName());
        try {
            // 根据配置决定是否需要绕过SSL验证
            SSLContext sslContext = config.needsSslBypass() ? createTrustAllSslContext() : SSLContext.getDefault();

            // 连接网站并获取页面内容
            Document doc = Jsoup.connect(config.listUrl())
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .get();
            Elements newsItems = doc.select(config.itemSelector());

            // 遍历新闻列表项
            for (Element item : newsItems) {
                Element link = item.select(config.linkSelector()).first();
                Element dateElement = item.select(config.dateSelector()).first();

                // 检查必要元素是否存在
                if (link == null || dateElement == null) {
                    logger.warn("【{}】跳过不完整的列表项: {}", config.sourceName(), item.text());
                    continue;
                }

                // 提取文章链接、标题和日期
                String articleUrl = link.absUrl("href");
                String title = link.attr("title").isEmpty() ? link.text() : link.attr("title");
                String dateString = dateElement.text();

                // 检查文章信息是否完整
                if (articleUrl.isEmpty() || title.isEmpty() || dateString.isEmpty()) {
                    logger.warn("【{}】文章信息不完整，跳过处理: {}", config.sourceName(), title);
                    continue;
                }

                // 解析文章发布日期
                LocalDate publishDate = parseDate(dateString, config.datePattern());
                if (publishDate == null) {
                    logger.warn("【{}】日期解析失败: {}, 文章: {}", config.sourceName(), dateString, title);
                    continue;
                }

                // 检查文章是否已存在
                if (monitoringArticleRepository.existsByUrl(articleUrl)) {
                    logger.debug("【{}】文章已存在，跳过: {}", config.sourceName(), articleUrl);
                    continue;
                }

                logger.info("【{}】正在爬取文章: {}", config.sourceName(), articleUrl);
                // 获取文章详细内容
                Document articleDoc = Jsoup.connect(articleUrl)
                        .sslSocketFactory(sslContext.getSocketFactory())
                        .get();

                // 提取关键词和文章内容
                String keywordsMeta = articleDoc.select("meta[name=Keywords]").attr("content");
                Element articleContentElement = articleDoc.select(config.contentSelector()).first();
                String articleContentText = articleContentElement != null ? articleContentElement.text() : "";
                String articleContentHtml = articleContentElement != null ? articleContentElement.html() : "";

                // 检查文章内容是否为空
                if (articleContentText.isEmpty()) {
                    logger.warn("【{}】文章内容为空，跳过风险分析: {}", config.sourceName(), title);
                    continue;
                }

                // 提取文章首张图片
                String imageUrl = extractFirstImage(articleDoc, config.contentSelector());

                // 创建监测文章对象并填充基本信息
                MonitoringArticle article = new MonitoringArticle();
                article.setTitle(title);
                article.setDate(publishDate);
                article.setUrl(articleUrl);
                article.setAuthor(config.sourceName());
                article.setContent(articleContentHtml);
                article.setImage(imageUrl);

                // 执行风险分析并保存文章
                performRiskAnalysisAndSave(article, articleContentText, keywordsMeta);
            }
        } catch (Exception e) {
            logger.error("【{}】任务执行失败", config.sourceName(), e);
        }
        logger.info("【{}】网络信息爬取和风险识别任务执行完毕。", config.sourceName());
    }

    /**
     * 对文章进行风险分析并保存到数据库
     *
     * @param article 待处理的文章对象
     * @param contentText 文章内容文本
     * @param keywords 文章关键词
     */
    private void performRiskAnalysisAndSave(MonitoringArticle article, String contentText, String keywords) {
        // 调用风险识别服务进行风险分析
        MonitorRiskIdentificationResult riskResult = monitorRiskIdentificationService.identifyRisk(contentText);

        // 根据风险识别结果设置文章类型和相关信息
        if (riskResult.isRisk()) {
            logger.info("发现风险文章: {}", article.getTitle());
            article.setType("risk");
            article.setRiskSource(String.join(", ", riskResult.getMatchedRiskKeywords()));
            article.setRelatedCompany(riskResult.getMatchedCompanies().stream().map(CompanyInfo::getName).collect(Collectors.joining(", ")));
            article.setRelatedProduct(riskResult.getMatchedProducts().stream().map(ProductNode::getName).collect(Collectors.joining(", ")));
        } else {
            article.setType("news");
        }

        // 处理文章标签
        if (keywords != null && !keywords.isEmpty()) {
            List<String> tags = Arrays.stream(keywords.split("[，,]"))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
            article.setTags(tags);
        } else {
            article.setTags(Collections.emptyList());
        }

        // 保存文章到数据库
        monitoringArticleRepository.save(article);
        logger.info("成功保存文章: {} (类型: {})", article.getTitle(), article.getType());
    }

    /**
     * 解析日期字符串为LocalDate对象
     *
     * @param dateString 日期字符串
     * @param pattern 日期格式模式
     * @return 解析后的LocalDate对象，解析失败返回null
     */
    private LocalDate parseDate(String dateString, String pattern) {
        try {
            // 特殊处理MM-dd格式的日期，补全年份
            if ("MM-dd".equals(pattern)) {
                return LocalDate.parse(LocalDate.now().getYear() + "-" + dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            // 处理相对时间格式（如"2小时前"）
            if (dateString.contains("小时前") || dateString.contains("分钟前")) {
                return LocalDate.now();
            }
            // 按指定格式解析日期
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 从文章内容中提取第一张图片的URL
     *
     * @param doc 文章DOM文档
     * @param contentSelector 内容选择器
     * @return 图片URL，未找到返回null
     */
    private String extractFirstImage(Document doc, String contentSelector) {
        try {
            Element articleElement = doc.select(contentSelector).first();
            if (articleElement != null) {
                Element firstImage = articleElement.select("img").first();
                if (firstImage != null) {
                    return firstImage.absUrl("src");
                }
            }
        } catch (Exception e) {
            logger.warn("提取文章图片时出错", e);
        }
        return null;
    }

    /**
     * 创建信任所有证书的SSL上下文，用于绕过SSL验证
     *
     * @return SSLContext对象
     * @throws NoSuchAlgorithmException 当指定的算法不存在时抛出
     * @throws KeyManagementException 当密钥管理出现问题时抛出
     */
    private SSLContext createTrustAllSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        // 创建信任所有证书的TrustManager
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        // 初始化SSL上下文
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }
}
