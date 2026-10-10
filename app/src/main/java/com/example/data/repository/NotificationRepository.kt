package com.example.data.repository

import com.example.data.local.NotificationDao
import com.example.data.model.NotificationEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NotificationRepository(private val notificationDao: NotificationDao) {

    fun getNotificationsForRole(role: UserRole): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsForRole(role.label)
    }

    val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun sendNotification(
        title: String,
        message: String,
        targetRole: String,
        senderName: String
    ): Result<NotificationEntity> {
        if (title.isBlank() || message.isBlank()) {
            return Result.failure(Exception("Title and message cannot be empty"))
        }

        val notification = NotificationEntity(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = title.trim(),
            message = message.trim(),
            targetRole = targetRole,
            senderName = senderName
        )

        notificationDao.insertNotification(notification)
        return Result.success(notification)
    }

    suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }
}
