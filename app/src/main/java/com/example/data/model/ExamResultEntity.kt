package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val subject: String,
    val marks: Double, // 0 - 100
    val maxMarks: Double = 100.0,
    val grade: String = calculateGrade(marks),
    val semester: String = "Fall 2026",
    val examType: String = "Midterm", // "Midterm", "Final", "Quiz"
    val addedBy: String = "Faculty",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun calculateGrade(marks: Double): String {
            return when {
                marks >= 90.0 -> "A+"
                marks >= 80.0 -> "A"
                marks >= 70.0 -> "B"
                marks >= 60.0 -> "C"
                marks >= 50.0 -> "D"
                else -> "F"
            }
        }
    }
}
