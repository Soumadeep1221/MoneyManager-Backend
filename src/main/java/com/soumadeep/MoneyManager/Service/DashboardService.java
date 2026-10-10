package com.soumadeep.MoneyManager.Service;

import com.soumadeep.MoneyManager.DTO.ExpenseDTO;
import com.soumadeep.MoneyManager.DTO.IncomeDTO;
import com.soumadeep.MoneyManager.DTO.RecentTransactionDTO;
import com.soumadeep.MoneyManager.Entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String,Object> getDashboardData() {
        ProfileEntity profile=profileService.getCurrentProfile();
        Map<String,Object>returnValue=new LinkedHashMap<>();
        List<IncomeDTO> latestIncomes=incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses=expenseService.getLatest5ExpensesForCurrentUser();

        List<RecentTransactionDTO> recentTransactions=concat(latestIncomes.stream().map(incomeDTO ->
                RecentTransactionDTO.builder()
                        .id(incomeDTO.getId())
                        .profileId(profile.getId())
                        .icon(incomeDTO.getIcon())
                        .name(incomeDTO.getName())
                        .amount(incomeDTO.getAmount())
                        .date(incomeDTO.getDate())
                        .createdAt(incomeDTO.getCreatedAt())
                        .updatedAt(incomeDTO.getUpdatedAt())
                        .type("Income")
                        .build()),
                latestExpenses.stream().map(expenseDTO ->
                        RecentTransactionDTO.builder()
                                .id(expenseDTO.getId())
                                .profileId(profile.getId())
                                .icon(expenseDTO.getIcon())
                                .name(expenseDTO.getName())
                                .amount(expenseDTO.getAmount())
                                .date(expenseDTO.getDate())
                                .createdAt(expenseDTO.getCreatedAt())
                                .updatedAt(expenseDTO.getUpdatedAt())
                                .type("Expense")
                                .build()))
                        .sorted((a, b) -> {
                            int cmp=b.getDate().compareTo(a.getDate());
                            if(cmp==0 && b.getCreatedAt()!=null && a.getCreatedAt()!=null) {
                                return b.getCreatedAt().compareTo(a.getCreatedAt());
                            }
                            return cmp;
                        }).toList();

        returnValue.put("totalIncome",incomeService.getTotalIncomesForCurrentUser());
        returnValue.put("totalExpense",expenseService.getTotalExpensesForCurrentUser());
        returnValue.put("latestIncome",latestIncomes);
        returnValue.put("latestExpense",latestExpenses);
        returnValue.put("recentTransactions",recentTransactions);
        return returnValue;
    }
}
