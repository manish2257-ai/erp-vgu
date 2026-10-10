package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiTestResult
import com.example.data.api.RestApiSimulator
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class ErpTab(val label: String) {
    DASHBOARD("Dashboard"),
    STUDENTS("Students"),
    FACULTY("Faculty"),
    ATTENDANCE("Attendance"),
    FEES("Fees"),
    EXAMS("Exams & Results"),
    API_SANDBOX("API Tester"),
    DOCS("Docs"),
    PROFILE("Profile"),
    EDIT_PROFILE("Edit Profile"),
    SETTINGS("Settings")
}

data class DashboardAnalytics(
    val totalStudents: Int,
    val totalFaculty: Int,
    val attendanceAverage: Float,
    val totalFeesCollected: Double,
    val totalFeesPending: Double,
    val averageExamScore: Double,
    val passRatePercentage: Double
)

class ErpViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val authRepository = AuthRepository(database.userDao())
    val studentRepository = StudentRepository(database.studentDao())
    val facultyRepository = FacultyRepository(database.facultyDao())
    val attendanceRepository = AttendanceRepository(database.attendanceDao())
    val feeRepository = FeeRepository(database.feeDao())
    val examRepository = ExamResultRepository(database.examResultDao())
    val notificationRepository = NotificationRepository(database.notificationDao())
    val apiSimulator = RestApiSimulator(database.userDao(), database.studentDao())

    // Active Navigation Screen
    private val _currentTab = MutableStateFlow(ErpTab.DASHBOARD)
    val currentTab: StateFlow<ErpTab> = _currentTab.asStateFlow()
    private val screenHistory = mutableListOf<ErpTab>()

    // Auth & User State
    val currentUser: StateFlow<UserEntity?> = authRepository.currentUser
    val authToken: StateFlow<String?> = authRepository.token
    val userRole: StateFlow<UserRole> = currentUser
        .map { it?.toUserRole() ?: UserRole.ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserRole.ADMIN)

    // Notification Preferences
    val notifBulletins = MutableStateFlow(true)
    val notifGrades = MutableStateFlow(true)
    val notifSystemAlerts = MutableStateFlow(true)
    val notifEmailDigests = MutableStateFlow(false)

    // Role-filtered Notifications
    val notifications: StateFlow<List<NotificationEntity>> = userRole
        .flatMapLatest { role -> notificationRepository.getNotificationsForRole(role) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bulletins: StateFlow<List<NotificationEntity>> = notifications

    val unreadNotifCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Students
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedCourseFilter = MutableStateFlow("All")
    val selectedCourseFilter: StateFlow<String> = _selectedCourseFilter.asStateFlow()
    private val _selectedYearFilter = MutableStateFlow(0)
    val selectedYearFilter: StateFlow<Int> = _selectedYearFilter.asStateFlow()

    val allStudents: StateFlow<List<StudentEntity>> = studentRepository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStudents: StateFlow<List<StudentEntity>> = combine(
        allStudents,
        _searchQuery,
        _selectedCourseFilter,
        _selectedYearFilter
    ) { list, query, course, year ->
        list.filter { student ->
            val matchesQuery = if (query.isBlank()) true else {
                student.name.contains(query, ignoreCase = true) ||
                student.rollNumber.contains(query, ignoreCase = true) ||
                student.email.contains(query, ignoreCase = true) ||
                student.course.contains(query, ignoreCase = true)
            }
            val matchesCourse = if (course == "All") true else student.course.equals(course, ignoreCase = true)
            val matchesYear = if (year == 0) true else student.year == year
            matchesQuery && matchesCourse && matchesYear
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Faculty
    val allFaculty: StateFlow<List<FacultyEntity>> = facultyRepository.allFaculty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _facultySearchQuery = MutableStateFlow("")
    val facultySearchQuery: StateFlow<String> = _facultySearchQuery.asStateFlow()

    val filteredFaculty: StateFlow<List<FacultyEntity>> = combine(
        allFaculty,
        _facultySearchQuery
    ) { list, query ->
        if (query.isBlank()) list else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.department.contains(query, ignoreCase = true) ||
                it.subjects.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attendance
    val allAttendance: StateFlow<List<AttendanceEntity>> = attendanceRepository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val selectedAttendanceSubject = MutableStateFlow("All Subjects")
    val selectedAttendanceDate = MutableStateFlow(LocalDate.now().toString())

    val filteredAttendance: StateFlow<List<AttendanceEntity>> = combine(
        allAttendance,
        selectedAttendanceSubject,
        selectedAttendanceDate
    ) { list, subject, date ->
        list.filter {
            val matchSub = if (subject == "All Subjects") true else it.subject.equals(subject, ignoreCase = true)
            val matchDate = if (date.isBlank() || date == "All Dates") true else it.date == date
            matchSub && matchDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallAttendanceRate: StateFlow<Float> = allAttendance.map { records ->
        AttendanceRepository.calculatePercentage(records)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 92.5f)

    // Fees Module State
    val allFees: StateFlow<List<FeeEntity>> = feeRepository.allFees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalFeesCollected: StateFlow<Double> = feeRepository.totalPaid
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalFeesPending: StateFlow<Double> = feeRepository.totalPending
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val feeStatusFilter = MutableStateFlow("All")
    val feeSearchQuery = MutableStateFlow("")

    val filteredFees: StateFlow<List<FeeEntity>> = combine(
        allFees,
        feeStatusFilter,
        feeSearchQuery
    ) { list, status, query ->
        list.filter {
            val matchStatus = if (status == "All") true else it.status.equals(status, ignoreCase = true)
            val matchQuery = if (query.isBlank()) true else {
                it.studentName.contains(query, ignoreCase = true) ||
                it.rollNumber.contains(query, ignoreCase = true) ||
                it.feeType.contains(query, ignoreCase = true) ||
                it.receiptNumber.contains(query, ignoreCase = true)
            }
            matchStatus && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Exam Results Module State
    val allExamResults: StateFlow<List<ExamResultEntity>> = examRepository.allResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val averageExamScore: StateFlow<Double> = examRepository.averageScore
        .map { it ?: 85.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 85.0)

    val examSubjectFilter = MutableStateFlow("All Subjects")
    val examStudentSearch = MutableStateFlow("")

    val filteredExamResults: StateFlow<List<ExamResultEntity>> = combine(
        allExamResults,
        examSubjectFilter,
        examStudentSearch
    ) { list, subject, search ->
        list.filter {
            val matchSub = if (subject == "All Subjects") true else it.subject.equals(subject, ignoreCase = true)
            val matchSearch = if (search.isBlank()) true else {
                it.studentName.contains(search, ignoreCase = true) ||
                it.rollNumber.contains(search, ignoreCase = true)
            }
            matchSub && matchSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Combined Dashboard Analytics
    val dashboardAnalytics: StateFlow<DashboardAnalytics> = combine(
        combine(allStudents, allFaculty, overallAttendanceRate) { students, faculty, attRate ->
            Triple(students, faculty, attRate)
        },
        combine(totalFeesCollected, totalFeesPending, allExamResults) { paid, pending, exams ->
            Triple(paid, pending, exams)
        }
    ) { (students, faculty, attRate), (paid, pending, exams) ->
        val avgScore = if (exams.isNotEmpty()) exams.map { it.marks }.average() else 85.0
        val passCount = exams.count { it.marks >= 50.0 }
        val passRate = if (exams.isNotEmpty()) (passCount.toDouble() / exams.size) * 100.0 else 100.0

        DashboardAnalytics(
            totalStudents = students.size,
            totalFaculty = faculty.size,
            attendanceAverage = attRate,
            totalFeesCollected = paid,
            totalFeesPending = pending,
            averageExamScore = Math.round(avgScore * 10.0) / 10.0,
            passRatePercentage = Math.round(passRate * 10.0) / 10.0
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardAnalytics(5, 4, 92.5f, 9350.0, 5700.0, 89.6, 100.0)
    )

    // Dialog States
    val snackbarMessage = MutableStateFlow<String?>(null)
    val showAddEditDialog = MutableStateFlow(false)
    val editingStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudentDetails = MutableStateFlow<StudentEntity?>(null)
    val studentToDelete = MutableStateFlow<StudentEntity?>(null)

    val showAddEditFacultyDialog = MutableStateFlow(false)
    val editingFaculty = MutableStateFlow<FacultyEntity?>(null)
    val facultyToDelete = MutableStateFlow<FacultyEntity?>(null)

    val showMarkAttendanceDialog = MutableStateFlow(false)
    val selectedStudentForAttendance = MutableStateFlow<StudentEntity?>(null)

    val showAddFeeDialog = MutableStateFlow(false)
    val selectedFeeForReceipt = MutableStateFlow<FeeEntity?>(null)
    val selectedFeeForStatusUpdate = MutableStateFlow<FeeEntity?>(null)

    val showAddMarksDialog = MutableStateFlow(false)
    val selectedStudentForReportCard = MutableStateFlow<StudentEntity?>(null)

    val showRoleSwitchDialog = MutableStateFlow(false)
    val showProfileMenu = MutableStateFlow(false)
    val showBulletinsSheet = MutableStateFlow(false)
    val showChangePasswordDialog = MutableStateFlow(false)
    val showLogoutConfirmDialog = MutableStateFlow(false)

    // User authentication session status
    val isLoggedIn = MutableStateFlow(false)

    // API Sandbox
    val apiTestResult = MutableStateFlow<ApiTestResult?>(null)
    val isExecutingApi = MutableStateFlow(false)
    val isProfileSaving = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            AppDatabase.populateInitialData(
                database.userDao(),
                database.studentDao(),
                database.facultyDao(),
                database.attendanceDao(),
                database.feeDao(),
                database.examResultDao(),
                database.notificationDao()
            )
            // If Firebase session already exists, restore session
            val fbUser = Firebase.auth.currentUser
            if (fbUser != null) {
                authRepository.loginWithGoogleAccount(fbUser.email ?: "", fbUser.displayName)
                isLoggedIn.value = true
            }
        }
    }

    fun loginWithCredentials(
        userId: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = authRepository.login(userId, pass)) {
                is AuthResult.Success -> {
                    isLoggedIn.value = true
                    showMessage("Welcome back, ${result.user.name} (${result.user.role})")
                    onSuccess()
                }
                is AuthResult.Error -> {
                    onError(result.message)
                }
            }
        }
    }

    fun registerUser(
        name: String,
        email: String,
        pass: String,
        role: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = authRepository.register(name, email, pass, role)) {
                is AuthResult.Success -> {
                    isLoggedIn.value = true
                    showMessage("Account registered! Signed in as ${result.user.name}")
                    onSuccess()
                }
                is AuthResult.Error -> {
                    onError(result.message)
                }
            }
        }
    }

    fun adminCreateUser(
        name: String,
        email: String,
        pass: String,
        role: String,
        identifier: String?,
        onSuccess: (com.example.data.model.UserEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = authRepository.adminCreateUser(name, email, pass, role, identifier)
            res.onSuccess { user ->
                showMessage("Account created for ${user.name} (${user.role})")
                onSuccess(user)
            }.onFailure { ex ->
                onError(ex.message ?: "Failed to create user account")
            }
        }
    }

    fun onGoogleLoginSuccess(email: String, displayName: String?) {
        viewModelScope.launch {
            authRepository.loginWithGoogleAccount(email, displayName)
            isLoggedIn.value = true
            showMessage("Signed in with Google as $email")
        }
    }

    fun quickLoginRole(role: UserRole) {
        viewModelScope.launch {
            authRepository.quickLoginRole(role)
            isLoggedIn.value = true
            showMessage("Logged in as ${role.label}")
        }
    }

    fun navigateTo(tab: ErpTab) {
        if (_currentTab.value != tab) {
            screenHistory.add(_currentTab.value)
            _currentTab.value = tab
        }
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            _currentTab.value = screenHistory.removeAt(screenHistory.size - 1)
            return true
        } else if (_currentTab.value != ErpTab.DASHBOARD) {
            _currentTab.value = ErpTab.DASHBOARD
            return true
        }
        return false
    }

    fun selectTab(tab: ErpTab) { navigateTo(tab) }
    fun showMessage(msg: String) { snackbarMessage.value = msg }
    fun clearSnackbar() { snackbarMessage.value = null }

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            authRepository.quickLoginRole(role)
            isLoggedIn.value = true
            showMessage("Switched active profile to ${role.label}")
            showRoleSwitchDialog.value = false
        }
    }

    fun openRoleSwitchDialog() { showRoleSwitchDialog.value = true }
    fun closeRoleSwitchDialog() { showRoleSwitchDialog.value = false }
    fun viewStudentDetails(student: StudentEntity) { selectedStudentDetails.value = student }
    fun confirmDeleteStudent(student: StudentEntity) { studentToDelete.value = student }

    // User Profile
    fun saveUserProfile(name: String, phone: String, department: String, designation: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isProfileSaving.value = true
            val result = authRepository.updateProfile(name, phone, department, designation)
            isProfileSaving.value = false
            result.onSuccess {
                showMessage("Profile updated successfully")
                onSuccess()
            }.onFailure { showMessage(it.message ?: "Failed to update profile") }
        }
    }

    fun updateProfileAvatar(uriString: String?) {
        viewModelScope.launch {
            authRepository.updateAvatar(uriString).onSuccess {
                showMessage(if (uriString == null) "Photo removed" else "Photo updated")
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {
        if (newPass != confirmPass) {
            showMessage("Passwords do not match")
            return
        }
        viewModelScope.launch {
            authRepository.changePassword(oldPass, newPass).onSuccess {
                showMessage("Password changed successfully")
                showChangePasswordDialog.value = false
            }.onFailure { showMessage(it.message ?: "Update failed") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            try {
                Firebase.auth.signOut()
            } catch (_: Exception) {}
            isLoggedIn.value = false
            showLogoutConfirmDialog.value = false
            _currentTab.value = ErpTab.DASHBOARD
            screenHistory.clear()
            showMessage("Logged out successfully.")
        }
    }

    // Students CRUD
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun setCourseFilter(course: String) { _selectedCourseFilter.value = course }
    fun setYearFilter(year: Int) { _selectedYearFilter.value = year }

    fun openAddStudentDialog() {
        if (userRole.value != UserRole.ADMIN) {
            showMessage("403 Forbidden: Only Admin accounts can enroll students")
            return
        }
        editingStudent.value = null
        showAddEditDialog.value = true
    }

    fun openEditStudentDialog(student: StudentEntity) {
        if (userRole.value != UserRole.ADMIN) {
            showMessage("403 Forbidden: Only Admin accounts can modify student records")
            return
        }
        editingStudent.value = student
        showAddEditDialog.value = true
    }

    fun saveStudent(name: String, email: String, course: String, year: Int, rollNumber: String) {
        val currentRole = userRole.value
        val existing = editingStudent.value
        viewModelScope.launch {
            val result = if (existing == null) {
                studentRepository.addStudent(currentRole, name, email, course, year, rollNumber, currentUser.value?.id)
            } else {
                studentRepository.updateStudent(currentRole, existing.id, name, email, course, year, rollNumber)
            }
            when (result) {
                is StudentOpResult.Success -> {
                    showMessage(result.message)
                    showAddEditDialog.value = false
                    editingStudent.value = null
                }
                is StudentOpResult.Error -> showMessage(result.message)
            }
        }
    }

    fun executeDeleteStudent() {
        val target = studentToDelete.value ?: return
        viewModelScope.launch {
            when (val result = studentRepository.deleteStudent(userRole.value, target.id)) {
                is StudentOpResult.Success -> {
                    showMessage(result.message)
                    studentToDelete.value = null
                }
                is StudentOpResult.Error -> {
                    showMessage("Error: ${result.message}")
                    studentToDelete.value = null
                }
            }
        }
    }

    // Faculty CRUD
    fun updateFacultySearch(q: String) { _facultySearchQuery.value = q }
    fun openAddFacultyDialog() {
        if (userRole.value != UserRole.ADMIN) {
            showMessage("403 Forbidden: Only Admins can add faculty")
            return
        }
        editingFaculty.value = null
        showAddEditFacultyDialog.value = true
    }

    fun openEditFacultyDialog(faculty: FacultyEntity) {
        if (userRole.value != UserRole.ADMIN) {
            showMessage("403 Forbidden: Only Admins can update faculty")
            return
        }
        editingFaculty.value = faculty
        showAddEditFacultyDialog.value = true
    }

    fun saveFaculty(name: String, dept: String, subjects: String, email: String, phone: String) {
        val existing = editingFaculty.value
        viewModelScope.launch {
            val result = if (existing == null) {
                facultyRepository.addFaculty(userRole.value, name, dept, subjects, email, phone)
            } else {
                facultyRepository.updateFaculty(userRole.value, existing.id, name, dept, subjects, email, phone)
            }
            when (result) {
                is FacultyOpResult.Success -> {
                    showMessage(result.message)
                    showAddEditFacultyDialog.value = false
                    editingFaculty.value = null
                }
                is FacultyOpResult.Error -> showMessage(result.message)
            }
        }
    }

    fun executeDeleteFaculty() {
        val target = facultyToDelete.value ?: return
        viewModelScope.launch {
            when (val result = facultyRepository.deleteFaculty(userRole.value, target.id)) {
                is FacultyOpResult.Success -> {
                    showMessage(result.message)
                    facultyToDelete.value = null
                }
                is FacultyOpResult.Error -> {
                    showMessage("Error: ${result.message}")
                    facultyToDelete.value = null
                }
            }
        }
    }

    // Attendance
    fun openMarkAttendanceDialog(student: StudentEntity) {
        if (userRole.value == UserRole.STUDENT) {
            showMessage("403 Forbidden: Students cannot mark attendance. Faculty & Admins only.")
            return
        }
        selectedStudentForAttendance.value = student
        showMarkAttendanceDialog.value = true
    }

    fun recordAttendance(status: String, subject: String, date: String) {
        val student = selectedStudentForAttendance.value ?: return
        val facultyName = currentUser.value?.name ?: "Faculty"
        viewModelScope.launch {
            when (val result = attendanceRepository.markAttendance(
                userRole.value, student.id, student.name, student.rollNumber, subject, date, status, facultyName
            )) {
                is AttendanceOpResult.Success -> {
                    showMessage(result.message)
                    showMarkAttendanceDialog.value = false
                    selectedStudentForAttendance.value = null
                }
                is AttendanceOpResult.Error -> showMessage(result.message)
            }
        }
    }

    // Fees Module Operations
    fun openAddFeeDialog() {
        if (userRole.value != UserRole.ADMIN) {
            showMessage("403 Forbidden: Only Admin accounts can create fee records")
            return
        }
        showAddFeeDialog.value = true
    }

    fun createFeeRecord(
        student: StudentEntity,
        feeType: String,
        amount: Double,
        status: String,
        paymentDate: String,
        method: String
    ) {
        viewModelScope.launch {
            when (val result = feeRepository.addFeeRecord(
                userRole.value, student.id, student.name, student.rollNumber, feeType, amount, status, paymentDate, method
            )) {
                is FeeOpResult.Success -> {
                    showMessage(result.message)
                    showAddFeeDialog.value = false
                }
                is FeeOpResult.Error -> showMessage(result.message)
            }
        }
    }

    fun updateFeeStatus(feeId: String, newStatus: String, paymentDate: String, method: String) {
        viewModelScope.launch {
            when (val result = feeRepository.updatePaymentStatus(userRole.value, feeId, newStatus, paymentDate, method)) {
                is FeeOpResult.Success -> {
                    showMessage(result.message)
                    selectedFeeForStatusUpdate.value = null
                }
                is FeeOpResult.Error -> showMessage(result.message)
            }
        }
    }

    // Exams & Results Operations
    fun openAddMarksDialog() {
        if (userRole.value == UserRole.STUDENT) {
            showMessage("403 Forbidden: Students cannot record marks. Teachers only.")
            return
        }
        showAddMarksDialog.value = true
    }

    fun recordExamMarks(
        student: StudentEntity,
        subject: String,
        marks: Double,
        examType: String
    ) {
        val facultyName = currentUser.value?.name ?: "Faculty"
        viewModelScope.launch {
            when (val result = examRepository.addExamMarks(
                userRole.value, student.id, student.name, student.rollNumber, subject, marks, examType, facultyName
            )) {
                is ExamOpResult.Success -> {
                    showMessage(result.message)
                    showAddMarksDialog.value = false
                }
                is ExamOpResult.Error -> showMessage(result.message)
            }
        }
    }

    // Notifications Operations
    fun markNotificationAsRead(id: String) {
        viewModelScope.launch { notificationRepository.markAsRead(id) }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            showMessage("All notifications marked as read")
        }
    }

    // API Sandbox operations
    fun runTestEndpoint() {
        viewModelScope.launch {
            isExecutingApi.value = true
            apiTestResult.value = apiSimulator.executeTestEndpoint()
            isExecutingApi.value = false
        }
    }

    fun runAuthLoginTest() {
        viewModelScope.launch {
            isExecutingApi.value = true
            val email = currentUser.value?.email ?: "admin@campus.edu"
            apiTestResult.value = apiSimulator.executeAuthLogin(email, userRole.value.label, authToken.value)
            isExecutingApi.value = false
        }
    }

    fun runGetStudentsTest() {
        viewModelScope.launch {
            isExecutingApi.value = true
            apiTestResult.value = apiSimulator.executeGetStudents(authToken.value)
            isExecutingApi.value = false
        }
    }

    fun runCreateStudentTest(name: String, email: String, course: String, year: Int, rollNumber: String) {
        viewModelScope.launch {
            isExecutingApi.value = true
            apiTestResult.value = apiSimulator.executeCreateStudent(
                userRole.value, authToken.value, name, email, course, year, rollNumber
            )
            isExecutingApi.value = false
        }
    }
}
