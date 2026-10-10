package com.soumadeep.MoneyManager.Service;

import com.soumadeep.MoneyManager.DTO.ExpenseDTO;
import com.soumadeep.MoneyManager.Entity.CategoryEntity;
import com.soumadeep.MoneyManager.Entity.ExpenseEntity;
import com.soumadeep.MoneyManager.Entity.ProfileEntity;
import com.soumadeep.MoneyManager.Repository.CategoryRepository;
import com.soumadeep.MoneyManager.Repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        ProfileEntity profile=profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findByIdAndProfileId(expenseDTO.getCategoryId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ExpenseEntity newExpense = toEntity(expenseDTO, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile=profileService.getCurrentProfile();
        LocalDate now=LocalDate.now();
        LocalDate startDate=now.withDayOfMonth(1);
        LocalDate endDate=now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return expenses.stream().map(this::toDTO).toList();
    }

    public void deleteExpense(Long expenseId) {
        ProfileEntity profile=profileService.getCurrentProfile();
        ExpenseEntity expense = expenseRepository.findById(expenseId).
                orElseThrow(() -> new RuntimeException("Expense not found"));

        if(!expense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You are not authorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile=profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalExpensesForCurrentUser() {
        ProfileEntity profile=profileService.getCurrentProfile();
        BigDecimal totalExpense = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpense != null ? totalExpense : BigDecimal.ZERO;
    }

    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile=profileService.getCurrentProfile();
        if (startDate == null) {
            LocalDate earliest = expenseRepository.findEarliestDateByProfileId(profile.getId());
            startDate = earliest != null ? earliest : LocalDate.now();
        }
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                profile.getId(), startDate, endDate, keyword, sort);
        return expenses.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenses.stream().map(this::toDTO).toList();
    }

    // Helper method to convert ExpenseDTO to ExpenseEntity
    private ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .date(expenseDTO.getDate())
                .amount(expenseDTO.getAmount())
                .profile(profile)
                .category(category)
                .build();
    }

    // Helper method to convert ExpenseEntity to ExpenseDTO
    private ExpenseDTO toDTO(ExpenseEntity expenseEntity) {
        return ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .date(expenseEntity.getDate())
                .amount(expenseEntity.getAmount())
                .categoryId(expenseEntity.getCategory()!=null ? expenseEntity.getCategory().getId() : null)
                .categoryName(expenseEntity.getCategory()!=null ? expenseEntity.getCategory().getName() : "N/A")
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }
}
