package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "faculty")
data class FacultyEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val department: String,
    val subjects: String, // Comma-separated or list of subjects
    val email: String,
    val phone: String = "+1 (555) 345-6789",
    val createdAt: Long = System.currentTimeMillis()
)
