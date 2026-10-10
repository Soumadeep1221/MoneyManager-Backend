package com.soumadeep.MoneyManager.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDTO {
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; // Field to sort by (e.g., "date", "amount", "name")
    private String sortOrder; // Sort order ("asc" for ascending, "desc" for descending)
}
