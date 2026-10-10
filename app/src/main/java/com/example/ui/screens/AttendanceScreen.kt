package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.data.repository.AttendanceRepository
import com.example.ui.ErpViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: ErpViewModel,
    modifier: Modifier = Modifier
) {
    val attendanceRecords by viewModel.filteredAttendance.collectAsState()
    val allRecords by viewModel.allAttendance.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val selectedSubject by viewModel.selectedAttendanceSubject.collectAsState()
    val selectedDate by viewModel.selectedAttendanceDate.collectAsState()

    val showMarkDialog by viewModel.showMarkAttendanceDialog.collectAsState()
    val studentForAttendance by viewModel.selectedStudentForAttendance.collectAsState()

    val subjects = listOf("All Subjects", "Data Structures", "Algorithms", "Database Systems", "Thermodynamics", "Power Circuits", "Business Ethics")
    val today = LocalDate.now().toString()
    val yesterday = LocalDate.now().minusDays(1).toString()

    val canMark = userRole == UserRole.TEACHER || userRole == UserRole.ADMIN

    // Attendance stats
    val overallPct = AttendanceRepository.calculatePercentage(attendanceRecords)
    val presentCount = attendanceRecords.count { it.status.equals("Present", ignoreCase = true) }
    val absentCount = attendanceRecords.count { it.status.equals("Absent", ignoreCase = true) }
    val lateCount = attendanceRecords.count { it.status.equals("Late", ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen")
    ) {
        // Attendance Overview & Analytics Card
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Attendance Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (canMark) "Teacher Mode: Select students below to mark attendance" else "Student Mode: Read-only attendance summary",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${"%.1f".format(overallPct)}% Rate",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats breakdown badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AttendancePill("Present: $presentCount", Color(0xFF10B981), Modifier.weight(1f))
                    AttendancePill("Late: $lateCount", Color(0xFFF59E0B), Modifier.weight(1f))
                    AttendancePill("Absent: $absentCount", Color(0xFFEF4444), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subject filter row
                Text("Filter by Subject:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { viewModel.selectedAttendanceSubject.value = subj },
                            label = { Text(subj, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date filter row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDate == today,
                        onClick = { viewModel.selectedAttendanceDate.value = today },
                        label = { Text("Today ($today)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedDate == yesterday,
                        onClick = { viewModel.selectedAttendanceDate.value = yesterday },
                        label = { Text("Yesterday", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedDate == "All Dates",
                        onClick = { viewModel.selectedAttendanceDate.value = "All Dates" },
                        label = { Text("All Dates", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Student List to Mark or View Attendance Records
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (canMark) "Enrolled Students (Tap to Mark/Update)" else "Attendance Records",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (canMark) {
                // List students so teacher can tap and mark attendance
                items(students, key = { it.id }) { student ->
                    val studentHistory = allRecords.filter { it.studentId == student.id }
                    val studentPct = AttendanceRepository.calculatePercentage(studentHistory)
                    val todayRecord = allRecords.find { it.studentId == student.id && it.date == today }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { viewModel.openMarkAttendanceDialog(student) },
                        modifier = Modifier.fillMaxWidth().testTag("mark_att_${student.rollNumber}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, fontWeight = FontWeight.Bold)
                                Text("${student.rollNumber} • ${student.course}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (todayRecord != null) {
                                StatusBadge(todayRecord.status)
                            } else {
                                TextButton(onClick = { viewModel.openMarkAttendanceDialog(student) }) {
                                    Text("Mark")
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${"%.0f".format(studentPct)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Student view: show records
                items(attendanceRecords, key = { it.id }) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(record.subject, fontWeight = FontWeight.Bold)
                                Text("${record.studentName} (${record.rollNumber})", style = MaterialTheme.typography.bodySmall)
                                Text("Date: ${record.date} • By: ${record.markedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(record.status)
                        }
                    }
                }
            }
        }
    }

    // Mark Attendance Dialog
    if (showMarkDialog && studentForAttendance != null) {
        MarkAttendanceDialog(
            student = studentForAttendance!!,
            subjectList = subjects.filter { it != "All Subjects" },
            onDismiss = { viewModel.showMarkAttendanceDialog.value = false },
            onSave = { status, subj, dt ->
                viewModel.recordAttendance(status, subj, dt)
            }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val bg = when (status.lowercase()) {
        "present" -> Color(0xFF10B981)
        "late" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text = status.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AttendancePill(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(text = label, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun MarkAttendanceDialog(
    student: StudentEntity,
    subjectList: List<String>,
    onDismiss: () -> Unit,
    onSave: (status: String, subject: String, date: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf("Present") }
    var selectedSubject by remember { mutableStateOf(subjectList.firstOrNull() ?: "Data Structures") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark Attendance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Student: ${student.name} (${student.rollNumber})", fontWeight = FontWeight.SemiBold)

                Text("Status:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Present", "Late", "Absent").forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st) }
                        )
                    }
                }

                Text("Subject:", style = MaterialTheme.typography.labelSmall)
                Column {
                    subjectList.take(4).forEach { subj ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(selected = selectedSubject == subj, onClick = { selectedSubject = subj })
                            Text(subj, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedStatus, selectedSubject, date) }) {
                Text("Record Attendance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
