package com.companyleveltraining.backend.notification;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.common.BizNoGenerator;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final BizNoGenerator bizNoGenerator;

    public NotificationService(NotificationRepository repository, BizNoGenerator bizNoGenerator) {
        this.repository = repository;
        this.bizNoGenerator = bizNoGenerator;
    }

    /** 发送一条站内通知。recipientRole: student/teacher/admin。 */
    public void send(Long recipientUserId, String recipientRole, String recipientAccountNo,
                     String title, String content, String type, Long relatedApplicationId) {
        repository.insert(bizNoGenerator.generate("N"), recipientUserId, recipientRole,
            recipientAccountNo, title, content, type, relatedApplicationId);
    }

    public List<Map<String, Object>> list(Long userId) {
        return repository.findByUser(userId);
    }

    public int unreadCount(Long userId) {
        return repository.unreadCount(userId);
    }

    public void markRead(Long id, Long userId) {
        repository.markRead(id, userId);
    }

    public void markAllRead(Long userId) {
        repository.markAllRead(userId);
    }
}
