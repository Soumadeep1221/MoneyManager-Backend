package com.soumadeep.MoneyManager.Service;

import com.soumadeep.MoneyManager.DTO.ExpenseDTO;
import com.soumadeep.MoneyManager.Entity.ProfileEntity;
import com.soumadeep.MoneyManager.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kolkata") // Every day at 10 PM
    public void sendDailyIncomeExpenseReminder() {
        log.info("Sending daily income and expense reminder...");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            if(profile.getIsActive()) {
                String firstName = extractFirstName(profile.getFullName());
                String body="Hi "+ firstName + ",<br><br>"
                        + "This is a friendly reminder to update your daily income and expenses in the Money Manager app.<br>"
                        + "Keeping track of your finances regularly helps you stay on top of your financial goals.<br><br>"
                        + "You can update your income and expenses by clicking the link below:<br>"
                        + "<a href=" + frontendUrl + " style='display: inline-block; padding: 10px 20px; background-color: #007bff; color: #fff; text-decoration: none; border-radius: 5px;'>Go to Money Manager</a><br><br>"
                        + "Thank you for using Money Manager!<br>"
                        + "Best regards,<br>"
                        + "The Money Manager Team";
                emailService.sendEmail(profile.getEmail(), "Daily Income and Expense Reminder", body);
            }
        }
        log.info("Completed sending daily income and expense reminder.");
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata") // Every day at 11 PM
    public void sendDailyExpensesSummary() {
        log.info("Sending daily expenses summary...");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            List<ExpenseDTO> todayExpenses=expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());
            if(!todayExpenses.isEmpty()){
                StringBuilder builder=new StringBuilder();
                builder.append("<table style='width: 100%; border-collapse: collapse; width: 100%;'>");
                builder.append("<tr style='background-color: #f2f2f2;'><th style='border: 1px solid #ddd; padding: 8px;'>S.No</th><th style='border: 1px solid #ddd; padding: 8px;'>Name</th><th style='border: 1px solid #ddd; padding: 8px;'>Amount</th><th style='border: 1px solid #ddd; padding: 8px;'>Category</th></tr>");
                int i=1;
                for(ExpenseDTO todayExpense:todayExpenses){
                    builder.append("<tr>");
                    builder.append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(i++).append("</td>");
                    builder.append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(todayExpense.getName()).append("</td>");
                    builder.append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(todayExpense.getAmount()).append("</td>");
                    builder.append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(todayExpense.getCategoryName()).append("</td>");
                    builder.append("</tr>");
                }
                builder.append("</table>");
                String body="Hi "+ profile.getFullName() + ",<br><br>"
                        + "Here is the summary of your expenses for today:<br><br>"
                        + builder
                        + "<br>Thank you for using Money Manager!<br>"
                        + "Best regards,<br>"
                        + "The Money Manager Team";
                emailService.sendEmail(profile.getEmail(), "Daily Expenses Summary", body);
            }
        }
        log.info("Completed sending daily expenses summary.");
    }

    public String extractFirstName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }
        String trimmedName = fullName.trim();
        int spaceIndex = trimmedName.indexOf(' ');
        if (spaceIndex != -1) {
            return trimmedName.substring(0, spaceIndex);
        }
        return trimmedName; // Return the full name if there's no space
    }
}
