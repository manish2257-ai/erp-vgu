package com.example.data.repository

import com.example.data.local.FacultyDao
import com.example.data.model.FacultyEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class FacultyOpResult {
    data class Success(val message: String, val faculty: FacultyEntity? = null) : FacultyOpResult()
    data class Error(val message: String, val statusCode: Int = 400) : FacultyOpResult()
}

class FacultyRepository(private val facultyDao: FacultyDao) {

    val allFaculty: Flow<List<FacultyEntity>> = facultyDao.getAllFaculty()

    suspend fun getFacultyById(id: String): FacultyEntity? {
        return facultyDao.getFacultyById(id)
    }

    suspend fun addFaculty(
        role: UserRole,
        name: String,
        department: String,
        subjects: String,
        email: String,
        phone: String
    ): FacultyOpResult {
        // Enforce RBAC security: only Admin can add faculty
        if (role != UserRole.ADMIN) {
            return FacultyOpResult.Error(
                message = "403 Forbidden: Only Admin accounts can add faculty members",
                statusCode = 403
            )
        }

        if (name.isBlank() || department.isBlank() || subjects.isBlank() || email.isBlank()) {
            return FacultyOpResult.Error("All fields (Name, Department, Subjects, Email) are required")
        }

        if (!email.contains("@") || !email.contains(".")) {
            return FacultyOpResult.Error("Please provide a valid institutional email address")
        }

        val existingEmail = facultyDao.getFacultyByEmail(email.trim())
        if (existingEmail != null) {
            return FacultyOpResult.Error("A faculty member with email '${email.trim()}' already exists")
        }

        val faculty = FacultyEntity(
            id = "fac_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            department = department.trim(),
            subjects = subjects.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim()
        )

        facultyDao.insertFaculty(faculty)
        return FacultyOpResult.Success("Faculty member '${faculty.name}' added successfully", faculty)
    }

    suspend fun updateFaculty(
        role: UserRole,
        id: String,
        name: String,
        department: String,
        subjects: String,
        email: String,
        phone: String
    ): FacultyOpResult {
        if (role != UserRole.ADMIN) {
            return FacultyOpResult.Error(
                message = "403 Forbidden: Only Admin accounts can modify faculty records",
                statusCode = 403
            )
        }

        val existing = facultyDao.getFacultyById(id)
            ?: return FacultyOpResult.Error("Faculty record not found with ID: $id", 404)

        if (email.trim().lowercase() != existing.email) {
            val duplicate = facultyDao.getFacultyByEmail(email.trim())
            if (duplicate != null) {
                return FacultyOpResult.Error("Another faculty member has email '${email.trim()}'")
            }
        }

        val updated = existing.copy(
            name = name.trim(),
            department = department.trim(),
            subjects = subjects.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim()
        )

        facultyDao.updateFaculty(updated)
        return FacultyOpResult.Success("Faculty '${updated.name}' updated successfully", updated)
    }

    suspend fun deleteFaculty(role: UserRole, id: String): FacultyOpResult {
        if (role != UserRole.ADMIN) {
            return FacultyOpResult.Error(
                message = "403 Forbidden: Only Admin accounts can delete faculty records",
                statusCode = 403
            )
        }

        val existing = facultyDao.getFacultyById(id)
            ?: return FacultyOpResult.Error("Faculty record not found with ID: $id", 404)

        facultyDao.deleteFacultyById(id)
        return FacultyOpResult.Success("Faculty member '${existing.name}' deleted successfully")
    }
}
