package com.example.data.repository

import com.example.data.local.StudentDao
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class StudentOpResult {
    data class Success(val message: String, val student: StudentEntity? = null) : StudentOpResult()
    data class Error(val message: String, val statusCode: Int = 400) : StudentOpResult()
}

class StudentRepository(private val studentDao: StudentDao) {

    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()

    suspend fun getStudentById(id: String): StudentEntity? {
        return studentDao.getStudentById(id)
    }

    suspend fun addStudent(
        role: UserRole,
        name: String,
        email: String,
        course: String,
        year: Int,
        rollNumber: String,
        creatorId: String?
    ): StudentOpResult {
        // Enforce RBAC security
        if (role != UserRole.ADMIN) {
            return StudentOpResult.Error(
                message = "403 Forbidden: Only Admin accounts are authorized to enroll students",
                statusCode = 403
            )
        }

        if (name.isBlank() || email.isBlank() || course.isBlank() || rollNumber.isBlank()) {
            return StudentOpResult.Error("All fields (Name, Email, Course, Year, Roll Number) are required", 400)
        }

        // Validate roll number uniqueness
        val existingRoll = studentDao.getStudentByRollNumber(rollNumber.trim())
        if (existingRoll != null) {
            return StudentOpResult.Error("Student with roll number '${rollNumber.trim()}' already exists", 400)
        }

        // Validate email uniqueness
        val existingEmail = studentDao.getStudentByEmail(email.trim())
        if (existingEmail != null) {
            return StudentOpResult.Error("Student with email '${email.trim()}' already exists", 400)
        }

        val student = StudentEntity(
            id = "std_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = email.trim().lowercase(),
            course = course.trim(),
            year = year,
            rollNumber = rollNumber.trim().uppercase(),
            createdBy = creatorId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        studentDao.insertStudent(student)
        return StudentOpResult.Success("Student '${student.name}' added successfully", student)
    }

    suspend fun updateStudent(
        role: UserRole,
        id: String,
        name: String,
        email: String,
        course: String,
        year: Int,
        rollNumber: String
    ): StudentOpResult {
        // Enforce RBAC security
        if (role != UserRole.ADMIN) {
            return StudentOpResult.Error(
                message = "403 Forbidden: Only Admin accounts are authorized to modify student records",
                statusCode = 403
            )
        }

        val existing = studentDao.getStudentById(id)
            ?: return StudentOpResult.Error("Student not found with ID: $id", 404)

        if (rollNumber.trim().uppercase() != existing.rollNumber) {
            val duplicateRoll = studentDao.getStudentByRollNumber(rollNumber.trim())
            if (duplicateRoll != null) {
                return StudentOpResult.Error("Another student has roll number '${rollNumber.trim()}'", 400)
            }
        }

        if (email.trim().lowercase() != existing.email) {
            val duplicateEmail = studentDao.getStudentByEmail(email.trim())
            if (duplicateEmail != null) {
                return StudentOpResult.Error("Another student has email '${email.trim()}'", 400)
            }
        }

        val updated = existing.copy(
            name = name.trim(),
            email = email.trim().lowercase(),
            course = course.trim(),
            year = year,
            rollNumber = rollNumber.trim().uppercase(),
            updatedAt = System.currentTimeMillis()
        )

        studentDao.updateStudent(updated)
        return StudentOpResult.Success("Student '${updated.name}' updated successfully", updated)
    }

    suspend fun deleteStudent(role: UserRole, id: String): StudentOpResult {
        // Enforce RBAC security
        if (role != UserRole.ADMIN) {
            return StudentOpResult.Error(
                message = "403 Forbidden: Only Admin accounts are authorized to delete student records",
                statusCode = 403
            )
        }

        val existing = studentDao.getStudentById(id)
            ?: return StudentOpResult.Error("Student not found with ID: $id", 404)

        studentDao.deleteStudentById(id)
        return StudentOpResult.Success("Student with roll number '${existing.rollNumber}' deleted successfully")
    }
}
