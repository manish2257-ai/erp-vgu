package com.example

import com.example.data.local.AppDatabase
import com.example.data.model.AttendanceEntity
import com.example.data.model.FacultyEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.AttendanceRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testUserRoleParsing() {
        assertEquals(UserRole.ADMIN, UserRole.fromString("Admin"))
        assertEquals(UserRole.ADMIN, UserRole.fromString("admin"))
        assertEquals(UserRole.TEACHER, UserRole.fromString("Teacher"))
        assertEquals(UserRole.STUDENT, UserRole.fromString("Student"))
        assertEquals(UserRole.STUDENT, UserRole.fromString("unknown_role"))
    }

    @Test
    fun testPasswordHashingConsistency() {
        val hash1 = AppDatabase.hashPassword("secret123")
        val hash2 = AppDatabase.hashPassword("secret123")
        val differentHash = AppDatabase.hashPassword("different_password")

        assertNotNull(hash1)
        assertEquals(hash1, hash2)
        assertNotEquals(hash1, differentHash)
    }

    @Test
    fun testStudentEntityAttributes() {
        val student = StudentEntity(
            id = "std_001",
            name = "Alex Mercer",
            email = "alex.m@campus.edu",
            course = "Computer Science",
            year = 3,
            rollNumber = "CS-2024-001"
        )

        assertEquals("Alex Mercer", student.name)
        assertEquals("CS-2024-001", student.rollNumber)
        assertEquals(3, student.year)
    }

    @Test
    fun testFacultyEntityAttributes() {
        val faculty = FacultyEntity(
            id = "fac_001",
            name = "Dr. Alan Turing",
            department = "Computer Science",
            subjects = "AI Systems, Theory of Computation",
            email = "alan.turing@campus.edu"
        )

        assertEquals("Dr. Alan Turing", faculty.name)
        assertEquals("Computer Science", faculty.department)
        assertTrue(faculty.subjects.contains("AI Systems"))
    }

    @Test
    fun testAttendancePercentageCalculation() {
        val records = listOf(
            AttendanceEntity(
                studentId = "std_001",
                studentName = "Alex Mercer",
                rollNumber = "CS-2024-001",
                subject = "Data Structures",
                date = "2026-10-09",
                status = "Present"
            ),
            AttendanceEntity(
                studentId = "std_001",
                studentName = "Alex Mercer",
                rollNumber = "CS-2024-001",
                subject = "Algorithms",
                date = "2026-10-08",
                status = "Late"
            ),
            AttendanceEntity(
                studentId = "std_001",
                studentName = "Alex Mercer",
                rollNumber = "CS-2024-001",
                subject = "Databases",
                date = "2026-10-07",
                status = "Absent"
            )
        )

        // 1 Present + 0.5 Late = 1.5 out of 3 = 50.0%
        val percentage = AttendanceRepository.calculatePercentage(records)
        assertEquals(50.0f, percentage, 0.01f)
    }

    @Test
    fun testUserProfileFields() {
        val user = UserEntity(
            id = "u1",
            name = "Dean Arthur Vance",
            email = "admin@campus.edu",
            passwordHash = "hash123",
            role = "Admin",
            phone = "+1 (555) 234-5678",
            department = "Office of Academic Affairs",
            designation = "Dean"
        )

        assertEquals("+1 (555) 234-5678", user.phone)
        assertEquals("Office of Academic Affairs", user.department)
        assertEquals("Dean", user.designation)
    }
}
