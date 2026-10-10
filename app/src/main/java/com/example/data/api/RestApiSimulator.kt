package com.example.data.api

import com.example.data.local.StudentDao
import com.example.data.local.UserDao
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.first

data class ApiTestResult(
    val endpoint: String,
    val method: String,
    val statusCode: Int,
    val statusText: String,
    val durationMs: Long,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val responseBody: String
)

class RestApiSimulator(
    private val userDao: UserDao,
    private val studentDao: StudentDao
) {

    suspend fun executeTestEndpoint(): ApiTestResult {
        val start = System.currentTimeMillis()
        val duration = 45L
        val body = """
        {
          "success": true,
          "message": "ERP API Server is online and functioning properly!",
          "service": "Campus ERP REST API",
          "version": "1.0.0",
          "timestamp": "${java.time.Instant.now()}"
        }
        """.trimIndent()

        return ApiTestResult(
            endpoint = "/api/test",
            method = "GET",
            statusCode = 200,
            statusText = "OK",
            durationMs = duration,
            requestHeaders = mapOf("Accept" to "application/json"),
            requestBody = null,
            responseBody = body
        )
    }

    suspend fun executeAuthLogin(email: String, role: String, token: String?): ApiTestResult {
        val start = System.currentTimeMillis()
        val effectiveToken = token ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6InVzZXJfMDAxIiwicm9sZSI6IiRyb2xlIn0.sig89234"
        val body = """
        {
          "success": true,
          "message": "Login successful",
          "token": "$effectiveToken",
          "user": {
            "email": "$email",
            "role": "$role"
          }
        }
        """.trimIndent()

        return ApiTestResult(
            endpoint = "/api/auth/login",
            method = "POST",
            statusCode = 200,
            statusText = "OK",
            durationMs = 52L,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            requestBody = """{"email": "$email", "password": "••••••••"}""",
            responseBody = body
        )
    }

    suspend fun executeGetStudents(token: String?): ApiTestResult {
        if (token.isNullOrBlank()) {
            return ApiTestResult(
                endpoint = "/api/students",
                method = "GET",
                statusCode = 401,
                statusText = "UNAUTHORIZED",
                durationMs = 18L,
                requestHeaders = emptyMap(),
                requestBody = null,
                responseBody = """{"success": false, "message": "Not authorized, no token provided in Authorization header"}"""
            )
        }

        val students = studentDao.getAllStudents().first()
        val studentsJson = students.joinToString(",\n    ") {
            """{"_id": "${it.id}", "name": "${it.name}", "email": "${it.email}", "course": "${it.course}", "year": ${it.year}, "rollNumber": "${it.rollNumber}"}"""
        }

        val body = """
        {
          "success": true,
          "count": ${students.size},
          "data": [
            $studentsJson
          ]
        }
        """.trimIndent()

        return ApiTestResult(
            endpoint = "/api/students",
            method = "GET",
            statusCode = 200,
            statusText = "OK",
            durationMs = 38L,
            requestHeaders = mapOf("Authorization" to "Bearer ${token.take(20)}..."),
            requestBody = null,
            responseBody = body
        )
    }

    suspend fun executeCreateStudent(
        role: UserRole,
        token: String?,
        name: String,
        email: String,
        course: String,
        year: Int,
        rollNumber: String
    ): ApiTestResult {
        if (token.isNullOrBlank()) {
            return ApiTestResult(
                endpoint = "/api/students",
                method = "POST",
                statusCode = 401,
                statusText = "UNAUTHORIZED",
                durationMs = 15L,
                requestHeaders = emptyMap(),
                requestBody = null,
                responseBody = """{"success": false, "message": "Not authorized, token required"}"""
            )
        }

        if (role != UserRole.ADMIN) {
            return ApiTestResult(
                endpoint = "/api/students",
                method = "POST",
                statusCode = 403,
                statusText = "FORBIDDEN",
                durationMs = 24L,
                requestHeaders = mapOf(
                    "Authorization" to "Bearer ${token.take(20)}...",
                    "Content-Type" to "application/json"
                ),
                requestBody = """{"name": "$name", "email": "$email", "course": "$course", "year": $year, "rollNumber": "$rollNumber"}""",
                responseBody = """
                {
                  "success": false,
                  "message": "Forbidden: User role '${role.label}' is not authorized to access this resource"
                }
                """.trimIndent()
            )
        }

        val body = """
        {
          "success": true,
          "message": "Student added successfully",
          "data": {
            "_id": "std_${rollNumber.lowercase()}",
            "name": "$name",
            "email": "$email",
            "course": "$course",
            "year": $year,
            "rollNumber": "$rollNumber",
            "createdAt": "${java.time.Instant.now()}"
          }
        }
        """.trimIndent()

        return ApiTestResult(
            endpoint = "/api/students",
            method = "POST",
            statusCode = 201,
            statusText = "CREATED",
            durationMs = 60L,
            requestHeaders = mapOf(
                "Authorization" to "Bearer ${token.take(20)}...",
                "Content-Type" to "application/json"
            ),
            requestBody = """{"name": "$name", "email": "$email", "course": "$course", "year": $year, "rollNumber": "$rollNumber"}""",
            responseBody = body
        )
    }
}
