package com.example.data.model

enum class UserRole(val label: String) {
    ADMIN("Admin"),
    TEACHER("Teacher"),
    STUDENT("Student");

    companion object {
        fun fromString(role: String?): UserRole {
            return when (role?.trim()?.lowercase()) {
                "admin" -> ADMIN
                "teacher" -> TEACHER
                "student" -> STUDENT
                else -> STUDENT
            }
        }
    }
}
