package com.companyleveltraining.backend.notification;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.notification.mq.NotificationMessage;
import com.companyleveltraining.backend.notification.mq.NotificationQueuePublisher;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final BizNoGenerator bizNoGenerator;
    private final NotificationQueuePublisher queuePublisher;

    public NotificationService(NotificationRepository repository, BizNoGenerator bizNoGenerator,
                               NotificationQueuePublisher queuePublisher) {
        this.repository = repository;
        this.bizNoGenerator = bizNoGenerator;
        this.queuePublisher = queuePublisher;
    }

    /** 发送一条站内通知。recipientRole: student/teacher/admin。 */
    public void send(Long recipientUserId, String recipientRole, String recipientAccountNo,
                     String title, String content, String type, Long relatedApplicationId) {
        NotificationMessage message = new NotificationMessage(
            recipientUserId, recipientRole, recipientAccountNo, title, content, type, relatedApplicationId
        );
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishOrSave(message);
                }
            });
            return;
        }
        publishOrSave(message);
    }

    public void save(NotificationMessage message) {
        repository.insert(bizNoGenerator.generate("N"), message.recipientUserId(), message.recipientRole(),
            message.recipientAccountNo(), message.title(), message.content(), message.type(), message.relatedApplicationId());
    }

    private void publishOrSave(NotificationMessage message) {
        if (!queuePublisher.publish(message)) {
            save(message);
        }
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
