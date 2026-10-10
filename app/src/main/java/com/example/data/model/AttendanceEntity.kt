package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val subject: String,
    val date: String, // Format: YYYY-MM-DD
    val status: String, // "Present", "Absent", "Late"
    val markedBy: String = "Faculty",
    val timestamp: Long = System.currentTimeMillis()
)
