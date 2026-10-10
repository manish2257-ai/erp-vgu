package com.example.data.repository

import com.example.data.local.ExamResultDao
import com.example.data.model.ExamResultEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class StudentPerformance(
    val totalMarks: Double,
    val maxPossibleMarks: Double,
    val percentage: Double,
    val averageScore: Double,
    val gpa: Double,
    val overallGrade: String,
    val subjectCount: Int
)

sealed class ExamOpResult {
    data class Success(val message: String, val result: ExamResultEntity? = null) : ExamOpResult()
    data class Error(val message: String, val statusCode: Int = 400) : ExamOpResult()
}

class ExamResultRepository(private val examResultDao: ExamResultDao) {

    val allResults: Flow<List<ExamResultEntity>> = examResultDao.getAllResults()
    val averageScore: Flow<Double?> = examResultDao.getAverageMarks()

    fun getResultsForStudent(studentId: String): Flow<List<ExamResultEntity>> {
        return examResultDao.getResultsByStudent(studentId)
    }

    suspend fun addExamMarks(
        role: UserRole,
        studentId: String,
        studentName: String,
        rollNumber: String,
        subject: String,
        marks: Double,
        examType: String,
        facultyName: String
    ): ExamOpResult {
        // Enforce RBAC security: Teacher (and Admin) can add marks; Student cannot
        if (role == UserRole.STUDENT) {
            return ExamOpResult.Error(
                message = "403 Forbidden: Students are not authorized to submit exam marks",
                statusCode = 403
            )
        }

        if (studentId.isBlank() || subject.isBlank()) {
            return ExamOpResult.Error("Student ID and Subject are required")
        }

        if (marks < 0.0 || marks > 100.0) {
            return ExamOpResult.Error("Marks must be between 0 and 100")
        }

        val existing = examResultDao.getExistingResult(studentId, subject.trim(), "Fall 2026")
        val entity = if (existing != null) {
            existing.copy(
                marks = marks,
                grade = ExamResultEntity.calculateGrade(marks),
                examType = examType,
                addedBy = facultyName
            )
        } else {
            ExamResultEntity(
                id = "res_" + UUID.randomUUID().toString().take(8),
                studentId = studentId,
                studentName = studentName,
                rollNumber = rollNumber,
                subject = subject.trim(),
                marks = marks,
                examType = examType,
                addedBy = facultyName
            )
        }

        examResultDao.insertResult(entity)
        return ExamOpResult.Success("Marks for '${entity.subject}' recorded with Grade ${entity.grade}", entity)
    }

    companion object {
        fun calculatePerformance(results: List<ExamResultEntity>): StudentPerformance {
            if (results.isEmpty()) {
                return StudentPerformance(
                    totalMarks = 0.0,
                    maxPossibleMarks = 0.0,
                    percentage = 0.0,
                    averageScore = 0.0,
                    gpa = 0.0,
                    overallGrade = "N/A",
                    subjectCount = 0
                )
            }

            val total = results.sumOf { it.marks }
            val max = results.size * 100.0
            val pct = (total / max) * 100.0
            val avg = total / results.size

            val gpa = when {
                avg >= 90.0 -> 4.0
                avg >= 80.0 -> 3.7
                avg >= 70.0 -> 3.0
                avg >= 60.0 -> 2.0
                avg >= 50.0 -> 1.0
                else -> 0.0
            }

            val grade = ExamResultEntity.calculateGrade(avg)

            return StudentPerformance(
                totalMarks = total,
                maxPossibleMarks = max,
                percentage = pct,
                averageScore = avg,
                gpa = gpa,
                overallGrade = grade,
                subjectCount = results.size
            )
        }
    }
}
