package org.example.pfabackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pfabackend.websocket.ExpenseReminderService;
import org.example.pfabackend.websocket.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ExpenseReminderService expenseReminderService;

    @PostMapping("/email")
    public String sendEmail(@RequestParam String to,
                            @RequestParam String subject,
                            @RequestParam String body) {
        notificationService.sendEmail(to, subject, body);
        return "Email sent to " + to;
    }

    @PostMapping("/socket")
    public String sendSocket(@RequestParam String userId,
                             @RequestParam String message) {
        notificationService.sendPushNotification(userId, message);
        return "WebSocket message sent to " + userId;
    }

    @PostMapping("/trigger-reminders")
    public ResponseEntity<String> triggerReminders() {
        expenseReminderService.sendUnpaidExpenseReminders();
        return ResponseEntity.ok("Reminders sent manually.");
    }
}
