package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DocsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("docs_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = "Campus ERP API Documentation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Full REST API and CRUD specifications for Node.js, Express, MongoDB, and Mongoose integration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Text(text = "1. Role-Based Access Control (RBAC) Matrix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleMatrixRow(method = "GET", endpoint = "/api/test", role = "Public (All)")
                    RoleMatrixRow(method = "POST", endpoint = "/api/auth/register", role = "Public")
                    RoleMatrixRow(method = "POST", endpoint = "/api/auth/login", role = "Public")
                    RoleMatrixRow(method = "GET", endpoint = "/api/auth/me", role = "Admin, Teacher, Student")
                    RoleMatrixRow(method = "GET", endpoint = "/api/students", role = "Admin, Teacher, Student")
                    RoleMatrixRow(method = "GET", endpoint = "/api/students/:id", role = "Admin, Teacher, Student")
                    RoleMatrixRow(method = "POST", endpoint = "/api/students", role = "ADMIN ONLY (403 for others)", highlight = true)
                    RoleMatrixRow(method = "PUT", endpoint = "/api/students/:id", role = "ADMIN ONLY (403 for others)", highlight = true)
                    RoleMatrixRow(method = "DELETE", endpoint = "/api/students/:id", role = "ADMIN ONLY (403 for others)", highlight = true)
                }
            }
        }

        item {
            Text(text = "2. Student Schema (MongoDB & Room)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            CodeBlock(
                code = """
                // Mongoose Schema (models/Student.js)
                const StudentSchema = new mongoose.Schema({
                  name: { type: String, required: true, trim: true },
                  email: { type: String, required: true, unique: true },
                  course: { type: String, required: true },
                  year: { type: Number, required: true, min: 1, max: 6 },
                  rollNumber: { type: String, required: true, unique: true, uppercase: true },
                  createdBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' }
                }, { timestamps: true });
                """.trimIndent()
            )
        }

        item {
            Text(text = "3. Endpoints & Sample Payloads", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Text(text = "POST /api/students (Add Student - Admin only)", style = MaterialTheme.typography.labelMedium)
            CodeBlock(
                code = """
                // Request Body (JSON)
                {
                  "name": "Alex Mercer",
                  "email": "alex.mercer@campus.edu",
                  "course": "Computer Science",
                  "year": 3,
                  "rollNumber": "CS-2024-001"
                }

                // Response (201 Created)
                {
                  "success": true,
                  "message": "Student added successfully",
                  "data": {
                    "_id": "6614b0557a0d1b32f5e10015",
                    "name": "Alex Mercer",
                    "rollNumber": "CS-2024-001",
                    "course": "Computer Science",
                    "year": 3
                  }
                }
                """.trimIndent()
            )
        }

        item {
            Text(text = "GET /api/students (List Students)", style = MaterialTheme.typography.labelMedium)
            CodeBlock(
                code = """
                // Headers
                Authorization: Bearer <jwt_token>

                // Response (200 OK)
                {
                  "success": true,
                  "count": 5,
                  "data": [
                    {
                      "_id": "std_001",
                      "name": "Alex Mercer",
                      "email": "alex.mercer@campus.edu",
                      "course": "Computer Science",
                      "year": 3,
                      "rollNumber": "CS-2024-001"
                    }
                  ]
                }
                """.trimIndent()
            )
        }
    }
}

@Composable
fun RoleMatrixRow(method: String, endpoint: String, role: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val color = when (method) {
                "GET" -> Color(0xFF2563EB)
                "POST" -> Color(0xFF16A34A)
                "PUT" -> Color(0xFFD97706)
                "DELETE" -> Color(0xFFDC2626)
                else -> Color.Gray
            }
            Surface(shape = RoundedCornerShape(4.dp), color = color) {
                Text(text = method, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }
            Text(text = endpoint, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
        Text(
            text = role,
            fontSize = 11.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .padding(12.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = code,
            color = Color(0xFF38BDF8),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
