package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.ExamResultEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.data.repository.ExamResultRepository
import com.example.data.repository.StudentPerformance
import com.example.ui.ErpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    viewModel: ErpViewModel,
    modifier: Modifier = Modifier
) {
    val resultsList by viewModel.filteredExamResults.collectAsState()
    val allResults by viewModel.allExamResults.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val subjectFilter by viewModel.examSubjectFilter.collectAsState()
    val studentSearch by viewModel.examStudentSearch.collectAsState()

    val showAddMarksDialog by viewModel.showAddMarksDialog.collectAsState()
    val selectedReportStudent by viewModel.selectedStudentForReportCard.collectAsState()

    val isTeacherOrAdmin = userRole == UserRole.TEACHER || userRole == UserRole.ADMIN
    val subjects = listOf("All Subjects", "Data Structures", "Algorithms", "Database Systems", "Thermodynamics", "Power Circuits", "Business Ethics")

    val overallAvg = if (allResults.isNotEmpty()) allResults.map { it.marks }.average() else 85.0
    val passCount = allResults.count { it.marks >= 50.0 }
    val passRate = if (allResults.isNotEmpty()) (passCount.toDouble() / allResults.size) * 100.0 else 100.0

    Scaffold(
        floatingActionButton = {
            if (isTeacherOrAdmin) {
                FloatingActionButton(
                    onClick = { viewModel.openAddMarksDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_marks_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Grade, contentDescription = "Record Marks")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Marks", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier.testTag("exams_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Exam Analytics Header
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Campus Average Score",
                            value = "${"%.1f".format(overallAvg)}%",
                            icon = Icons.Default.Analytics,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Pass Rate",
                            value = "${"%.1f".format(passRate)}%",
                            icon = Icons.Default.Verified,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = studentSearch,
                        onValueChange = { viewModel.examStudentSearch.value = it },
                        modifier = Modifier.fillMaxWidth().testTag("exam_search_input"),
                        placeholder = { Text("Search by student name or roll number...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (studentSearch.isNotEmpty()) {
                                IconButton(onClick = { viewModel.examStudentSearch.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subject Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.forEach { subj ->
                            FilterChip(
                                selected = subjectFilter == subj,
                                onClick = { viewModel.examSubjectFilter.value = subj },
                                label = { Text(subj, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            if (!isTeacherOrAdmin) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Student Mode: Viewing semester grades and performance report card.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(resultsList, key = { it.id }) { result ->
                    ExamResultCard(
                        result = result,
                        onViewReport = {
                            val st = students.find { it.id == result.studentId }
                            if (st != null) {
                                viewModel.selectedStudentForReportCard.value = st
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Marks Dialog (Teacher & Admin)
    if (showAddMarksDialog) {
        AddExamMarksDialog(
            students = students,
            subjectList = subjects.filter { it != "All Subjects" },
            onDismiss = { viewModel.showAddMarksDialog.value = false },
            onSave = { student, subject, marks, examType ->
                viewModel.recordExamMarks(student, subject, marks, examType)
            }
        )
    }

    // Student Performance Report Card Dialog
    if (selectedReportStudent != null) {
        val st = selectedReportStudent!!
        val studentResults = allResults.filter { it.studentId == st.id }
        val performance = ExamResultRepository.calculatePerformance(studentResults)

        StudentReportCardDialog(
            student = st,
            results = studentResults,
            performance = performance,
            onDismiss = { viewModel.selectedStudentForReportCard.value = null }
        )
    }
}

@Composable
fun ExamResultCard(
    result: ExamResultEntity,
    onViewReport: () -> Unit
) {
    val gradeColor = when (result.grade) {
        "A+", "A" -> Color(0xFF10B981)
        "B" -> Color(0xFF3B82F6)
        "C" -> Color(0xFFF59E0B)
        "D" -> Color(0xFFD97706)
        else -> Color(0xFFEF4444)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("exam_card_${result.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Grade badge
            Surface(
                shape = CircleShape,
                color = gradeColor.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = result.grade,
                        color = gradeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = result.subject, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "${result.studentName} (${result.rollNumber})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${result.examType} • Evaluated by ${result.addedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${"%.1f".format(result.marks)} / 100",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onViewReport) {
                    Text("Report Card", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddExamMarksDialog(
    students: List<StudentEntity>,
    subjectList: List<String>,
    onDismiss: () -> Unit,
    onSave: (student: StudentEntity, subject: String, marks: Double, examType: String) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var subject by remember { mutableStateOf(subjectList.firstOrNull() ?: "Data Structures") }
    var marksText by remember { mutableStateOf("88.0") }
    var examType by remember { mutableStateOf("Midterm") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Exam Marks", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Text("Select Student:", style = MaterialTheme.typography.labelSmall)
                students.take(4).forEach { st ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = selectedStudent?.id == st.id, onClick = { selectedStudent = st })
                        Text("${st.name} (${st.rollNumber})", fontSize = 13.sp)
                    }
                }

                Text("Subject:", style = MaterialTheme.typography.labelSmall)
                subjectList.take(4).forEach { subj ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = subject == subj, onClick = { subject = subj })
                        Text(subj, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = marksText,
                    onValueChange = { marksText = it; errorMsg = null },
                    label = { Text("Marks (0 - 100) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Exam Type:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Midterm", "Final", "Quiz").forEach { tp ->
                        FilterChip(
                            selected = examType == tp,
                            onClick = { examType = tp },
                            label = { Text(tp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = marksText.toDoubleOrNull()
                    if (selectedStudent == null || m == null || m < 0 || m > 100) {
                        errorMsg = "Please enter valid marks between 0 and 100"
                        return@Button
                    }
                    onSave(selectedStudent!!, subject, m, examType)
                }
            ) {
                Text("Save Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StudentReportCardDialog(
    student: StudentEntity,
    results: List<ExamResultEntity>,
    performance: StudentPerformance,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Academic Performance Report", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Roll: ${student.rollNumber} • ${student.course}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Overall Percentage", style = MaterialTheme.typography.labelSmall)
                                Text("${"%.1f".format(performance.percentage)}%", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Cumulative GPA", style = MaterialTheme.typography.labelSmall)
                                Text("${"%.2f".format(performance.gpa)} / 4.0", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Final Grade", style = MaterialTheme.typography.labelSmall)
                                Text(performance.overallGrade, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Text("Subject Breakdown:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                results.forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.subject, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("${"%.1f".format(r.marks)}/100", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(r.grade, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close Report") }
        }
    )
}
