package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResponseDto<T> {
    private int page;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private boolean hasPrevPage;
    private boolean hasNextPage;
    private List<T> records;

    public PaginatedResponseDto(Page<T> page) {
        this.page = page.getNumber() + 1; // Spring Page is 0-based
        this.pageSize = page.getSize();
        this.totalRecords = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.hasPrevPage = page.hasPrevious();
        this.hasNextPage = page.hasNext();
        this.records = page.getContent();
    }
}
