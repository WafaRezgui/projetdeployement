package com.example.contentmanagement.service.impl;

import com.example.contentmanagement.dto.NotificationDTO;
import com.example.contentmanagement.entity.Notification;
import com.example.contentmanagement.entity.User;
import com.example.contentmanagement.exception.ResourceNotFoundException;
import com.example.contentmanagement.repository.NotificationRepository;
import com.example.contentmanagement.repository.UserRepository;
import com.example.contentmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        try {
            log.info("Creating notification for user: {}", notificationDTO.getUserId());
            
            User user;
            if (notificationDTO.getUserId() != null && !notificationDTO.getUserId().isEmpty()) {
                user = userRepository.findById(notificationDTO.getUserId())
                        .orElseGet(() -> createAnonymousUserForNotification(notificationDTO.getUserId()));
            } else {
                user = createAnonymousUserForNotification("system-user");
            }

            Notification notification = Notification.builder()
                    .message(notificationDTO.getMessage())
                    .title(notificationDTO.getTitle())
                    .type(notificationDTO.getType())
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .user(user)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification saved successfully with ID: {}", savedNotification.getId());
            
            // Trigger external notifications if needed
            if ("EMAIL".equalsIgnoreCase(notificationDTO.getType())) {
                sendEmail(user.getEmail(), "New Notification", notificationDTO.getMessage());
            } else if ("SMS".equalsIgnoreCase(notificationDTO.getType())) {
                sendSms("USER_PHONE_NUMBER", notificationDTO.getMessage());
            }

            return mapToDTO(savedNotification);
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create notification: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationDTO> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUser_Id(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notificationRepository.delete(notification);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending Email to: {}, Subject: {}, Body: {}", to, subject, body);
        // Structure for real API integration
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("Sending SMS to: {}, Message: {}", phoneNumber, message);
        // Structure for real API integration
    }

    private User createAnonymousUserForNotification(String identifier) {
        try {
            User newUser = new User();
            newUser.setUsername(identifier);
            newUser.setEmail(identifier + "@system.local");
            newUser.setPassword(""); // System user - no password auth
            newUser.setEnabled(true);
            log.info("Created anonymous user for notification: {}", identifier);
            return userRepository.save(newUser);
        } catch (Exception e) {
            log.error("Error creating anonymous user: {}", e.getMessage());
            // Return a minimal user object instead of failing
            User fallbackUser = userRepository.findByUsername("system")
                    .orElseGet(() -> {
                        User sysUser = new User();
                        sysUser.setUsername("system");
                        sysUser.setEmail("system@system.local");
                        sysUser.setPassword("");
                        sysUser.setEnabled(true);
                        return userRepository.save(sysUser);
                    });
            return fallbackUser;
        }
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .title(notification.getTitle())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .userId(notification.getUser().getId())
                .username(notification.getUser().getUsername())
                .build();
    }
}
