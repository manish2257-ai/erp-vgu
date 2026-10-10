package com.example.data.repository

import com.example.data.local.AttendanceDao
import com.example.data.model.AttendanceEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class AttendanceOpResult {
    data class Success(val message: String, val record: AttendanceEntity? = null) : AttendanceOpResult()
    data class Error(val message: String, val statusCode: Int = 400) : AttendanceOpResult()
}

class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()

    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByStudent(studentId)
    }

    suspend fun markAttendance(
        role: UserRole,
        studentId: String,
        studentName: String,
        rollNumber: String,
        subject: String,
        date: String,
        status: String,
        markedByName: String
    ): AttendanceOpResult {
        // Enforce RBAC security: Only Teacher or Admin can mark attendance
        if (role == UserRole.STUDENT) {
            return AttendanceOpResult.Error(
                message = "403 Forbidden: Students are not authorized to mark attendance",
                statusCode = 403
            )
        }

        if (studentId.isBlank() || subject.isBlank() || date.isBlank()) {
            return AttendanceOpResult.Error("Missing required parameters for attendance")
        }

        val existing = attendanceDao.getExistingRecord(studentId, subject, date)
        val record = if (existing != null) {
            existing.copy(
                status = status,
                markedBy = markedByName,
                timestamp = System.currentTimeMillis()
            )
        } else {
            AttendanceEntity(
                id = "att_" + UUID.randomUUID().toString().take(8),
                studentId = studentId,
                studentName = studentName,
                rollNumber = rollNumber,
                subject = subject.trim(),
                date = date.trim(),
                status = status,
                markedBy = markedByName,
                timestamp = System.currentTimeMillis()
            )
        }

        attendanceDao.insertAttendance(record)
        return AttendanceOpResult.Success("Attendance for '$studentName' marked as $status", record)
    }

    companion object {
        fun calculatePercentage(records: List<AttendanceEntity>): Float {
            if (records.isEmpty()) return 100f
            val presentCount = records.count { it.status.equals("Present", ignoreCase = true) }
            val lateCount = records.count { it.status.equals("Late", ignoreCase = true) }
            // Late counts as 0.75 or 0.5 attendance
            val weightedPresent = presentCount + (lateCount * 0.5f)
            return (weightedPresent / records.size) * 100f
        }
    }
}
