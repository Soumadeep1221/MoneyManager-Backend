package com.soumadeep.MoneyManager.Service;

import com.soumadeep.MoneyManager.DTO.IncomeDTO;
import com.soumadeep.MoneyManager.Entity.CategoryEntity;
import com.soumadeep.MoneyManager.Entity.IncomeEntity;
import com.soumadeep.MoneyManager.Entity.ProfileEntity;
import com.soumadeep.MoneyManager.Repository.CategoryRepository;
import com.soumadeep.MoneyManager.Repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO incomeDTO) {
        ProfileEntity profile=profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findByIdAndProfileId(incomeDTO.getCategoryId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        IncomeEntity newIncome = toEntity(incomeDTO, profile, category);
        newIncome = incomeRepository.save(newIncome);
        return toDTO(newIncome);
    }

    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity profile=profileService.getCurrentProfile();
        LocalDate now=LocalDate.now();
        LocalDate startDate=now.withDayOfMonth(1);
        LocalDate endDate=now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return incomes.stream().map(this::toDTO).toList();
    }

    public void deleteIncome(Long incomeId) {
        ProfileEntity profile=profileService.getCurrentProfile();
        IncomeEntity income = incomeRepository.findById(incomeId).
                orElseThrow(() -> new RuntimeException("Income not found"));

        if(!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You are not authorized to delete this income");
        }
        incomeRepository.delete(income);
    }

    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile=profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomes.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalIncomesForCurrentUser() {
        ProfileEntity profile=profileService.getCurrentProfile();
        BigDecimal totalIncome = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return totalIncome != null ? totalIncome : BigDecimal.ZERO;
    }

    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile=profileService.getCurrentProfile();
        if (startDate == null) {
            LocalDate earliest = incomeRepository.findEarliestDateByProfileId(profile.getId());
            startDate = earliest != null ? earliest : LocalDate.now();
        }
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                profile.getId(), startDate, endDate, keyword, sort);
        return incomes.stream().map(this::toDTO).toList();
    }

    // Helper method to convert IncomeDTO to IncomeEntity
    private IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .date(incomeDTO.getDate())
                .amount(incomeDTO.getAmount())
                .profile(profile)
                .category(category)
                .build();
    }

    // Helper method to convert IncomeEntity to IncomeDTO
    private IncomeDTO toDTO(IncomeEntity incomeEntity) {
        return IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .date(incomeEntity.getDate())
                .amount(incomeEntity.getAmount())
                .categoryId(incomeEntity.getCategory()!=null ? incomeEntity.getCategory().getId() : null)
                .categoryName(incomeEntity.getCategory()!=null ? incomeEntity.getCategory().getName() : "N/A")
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }
}
