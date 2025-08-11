package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
import org.example.riskwarningsystembackend.dto.monitoring.ArticleDetailDto;
import org.example.riskwarningsystembackend.dto.monitoring.ArticleRecordDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    private final List<ArticleDetailDto> allArticles = Arrays.asList(
            new ArticleDetailDto(1, "news", "这是一个非常非常长的标题，用于专门测试当标题内容超出容器宽度时，是否能够正确地显示横向滚动条而不是简单地截断文本或者破坏布局。", "北极星风力发电网", "2024.11.12", "/风电.svg", Arrays.asList("运维", "人才"), null, null, null, null, "新闻正文内容..."),
            new ArticleDetailDto(2, "news", "风电“抢装潮”退潮！华东勘测设计院发布5份行政处罚决定书", "北极星风力发电网", "2024.11.12", "/法规.svg", Arrays.asList("法规", "处罚决定书"), null, null, null, null, "新闻正文内容..."),
            new ArticleDetailDto(3, "news", "新能源汽车下乡政策再加码，充电桩建设成关键", "第一财经", "2024.11.11", "/法规.svg", Arrays.asList("政策", "汽车"), null, null, null, null, "新闻正文内容..."),
            new ArticleDetailDto(4, "risk", "漳州帆船配舾工程有限公司员工坠亡", "北极星风力发电网", "2024.11.12", "/风险.svg", Arrays.asList("事故", "安全"), "人员坠落, 抢救无效死亡", "《通知》显示，2024年9月4日3时10分许...", "漳州帆船配舾工程有限公司", "船舵总筒", "<h4>事故背景</h4><p>近期，安全生产监督管理部门发布了一则关于高处作业安全的紧急通报...</p>"),
            new ArticleDetailDto(5, "risk", "某上市公司财务造假被证监会立案调查，股价连续跌停引发市场恐慌，这是一个为了测试而设置的非常长的风险新闻标题", "证券时报", "2024.11.10", "/风险.svg", Arrays.asList("财务风险", "调查"), "财务造假", "证监会立案调查", "某上市公司", "某产品", "风险详情正文..."),
            new ArticleDetailDto(6, "risk", "供应链中断，某手机厂商新款发布或将延迟", "供应链前沿", "2024.11.09", "/风险.svg", Arrays.asList("供应链", "中断"), "供应链中断", "官方公告", "某手机厂商", "新款手机", "风险详情正文..."),
            new ArticleDetailDto(7, "news", "光伏产业迎来新一轮技术迭代，N型电池成市场主流", "光伏资讯", "2024.11.08", "/法规.svg", Arrays.asList("技术", "光伏"), null, null, null, null, "新闻正文内容..."),
            new ArticleDetailDto(8, "news", "“东数西算”工程全面启动，数据中心建设提速", "人民邮电报", "2024.11.07", "/法规.svg", Arrays.asList("新基建", "数据中心"), null, null, null, null, "新闻正文内容..."),
            new ArticleDetailDto(9, "risk", "数据安全漏洞曝光，知名社交平台用户隐私面临威胁", "网络安全观察", "2024.11.06", "/风险.svg", Arrays.asList("数据安全", "隐私泄露"), "数据泄露", "安全通报", "知名社交平台", "用户数据", "风险详情正文..."),
            new ArticleDetailDto(10, "risk", "环保审查趋严，某化工企业因排污超标被责令停产整顿", "环保在线", "2024.11.05", "/风险.svg", Arrays.asList("环保", "监管"), "排污超标", "环保部门通告", "某化工企业", "化工产品", "风险详情正文...")
    );

    public PaginatedResponseDto<ArticleRecordDto> getArticles(int page, int pageSize, String type, String keyword) {
        List<ArticleDetailDto> filteredArticles = allArticles.stream()
                .filter(article -> (type == null || type.isEmpty() || article.getType().equalsIgnoreCase(type)))
                .filter(article -> (keyword == null || keyword.isEmpty() || article.getTitle().contains(keyword) || article.getTags().contains(keyword)))
                .toList();

        List<ArticleRecordDto> records = filteredArticles.stream()
                .map(this::convertToRecordDto)
                .collect(Collectors.toList());

        int totalRecords = records.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, totalRecords);

        List<ArticleRecordDto> pageRecords = records.subList(start, end);

        return new PaginatedResponseDto<>(
                page,
                pageSize,
                totalRecords,
                totalPages,
                page > 1,
                page < totalPages,
                pageRecords
        );
    }

    public Optional<ArticleDetailDto> getArticleById(int id) {
        return allArticles.stream().filter(article -> article.getId() == id).findFirst();
    }

    private ArticleRecordDto convertToRecordDto(ArticleDetailDto detailDto) {
        return new ArticleRecordDto(
                detailDto.getId(),
                detailDto.getType(),
                detailDto.getTitle(),
                detailDto.getAuthor(),
                detailDto.getDate(),
                detailDto.getImage(),
                detailDto.getTags()
        );
    }
}
