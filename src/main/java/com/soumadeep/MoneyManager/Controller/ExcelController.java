package com.soumadeep.MoneyManager.Controller;

import com.soumadeep.MoneyManager.Service.ExcelService;
import com.soumadeep.MoneyManager.Service.ExpenseService;
import com.soumadeep.MoneyManager.Service.IncomeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ExcelService excelService;

    @GetMapping("/download/income")
    public ResponseEntity<Void> downloadIncomeExcel(HttpServletResponse response)throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=incomes.xlsx");
        excelService.writeIncomesToExcel(response.getOutputStream(), incomeService.getCurrentMonthIncomesForCurrentUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/expense")
    public ResponseEntity<Void> downloadExpenseExcel(HttpServletResponse response)throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");
        excelService.writeExpensesToExcel(response.getOutputStream(), expenseService.getCurrentMonthExpensesForCurrentUser());
        return ResponseEntity.ok().build();
    }
}
