package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponseDto<T> {
    private int page;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private boolean hasPrevPage;
    private boolean hasNextPage;
    private List<T> records;
}
