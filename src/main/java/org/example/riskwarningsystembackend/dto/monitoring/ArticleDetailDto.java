package org.example.riskwarningsystembackend.dto.monitoring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailDto {
    private int id;
    private String type;
    private String title;
    private String author;
    private String date;
    private String image;
    private List<String> tags;
    private String riskSource;
    private String notice;
    private String relatedCompany;
    private String relatedProduct;
    private String content;
}
