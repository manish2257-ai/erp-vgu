package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String = "Student",
    val phone: String = "+1 (555) 234-5678",
    val avatarUri: String? = null,
    val identifierNumber: String = "ADM-2024-001",
    val department: String = "Office of Academic Affairs",
    val designation: String = "Campus Administrator",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toUserRole(): UserRole = UserRole.fromString(role)
}
