package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: ErpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusErpTheme {
                val firebaseAuth = com.google.firebase.Firebase.auth
                var firebaseUser by remember { mutableStateOf(firebaseAuth.currentUser) }
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                DisposableEffect(Unit) {
                    val authListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
                        firebaseUser = auth.currentUser
                        if (auth.currentUser != null) {
                            viewModel.onGoogleLoginSuccess(
                                auth.currentUser?.email ?: "",
                                auth.currentUser?.displayName
                            )
                        }
                    }
                    firebaseAuth.addAuthStateListener(authListener)
                    onDispose {
                        firebaseAuth.removeAuthStateListener(authListener)
                    }
                }

                val isAuthenticated = isLoggedIn || firebaseUser != null

                if (!isAuthenticated) {
                    LoginScreen(
                        viewModel = viewModel,
                        onAuthSuccess = {
                            firebaseUser = firebaseAuth.currentUser
                        }
                    )
                } else {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ErpViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val showAddEditDialog by viewModel.showAddEditDialog.collectAsState()
    val editingStudent by viewModel.editingStudent.collectAsState()
    val selectedStudentDetails by viewModel.selectedStudentDetails.collectAsState()
    val studentToDelete by viewModel.studentToDelete.collectAsState()
    val showLogoutConfirmDialog by viewModel.showLogoutConfirmDialog.collectAsState()
    val showRoleSwitchDialog by viewModel.showRoleSwitchDialog.collectAsState()
    val showProfileMenu by viewModel.showProfileMenu.collectAsState()
    val showBulletinsSheet by viewModel.showBulletinsSheet.collectAsState()
    val bulletins by viewModel.bulletins.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Android Photo Picker launcher (zero permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfileAvatar(uri.toString())
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Predictive / system back handling
    BackHandler(enabled = currentTab != ErpTab.DASHBOARD) {
        viewModel.navigateBack()
    }

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

    // Determine if TopAppBar and BottomBar should be shown (hide on dedicated sub-screens)
    val isTopLevelScreen = currentTab == ErpTab.DASHBOARD ||
            currentTab == ErpTab.STUDENTS ||
            currentTab == ErpTab.FACULTY ||
            currentTab == ErpTab.ATTENDANCE ||
            currentTab == ErpTab.FEES ||
            currentTab == ErpTab.EXAMS ||
            currentTab == ErpTab.API_SANDBOX ||
            currentTab == ErpTab.DOCS

    Scaffold(
        topBar = {
            if (isTopLevelScreen) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Campus ERP",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "College ERP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        // Notification Bell Icon with Badge
                        IconButton(
                            onClick = { viewModel.showBulletinsSheet.value = true },
                            modifier = Modifier.testTag("top_bar_notification_bell")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Role Indicator Pill (Admin / Teacher / Student)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = roleColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clickable { viewModel.showProfileMenu.value = true }
                                .testTag("top_bar_role_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when (userRole) {
                                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                        UserRole.TEACHER -> Icons.Default.School
                                        UserRole.STUDENT -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = userRole.label,
                                    color = roleColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Circular User Profile Avatar (Clickable)
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { viewModel.showProfileMenu.value = true }
                                .testTag("top_bar_profile_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == ErpTab.DASHBOARD,
                        onClick = { viewModel.navigateTo(ErpTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentTab == ErpTab.STUDENTS,
                        onClick = { viewModel.navigateTo(ErpTab.STUDENTS) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                        label = { Text("Students", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_students")
                    )
                    NavigationBarItem(
                        selected = currentTab == ErpTab.ATTENDANCE,
                        onClick = { viewModel.navigateTo(ErpTab.ATTENDANCE) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Attendance") },
                        label = { Text("Attendance", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_attendance")
                    )
                    NavigationBarItem(
                        selected = currentTab == ErpTab.FEES,
                        onClick = { viewModel.navigateTo(ErpTab.FEES) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Fees") },
                        label = { Text("Fees", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_fees")
                    )
                    NavigationBarItem(
                        selected = currentTab == ErpTab.EXAMS,
                        onClick = { viewModel.navigateTo(ErpTab.EXAMS) },
                        icon = { Icon(Icons.Default.Grade, contentDescription = "Exams") },
                        label = { Text("Exams", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_tab_exams")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ErpTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                ErpTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                ErpTab.FACULTY -> FacultyScreen(viewModel = viewModel)
                ErpTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                ErpTab.FEES -> FeesScreen(viewModel = viewModel)
                ErpTab.EXAMS -> ExamsScreen(viewModel = viewModel)
                ErpTab.API_SANDBOX -> ApiExplorerScreen(viewModel = viewModel)
                ErpTab.DOCS -> DocsScreen()
                ErpTab.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                ErpTab.EDIT_PROFILE -> EditProfileScreen(
                    viewModel = viewModel,
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                ErpTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Profile & Account Quick Menu Dialog
    if (showProfileMenu) {
        AlertDialog(
            onDismissRequest = { viewModel.showProfileMenu.value = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(roleColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, fontWeight = FontWeight.Bold, color = roleColor, fontSize = 16.sp)
                    }
                    Column {
                        Text(currentUser?.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(currentUser?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ListItem(
                        headlineContent = { Text("View Profile", fontWeight = FontWeight.Medium) },
                        leadingContent = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .clickable {
                                viewModel.showProfileMenu.value = false
                                viewModel.navigateTo(ErpTab.PROFILE)
                            }
                            .testTag("menu_view_profile")
                    )
                    ListItem(
                        headlineContent = { Text("Edit Profile", fontWeight = FontWeight.Medium) },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        modifier = Modifier
                            .clickable {
                                viewModel.showProfileMenu.value = false
                                viewModel.navigateTo(ErpTab.EDIT_PROFILE)
                            }
                            .testTag("menu_edit_profile")
                    )
                    ListItem(
                        headlineContent = { Text("Account Settings", fontWeight = FontWeight.Medium) },
                        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                        modifier = Modifier
                            .clickable {
                                viewModel.showProfileMenu.value = false
                                viewModel.navigateTo(ErpTab.SETTINGS)
                            }
                            .testTag("menu_account_settings")
                    )
                    ListItem(
                        headlineContent = { Text("Switch Role (RBAC)", fontWeight = FontWeight.Medium) },
                        leadingContent = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                        modifier = Modifier
                            .clickable {
                                viewModel.showProfileMenu.value = false
                                viewModel.openRoleSwitchDialog()
                            }
                            .testTag("menu_switch_role")
                    )
                    ListItem(
                        headlineContent = { Text("Log Out", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier
                            .clickable {
                                viewModel.showProfileMenu.value = false
                                viewModel.showLogoutConfirmDialog.value = true
                            }
                            .testTag("menu_logout")
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showProfileMenu.value = false }) { Text("Close") }
            }
        )
    }

    // Campus Bulletins & Notices Modal
    if (showBulletinsSheet) {
        AlertDialog(
            onDismissRequest = { viewModel.showBulletinsSheet.value = false },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Campus Bulletins & Notices", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    bulletins.forEach { bulletin ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = bulletin.targetRole,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = bulletin.senderName,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = bulletin.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = bulletin.message, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.showBulletinsSheet.value = false }) { Text("Dismiss") }
            }
        )
    }

    // Modal Student Dialogs
    if (showAddEditDialog) {
        AddEditStudentDialog(
            student = editingStudent,
            onDismiss = {
                viewModel.showAddEditDialog.value = false
                viewModel.editingStudent.value = null
            },
            onSave = { name, email, course, year, rollNumber ->
                viewModel.saveStudent(name, email, course, year, rollNumber)
            }
        )
    }

    if (studentToDelete != null) {
        DeleteConfirmDialog(
            student = studentToDelete!!,
            onConfirm = { viewModel.executeDeleteStudent() },
            onDismiss = { viewModel.studentToDelete.value = null }
        )
    }

    if (selectedStudentDetails != null) {
        StudentDetailsDialog(
            student = selectedStudentDetails!!,
            onDismiss = { viewModel.selectedStudentDetails.value = null }
        )
    }

    if (showRoleSwitchDialog) {
        RoleSwitchDialog(
            currentRole = userRole,
            onSelectRole = { newRole ->
                viewModel.switchRole(newRole)
            },
            onDismiss = { viewModel.closeRoleSwitchDialog() }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showLogoutConfirmDialog.value = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your session? You will return to the sign in portal.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_logout_dialog_btn")
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showLogoutConfirmDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
