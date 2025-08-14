package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

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

    public PaginatedResponseDTO(Page<T> page) {
        this.page = page.getNumber() + 1; // 获取当前页码
        this.pageSize = page.getSize(); // 获取每页记录数
        this.totalRecords = page.getTotalElements(); // 获取总记录数
        this.totalPages = page.getTotalPages(); // 获取总页数
        this.hasPrevPage = page.hasPrevious(); // 判断是否有上一页
        this.hasNextPage = page.hasNext(); // 判断是否有下一页
        this.records = page.getContent(); // 获取当前页的记录
    }
}
