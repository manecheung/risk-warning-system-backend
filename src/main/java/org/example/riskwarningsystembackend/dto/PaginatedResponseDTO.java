package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应数据传输对象
 * 用于封装分页查询的结果信息
 * @param <T> 泛型类型，表示分页记录的数据类型
 */
@Data
@AllArgsConstructor
public class PaginatedResponseDTO<T> {
    private int page; // 当前页码
    private int pageSize; // 每页的记录数
    private long totalRecords; // 总记录数
    private int totalPages; // 总页数
    private boolean hasPrevPage; // 判断是否有上一页
    private boolean hasNextPage; // 判断是否有下一页
    private List<T> records; // 当前页的记录

    /**
     * 构造函数，根据Spring Data的Page对象创建分页响应DTO
     * @param page Spring Data的Page对象，包含分页数据
     */
    public PaginatedResponseDTO(Page<T> page) {
        // 从Page对象中提取分页信息并赋值给对应字段
        this.page = page.getNumber() + 1; // 获取当前页码
        this.pageSize = page.getSize(); // 获取每页记录数
        this.totalRecords = page.getTotalElements(); // 获取总记录数
        this.totalPages = page.getTotalPages(); // 获取总页数
        this.hasPrevPage = page.hasPrevious(); // 判断是否有上一页
        this.hasNextPage = page.hasNext(); // 判断是否有下一页
        this.records = page.getContent(); // 获取当前页的记录
    }
}

