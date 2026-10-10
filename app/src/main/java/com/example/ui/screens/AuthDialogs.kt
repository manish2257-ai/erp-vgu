package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.ui.ErpViewModel
import com.example.ui.theme.RoleAdminColor
import com.example.ui.theme.RoleStudentColor
import com.example.ui.theme.RoleTeacherColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    student: StudentEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, course: String, year: Int, rollNumber: String) -> Unit
) {
    val isEditing = student != null
    var name by remember { mutableStateOf(student?.name ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var course by remember { mutableStateOf(student?.course ?: "Computer Science") }
    var year by remember { mutableStateOf(student?.year ?: 1) }
    var rollNumber by remember { mutableStateOf(student?.rollNumber ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val courseOptions = listOf(
        "Computer Science",
        "Mechanical Engineering",
        "Electrical Engineering",
        "Business Administration",
        "Information Technology"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Student Record" else "Enroll New Student",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Student Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_student_name")
                )

                // Roll Number
                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it.uppercase(); errorMessage = null },
                    label = { Text("Roll Number * (e.g. CS-2024-001)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_student_roll")
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Institutional Email *") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_student_email")
                )

                // Course Selection
                Text("Select Course / Major", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    courseOptions.forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { course = opt }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = course == opt,
                                onClick = { course = opt }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 14.sp)
                        }
                    }
                }

                // Year Selection
                Text("Academic Year", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..4).forEach { yr ->
                        FilterChip(
                            selected = year == yr,
                            onClick = { year = yr },
                            label = { Text("Year $yr") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || rollNumber.isBlank() || email.isBlank() || course.isBlank()) {
                        errorMessage = "Please fill in all mandatory fields"
                        return@Button
                    }
                    if (!email.contains("@") || !email.contains(".")) {
                        errorMessage = "Please enter a valid email address"
                        return@Button
                    }
                    onSave(name.trim(), email.trim(), course.trim(), year, rollNumber.trim().uppercase())
                },
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Enroll Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    student: StudentEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(text = "Confirm Student Deletion", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = "Are you sure you want to permanently delete student '${student.name}' (Roll No: ${student.rollNumber})? This operation cannot be undone."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_btn")
            ) {
                Text("Delete Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StudentDetailsDialog(
    student: StudentEntity,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val enrolledDate = dateFormat.format(Date(student.createdAt))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(text = student.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Roll: ${student.rollNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailItem(label = "Internal Database ID", value = student.id)
                DetailItem(label = "Institutional Email", value = student.email)
                DetailItem(label = "Department / Course", value = student.course)
                DetailItem(label = "Academic Standing", value = "Year ${student.year} Undergrad")
                DetailItem(label = "Enrollment Timestamp", value = enrolledDate)
                DetailItem(label = "Enrolled By", value = student.createdBy ?: "Admin (System Seed)")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RoleSwitchDialog(
    currentRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Switch User Role", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Test Role-Based Access Control (RBAC) across different account privileges:",
                    style = MaterialTheme.typography.bodySmall
                )

                RoleOptionCard(
                    role = UserRole.ADMIN,
                    accountName = "Dean Arthur Vance (admin@campus.edu)",
                    privileges = "Full CRUD: Add, update, delete students, full API access",
                    color = RoleAdminColor,
                    isSelected = currentRole == UserRole.ADMIN,
                    onClick = { onSelectRole(UserRole.ADMIN) }
                )

                RoleOptionCard(
                    role = UserRole.TEACHER,
                    accountName = "Prof. Eleanor Wright (teacher@campus.edu)",
                    privileges = "Read-Only: View student lists & details, modifications restricted",
                    color = RoleTeacherColor,
                    isSelected = currentRole == UserRole.TEACHER,
                    onClick = { onSelectRole(UserRole.TEACHER) }
                )

                RoleOptionCard(
                    role = UserRole.STUDENT,
                    accountName = "Marcus Brody (student@campus.edu)",
                    privileges = "Student Portal: Read-only directory access",
                    color = RoleStudentColor,
                    isSelected = currentRole == UserRole.STUDENT,
                    onClick = { onSelectRole(UserRole.STUDENT) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun RoleOptionCard(
    role: UserRole,
    accountName: String,
    privileges: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("switch_to_${role.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = CircleShape, color = color, modifier = Modifier.size(10.dp)) {}
                    Text(text = role.label, fontWeight = FontWeight.Bold, color = color)
                }
                if (isSelected) {
                    Text(text = "Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = accountName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(text = privileges, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
