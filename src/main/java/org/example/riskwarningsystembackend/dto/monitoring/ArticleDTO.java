package org.example.riskwarningsystembackend.dto.monitoring;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating and updating monitoring articles.
 * 用于创建和更新监控文章的数据传输对象
 */
@Data
public class ArticleDTO {

    /**
     * 文章类型
     * 不能为空
     */
    @NotBlank(message = "文章类型不能为空")
    private String type;

    /**
     * 文章标题
     * 不能为空，长度不能超过512个字符
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 512, message = "标题长度不能超过512个字符")
    private String title;

    /**
     * 文章作者
     */
    private String author;

    /**
     * 发布日期
     * 不能为空
     */
    @NotNull(message = "发布日期不能为空")
    private LocalDate date;

    /**
     * 文章图片URL
     */
    private String image;

    /**
     * 文章标签列表
     * 至少需要一个标签
     */
    @NotEmpty(message = "文章至少需要一个标签")
    private List<String> tags;

    // 风险相关字段，新闻类文章可为空
    /**
     * 风险来源
     */
    private String riskSource;

    /**
     * 通知内容
     */
    private String notice;

    /**
     * 相关公司
     */
    private String relatedCompany;

    /**
     * 相关产品
     */
    private String relatedProduct;

    /**
     * 文章内容
     * 不能为空
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;
}

