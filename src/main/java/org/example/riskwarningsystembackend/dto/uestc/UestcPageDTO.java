package org.example.riskwarningsystembackend.dto.uestc;

import lombok.Data;

import java.util.List;

@Data
public class UestcPageDTO<T> {
    private Integer total;
    private Integer current;
    private Integer pages;
    private Integer size;
    private List<T> records;
}
