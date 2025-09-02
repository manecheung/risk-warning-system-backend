package org.example.riskwarningsystembackend.dto.uestc;

import lombok.Data;

import java.util.List;

/**
 * 电子科技大学分页数据DTO
 * 用于封装分页查询结果，包含分页信息和记录列表
 *
 * @param <T> 记录数据类型
 */
@Data
public class UestcPageDTO<T> {
    private Integer total;
    private Integer current;
    private Integer pages;
    private Integer size;
    private List<T> records;
}
