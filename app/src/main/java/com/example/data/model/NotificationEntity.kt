package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val targetRole: String = "All", // "All", "Admin", "Teacher", "Student"
    val senderName: String = "Administration",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
