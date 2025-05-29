package org.example.pfabackend.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pfabackend.entities.Expense;
import org.example.pfabackend.entities.ExpenseShare;
import org.example.pfabackend.repositories.ExpenseRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseReminderService {

    private final ExpenseRepository expenseRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?") // chaque jour à 9h
    public void sendUnpaidExpenseReminders() {
        List<Expense> allExpenses = expenseRepository.findAll();
        LocalDate today = LocalDate.now();

        Map<String, Double> unpaidAmountsPerUser = new HashMap<>();
        Map<String, List<String>> unpaidLabelsPerUser = new HashMap<>();
        Map<String, String> emailMap = new HashMap<>();

        for (Expense expense : allExpenses) {
            if (expense.getDateLimit() != null &&
                    expense.getDateLimit().minusDays(7).equals(today)) {

                for (ExpenseShare share : expense.getShares()) {
                    if (share.getUserId() != null &&
                            share.getUserEmail() != null &&
                            !Boolean.TRUE.equals(share.getPaid())) {

                        String userId = share.getUserId();
                        String userEmail = share.getUserEmail();

                        unpaidAmountsPerUser.put(userId,
                                unpaidAmountsPerUser.getOrDefault(userId, 0.0) + share.getAmount());

                        unpaidLabelsPerUser
                                .computeIfAbsent(userId, k -> new ArrayList<>())
                                .add(expense.getLabel());

                        emailMap.putIfAbsent(userId, userEmail);
                    }
                }
            }
        }

        for (String userId : unpaidAmountsPerUser.keySet()) {
            double totalUnpaid = unpaidAmountsPerUser.get(userId);
            List<String> bills = unpaidLabelsPerUser.get(userId);
            String userEmail = emailMap.get(userId);

            String subject = "🔔 Reminder: You have unpaid expenses due soon";
            String body = "Hello,\n\n"
                    + "You have expenses due in 7 days totaling **$" + totalUnpaid + "**.\n"
                    + "Pending bills: " + String.join(", ", bills) + ".\n\n"
                    + "Please make your payments soon.\n\n"
                    + "Thank you.";

            try {
                notificationService.sendEmail(userEmail, subject, body);
                notificationService.sendPushNotification(userId, "💰 Expenses due in 7 days: $" + totalUnpaid);

                log.info("🔔 Reminder sent to {} ({}) - ${} unpaid", userId, userEmail, totalUnpaid);
            } catch (Exception e) {
                log.error("❌ Failed to send reminder to {} ({})", userId, userEmail, e);
            }
        }
    }

}
