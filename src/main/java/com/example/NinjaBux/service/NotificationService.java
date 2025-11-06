package com.example.NinjaBux.service;

import com.example.NinjaBux.dto.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendLessonCompleteNotification(Long ninjaId, String message) {
        NotificationDTO notification = new NotificationDTO(
            "LESSON_COMPLETE",
            "Lesson Complete!",
            message,
            ninjaId
        );
        messagingTemplate.convertAndSend("/topic/ninja/" + ninjaId, notification);
    }

    public void sendLevelUpNotification(Long ninjaId, String message, Object levelData) {
        NotificationDTO notification = new NotificationDTO(
            "LEVEL_UP",
            "Level Up!",
            message,
            ninjaId
        );
        notification.setData(levelData);
        messagingTemplate.convertAndSend("/topic/ninja/" + ninjaId, notification);
    }

    public void sendAchievementNotification(Long ninjaId, String message, Object achievementData) {
        NotificationDTO notification = new NotificationDTO(
            "ACHIEVEMENT",
            "Achievement Unlocked!",
            message,
            ninjaId
        );
        notification.setData(achievementData);
        messagingTemplate.convertAndSend("/topic/ninja/" + ninjaId, notification);
    }

    public void sendAnnouncementNotification(Long ninjaId, String title, String message) {
        NotificationDTO notification = new NotificationDTO(
            "ANNOUNCEMENT",
            title,
            message,
            ninjaId
        );
        messagingTemplate.convertAndSend("/topic/ninja/" + ninjaId, notification);
    }

    public void sendBroadcastAnnouncement(String title, String message) {
        NotificationDTO notification = new NotificationDTO(
            "ANNOUNCEMENT",
            title,
            message,
            null
        );
        messagingTemplate.convertAndSend("/topic/announcements", notification);
    }

    public void sendBroadcastNinjaLevelUp(Long ninjaId, String ninjaName, String message, Object levelData) {
        NotificationDTO notification = new NotificationDTO(
            "NINJA_LEVEL_UP",
            "Ninja Level Up!",
            message,
            ninjaId
        );
        notification.setData(levelData);
        messagingTemplate.convertAndSend("/topic/announcements", notification);
    }

    public void sendBroadcastNinjaBeltUp(Long ninjaId, String ninjaName, String message, Object beltData) {
        NotificationDTO notification = new NotificationDTO(
            "NINJA_BELT_UP",
            "Ninja Belt Up!",
            message,
            ninjaId
        );
        notification.setData(beltData);
        messagingTemplate.convertAndSend("/topic/announcements", notification);
    }

    public void sendQuestionRejectionNotification(Long ninjaId, String questionText, String reason) {
        String message = reason != null && !reason.trim().isEmpty()
            ? String.format("Your question suggestion \"%s\" was rejected. Reason: %s", 
                questionText.length() > 50 ? questionText.substring(0, 50) + "..." : questionText, reason)
            : String.format("Your question suggestion \"%s\" was rejected.", 
                questionText.length() > 50 ? questionText.substring(0, 50) + "..." : questionText);
        
        NotificationDTO notification = new NotificationDTO(
            "INFO",
            "Question Suggestion Rejected",
            message,
            ninjaId
        );
        messagingTemplate.convertAndSend("/topic/ninja/" + ninjaId, notification);
    }

    public void sendLockNotification(Long ninjaId, String reason) {
        NotificationDTO notification = new NotificationDTO(
            "ACCOUNT_LOCKED",
            "Account Locked",
            reason,
            ninjaId
        );
        messagingTemplate.convertAndSend("/topic/ninja/" + ninjaId, notification);
    }

    public void sendUnlockNotification(Long ninjaId) {
        NotificationDTO notification = new NotificationDTO(
            "ACCOUNT_UNLOCKED",
            "Account Unlocked",
            "Your account has been unlocked. You can now continue using the system.",
            ninjaId
        );
        messagingTemplate.convertAndSend("/topic/ninja/" + ninjaId, notification);
    }
}

