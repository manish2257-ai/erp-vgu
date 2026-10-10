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
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.UserRole
import com.example.ui.ErpTab
import com.example.ui.ErpViewModel
import com.example.ui.theme.RoleAdminColor
import com.example.ui.theme.RoleStudentColor
import com.example.ui.theme.RoleTeacherColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ErpViewModel,
    modifier: Modifier = Modifier,
    onPickPhoto: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    val roleColor = when (userRole) {
        UserRole.ADMIN -> RoleAdminColor
        UserRole.TEACHER -> RoleTeacherColor
        UserRole.STUDENT -> RoleStudentColor
    }

    val initials = (currentUser?.name ?: "User")
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.toString() }
        .joinToString("")
        .uppercase()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("profile_back_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ErpTab.EDIT_PROFILE) },
                        modifier = Modifier.testTag("profile_edit_top_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card with Avatar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Avatar with edit overlay
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.testTag("user_profile_avatar")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(roleColor.copy(alpha = 0.15f))
                                .clickable { showPhotoOptionsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Bold,
                                color = roleColor,
                                fontSize = 34.sp
                            )
                        }

                        // Camera icon overlay
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { showPhotoOptionsDialog = true }
                                .testTag("change_photo_badge")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Change Photo",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentUser?.name ?: "User",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = roleColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(shape = CircleShape, color = roleColor, modifier = Modifier.size(8.dp)) {}
                            Text(
                                text = userRole.label.uppercase(),
                                color = roleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentUser?.designation ?: "Designation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.navigateTo(ErpTab.EDIT_PROFILE) },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_main_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile Details")
                    }
                }
            }

            // Academic & Contact Information
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Contact & Institutional Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    ProfileInfoRow(
                        icon = Icons.Default.Badge,
                        label = if (userRole == UserRole.STUDENT) "Student Roll / ID" else "Employee Identifier",
                        value = currentUser?.identifierNumber ?: "N/A"
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Institutional Email",
                        value = currentUser?.email ?: "N/A"
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Phone,
                        label = "Phone Number",
                        value = currentUser?.phone ?: "Not registered"
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Apartment,
                        label = if (userRole == UserRole.STUDENT) "Enrolled Department / Course" else "Faculty Department",
                        value = currentUser?.department ?: "N/A"
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Work,
                        label = "Academic Standing / Designation",
                        value = currentUser?.designation ?: "N/A"
                    )
                }
            }

            // Quick Account Settings Button
            OutlinedButton(
                onClick = { viewModel.navigateTo(ErpTab.SETTINGS) },
                modifier = Modifier.fillMaxWidth().testTag("goto_settings_btn")
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Account Settings & Security")
            }
        }
    }

    // Photo Options Dialog (Change / Remove Photo)
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = { Text("Profile Picture Options", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a new photo or reset your current avatar.")
                    ListItem(
                        headlineContent = { Text("Choose New Photo") },
                        supportingContent = { Text("Select from device gallery or camera") },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showPhotoOptionsDialog = false
                            onPickPhoto()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Remove Photo") },
                        supportingContent = { Text("Reset to default role initials avatar") },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            showPhotoOptionsDialog = false
                            viewModel.updateProfileAvatar(null)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
