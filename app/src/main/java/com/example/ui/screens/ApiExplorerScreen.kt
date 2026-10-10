package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.ErpViewModel
import com.example.ui.theme.RoleAdminColor
import com.example.ui.theme.RoleStudentColor
import com.example.ui.theme.RoleTeacherColor

@Composable
fun ApiExplorerScreen(
    viewModel: ErpViewModel,
    modifier: Modifier = Modifier
) {
    val apiResult by viewModel.apiTestResult.collectAsState()
    val isExecuting by viewModel.isExecutingApi.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val authToken by viewModel.authToken.collectAsState()

    var testStudentName by remember { mutableStateOf("Elena Rostova") }
    var testStudentEmail by remember { mutableStateOf("elena.r@campus.edu") }
    var testCourse by remember { mutableStateOf("Computer Science") }
    var testYear by remember { mutableStateOf(1) }
    var testRoll by remember { mutableStateOf("CS-2026-999") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("api_explorer_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Interactive REST API Sandbox",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trigger endpoints directly against the ERP engine. Observe headers, JWT bearer authentication, and status codes (200, 201, 401, 403 Forbidden).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Session Badge
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Active Authorization Header", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (authToken != null) "Bearer ${authToken!!.take(24)}..." else "No Token (Public)",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val roleColor = when (userRole) {
                        UserRole.ADMIN -> RoleAdminColor
                        UserRole.TEACHER -> RoleTeacherColor
                        UserRole.STUDENT -> RoleStudentColor
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = roleColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Role: ${userRole.label}",
                            color = roleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Test Endpoint 1: GET /api/test
        item {
            ApiEndpointCard(
                method = "GET",
                methodColor = Color(0xFF2563EB),
                path = "/api/test",
                description = "Basic server health ping & JSON parser verification (Public)",
                isExecuting = isExecuting,
                onExecute = { viewModel.runTestEndpoint() },
                testTag = "btn_test_get_test"
            )
        }

        // Test Endpoint 2: POST /api/auth/login
        item {
            ApiEndpointCard(
                method = "POST",
                methodColor = Color(0xFF16A34A),
                path = "/api/auth/login",
                description = "Authenticate user credentials & issue signed JWT token",
                isExecuting = isExecuting,
                onExecute = { viewModel.runAuthLoginTest() },
                testTag = "btn_test_auth_login"
            )
        }

        // Test Endpoint 3: GET /api/students
        item {
            ApiEndpointCard(
                method = "GET",
                methodColor = Color(0xFF2563EB),
                path = "/api/students",
                description = "Fetch all enrolled students (Protected: Admin, Teacher, Student)",
                isExecuting = isExecuting,
                onExecute = { viewModel.runGetStudentsTest() },
                testTag = "btn_test_get_students"
            )
        }

        // Test Endpoint 4: POST /api/students (RBAC Protected: Admin Only!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF16A34A)) {
                                Text(
                                    text = "POST",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(text = "/api/students", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Surface(shape = RoundedCornerShape(6.dp), color = RoleAdminColor.copy(alpha = 0.15f)) {
                            Text(
                                text = "ADMIN ONLY",
                                color = RoleAdminColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enroll a new student. Non-Admin roles return 403 Forbidden.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Inline payload inputs
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = testStudentName,
                            onValueChange = { testStudentName = it },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = testRoll,
                            onValueChange = { testRoll = it },
                            label = { Text("Roll No") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.runCreateStudentTest(
                                testStudentName,
                                testStudentEmail,
                                testCourse,
                                testYear,
                                testRoll
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_test_create_student")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send POST /api/students Request")
                    }
                }
            }
        }

        // Live Response Console
        item {
            Text(
                text = "Live HTTP Response Inspector",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (apiResult == null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "// Click any 'Send Request' button above to inspect response payload",
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                val res = apiResult!!
                val statusBg = when (res.statusCode) {
                    in 200..299 -> Color(0xFF10B981)
                    in 400..499 -> Color(0xFFEF4444)
                    else -> Color(0xFFF59E0B)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth().testTag("api_response_inspector")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(4.dp), color = statusBg) {
                                    Text(
                                        text = "${res.statusCode} ${res.statusText}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${res.method} ${res.endpoint}",
                                    color = Color(0xFFE2E8F0),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${res.durationMs}ms",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Response Body (JSON):",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF090D16), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = res.responseBody,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApiEndpointCard(
    method: String,
    methodColor: Color,
    path: String,
    description: String,
    isExecuting: Boolean,
    onExecute: () -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = methodColor) {
                        Text(
                            text = method,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = path, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                FilledTonalButton(
                    onClick = onExecute,
                    enabled = !isExecuting,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(testTag)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Request", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
