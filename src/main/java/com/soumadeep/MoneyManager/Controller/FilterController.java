package com.soumadeep.MoneyManager.Controller;

import com.soumadeep.MoneyManager.DTO.ExpenseDTO;
import com.soumadeep.MoneyManager.DTO.FilterDTO;
import com.soumadeep.MoneyManager.DTO.IncomeDTO;
import com.soumadeep.MoneyManager.Service.ExpenseService;
import com.soumadeep.MoneyManager.Service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final Set<String> allowedSortFields = Set.of("date", "amount", "name");

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO) {

        LocalDate startDate=filterDTO.getStartDate()!=null ? filterDTO.getStartDate() : null;
        LocalDate endDate=filterDTO.getEndDate()!=null ? filterDTO.getEndDate() : LocalDate.now();
        String keyword=filterDTO.getKeyword()!=null ? filterDTO.getKeyword() : "";
        String sortField=filterDTO.getSortField()!=null ? filterDTO.getSortField() : "date";
        if (!allowedSortFields.contains(sortField)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid sort field. Must be one of: " + allowedSortFields);
        }
        Sort.Direction direction="desc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort=Sort.by(direction, sortField);

        if("income".equalsIgnoreCase(filterDTO.getType())) {
            List<IncomeDTO> incomes=incomeService.filterIncomes(startDate, endDate, keyword, sort);
            return ResponseEntity.status(HttpStatus.OK).body(incomes);
        } else if ("expense".equalsIgnoreCase(filterDTO.getType())) {
            List<ExpenseDTO> expenses=expenseService.filterExpenses(startDate, endDate, keyword, sort);
            return ResponseEntity.status(HttpStatus.OK).body(expenses);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid filter type. Must be 'income' or 'expense'.");
        }
    }
}
