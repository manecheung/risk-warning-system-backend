package org.example.riskwarningsystembackend.dto.monitoring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRecordDto {
    private int id;
    private String type;
    private String title;
    private String author;
    private String date;
    private String image;
    private List<String> tags;
}
