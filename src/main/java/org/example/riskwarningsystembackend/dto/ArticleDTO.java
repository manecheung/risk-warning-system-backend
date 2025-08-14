package org.example.riskwarningsystembackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating and updating monitoring articles.
 */
@Data
public class ArticleDTO {

    @NotBlank(message = "文章类型不能为空")
    private String type;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 512, message = "标题长度不能超过512个字符")
    private String title;

    private String author;

    @NotNull(message = "发布日期不能为空")
    private LocalDate date;

    private String image;

    @NotEmpty(message = "文章至少需要一个标签")
    private List<String> tags;

    // Risk-specific fields, can be null for news
    private String riskSource;
    private String notice;
    private String relatedCompany;
    private String relatedProduct;

    @NotBlank(message = "文章内容不能为空")
    private String content;
}
