package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FacultyEntity
import com.example.data.model.UserRole
import com.example.ui.ErpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyScreen(
    viewModel: ErpViewModel,
    modifier: Modifier = Modifier
) {
    val facultyList by viewModel.filteredFaculty.collectAsState()
    val searchQuery by viewModel.facultySearchQuery.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val showAddEdit by viewModel.showAddEditFacultyDialog.collectAsState()
    val editingFaculty by viewModel.editingFaculty.collectAsState()
    val facultyToDelete by viewModel.facultyToDelete.collectAsState()

    val isAdmin = userRole == UserRole.ADMIN

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { viewModel.openAddFacultyDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_faculty_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Faculty")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Faculty", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier.testTag("faculty_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Header
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateFacultySearch(it) },
                        modifier = Modifier.fillMaxWidth().testTag("faculty_search_input"),
                        placeholder = { Text("Search faculty by name, department, or subject...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateFacultySearch("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Security Banner
            if (!isAdmin) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Only Module: Faculty creation, assignment, and updates are restricted to Admins.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Faculty List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(facultyList, key = { it.id }) { faculty ->
                    FacultyCard(
                        faculty = faculty,
                        isAdmin = isAdmin,
                        onEdit = { viewModel.openEditFacultyDialog(faculty) },
                        onDelete = { viewModel.facultyToDelete.value = faculty }
                    )
                }
            }
        }
    }

    // Add / Edit Faculty Dialog
    if (showAddEdit) {
        AddEditFacultyDialog(
            faculty = editingFaculty,
            onDismiss = {
                viewModel.showAddEditFacultyDialog.value = false
                viewModel.editingFaculty.value = null
            },
            onSave = { name, dept, subjects, email, phone ->
                viewModel.saveFaculty(name, dept, subjects, email, phone)
            }
        )
    }

    // Delete Faculty Dialog
    if (facultyToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.facultyToDelete.value = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Faculty Record", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${facultyToDelete!!.name}' from the faculty registry?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeDeleteFaculty() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.facultyToDelete.value = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FacultyCard(
    faculty: FacultyEntity,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("faculty_card_${faculty.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = faculty.name.split(" ")
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .joinToString("")
                    .uppercase()

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = faculty.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = faculty.department, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (isAdmin) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Subjects Chips
            Text("Assigned Subjects:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                faculty.subjects.split(",").forEach { subj ->
                    val trimmed = subj.trim()
                    if (trimmed.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = trimmed,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = faculty.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(text = faculty.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AddEditFacultyDialog(
    faculty: FacultyEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, department: String, subjects: String, email: String, phone: String) -> Unit
) {
    val isEditing = faculty != null
    var name by remember { mutableStateOf(faculty?.name ?: "") }
    var department by remember { mutableStateOf(faculty?.department ?: "Computer Science") }
    var subjects by remember { mutableStateOf(faculty?.subjects ?: "Data Structures, Algorithms") }
    var email by remember { mutableStateOf(faculty?.email ?: "") }
    var phone by remember { mutableStateOf(faculty?.phone ?: "+1 (555) 789-0123") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Faculty Member" else "Add New Faculty", fontWeight = FontWeight.Bold) },
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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Faculty Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it; errorMsg = null },
                    label = { Text("Department *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subjects,
                    onValueChange = { subjects = it; errorMsg = null },
                    label = { Text("Assigned Subjects (comma-separated) *") },
                    placeholder = { Text("e.g. Operating Systems, Computer Networks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = null },
                    label = { Text("Institutional Email *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || department.isBlank() || subjects.isBlank() || email.isBlank()) {
                        errorMsg = "Please fill in all required fields"
                        return@Button
                    }
                    onSave(name.trim(), department.trim(), subjects.trim(), email.trim(), phone.trim())
                }
            ) {
                Text(if (isEditing) "Save Changes" else "Add Faculty")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
