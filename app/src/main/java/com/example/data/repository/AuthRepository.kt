package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.UserDao
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserEntity, val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val userDao: UserDao) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    fun generateJwtSimulator(userId: String, role: String): String {
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        val payloadBody = """{"id":"$userId","role":"$role","exp":${(System.currentTimeMillis() / 1000) + 86400}}"""
        val payload = android.util.Base64.encodeToString(payloadBody.toByteArray(), android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        val sig = UUID.randomUUID().toString().replace("-", "").take(16)
        return "$header.$payload.$sig"
    }

    suspend fun login(identifier: String, password: String): AuthResult {
        if (identifier.isBlank() || password.isBlank()) {
            return AuthResult.Error("Please enter both User ID / Email and password")
        }

        val user = userDao.getUserByIdentifier(identifier.trim())
            ?: return AuthResult.Error("Invalid credentials: No account found for '$identifier'")

        val inputHash = AppDatabase.hashPassword(password)
        if (user.passwordHash != inputHash) {
            return AuthResult.Error("Invalid credentials: Password does not match")
        }

        val jwt = generateJwtSimulator(user.id, user.role)
        _currentUser.value = user
        _token.value = jwt
        return AuthResult.Success(user, jwt)
    }

    suspend fun register(name: String, email: String, password: String, role: String): AuthResult {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Please provide all required fields (Name, Email, Password)")
        }

        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters long")
        }

        val existing = userDao.getUserByEmail(email.trim())
        if (existing != null) {
            return AuthResult.Error("A user with email '${email.trim()}' already exists")
        }

        val newUser = UserEntity(
            id = "user_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = email.trim().lowercase(),
            passwordHash = AppDatabase.hashPassword(password),
            role = role,
            phone = "+1 (555) 000-0000",
            identifierNumber = if (role == "Admin") "ADM-2024-999" else if (role == "Teacher") "FAC-2024-999" else "STU-2024-999"
        )

        userDao.insertUser(newUser)
        val jwt = generateJwtSimulator(newUser.id, newUser.role)
        _currentUser.value = newUser
        _token.value = jwt
        return AuthResult.Success(newUser, jwt)
    }

    suspend fun adminCreateUser(
        name: String,
        email: String,
        password: String,
        role: String,
        identifier: String?
    ): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        if (name.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("All required fields must be filled"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long"))
        }
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("A user with email '$trimmedEmail' already exists"))
        }
        val idNum = identifier?.trim()?.ifEmpty { null } ?: when (role) {
            "Admin" -> "ADM-2024-${System.currentTimeMillis().toString().takeLast(4)}"
            "Teacher" -> "FAC-2024-${System.currentTimeMillis().toString().takeLast(4)}"
            else -> "STU-2024-${System.currentTimeMillis().toString().takeLast(4)}"
        }
        val newUser = UserEntity(
            id = "user_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = trimmedEmail,
            passwordHash = AppDatabase.hashPassword(password),
            role = role,
            phone = "+1 (555) 000-0000",
            identifierNumber = idNum
        )
        userDao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun updateProfile(
        name: String,
        phone: String,
        department: String,
        designation: String
    ): Result<UserEntity> {
        val current = _currentUser.value ?: return Result.failure(Exception("User not authenticated"))

        if (name.isBlank()) {
            return Result.failure(Exception("Full Name cannot be empty"))
        }

        // Validate phone number format
        val cleanPhone = phone.trim()
        if (cleanPhone.isNotEmpty() && cleanPhone.length < 7) {
            return Result.failure(Exception("Phone number must have at least 7 digits"))
        }

        val updated = current.copy(
            name = name.trim(),
            phone = cleanPhone,
            department = department.trim(),
            designation = designation.trim()
        )

        userDao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun updateAvatar(uriString: String?): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(Exception("User not authenticated"))
        userDao.updateAvatar(current.id, uriString)
        val updated = current.copy(avatarUri = uriString)
        _currentUser.value = updated
        return Result.success(Unit)
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String> {
        val current = _currentUser.value ?: return Result.failure(Exception("User not authenticated"))

        val oldHash = AppDatabase.hashPassword(oldPassword)
        if (current.passwordHash != oldHash) {
            return Result.failure(Exception("Current password is incorrect"))
        }

        if (newPassword.length < 6) {
            return Result.failure(Exception("New password must be at least 6 characters"))
        }

        val newHash = AppDatabase.hashPassword(newPassword)
        userDao.updatePassword(current.id, newHash)
        _currentUser.value = current.copy(passwordHash = newHash)
        return Result.success("Password updated securely")
    }

    suspend fun quickLoginRole(role: UserRole) {
        val email = when (role) {
            UserRole.ADMIN -> "admin@campus.edu"
            UserRole.TEACHER -> "teacher@campus.edu"
            UserRole.STUDENT -> "student@campus.edu"
        }
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            val jwt = generateJwtSimulator(user.id, user.role)
            _currentUser.value = user
            _token.value = jwt
        }
    }

    suspend fun loginWithGoogleAccount(googleEmail: String, displayName: String?) {
        val email = googleEmail.trim().lowercase()
        var user = userDao.getUserByEmail(email)
        if (user == null) {
            val newUser = UserEntity(
                id = "usr_g_" + UUID.randomUUID().toString().take(8),
                name = displayName?.takeIf { it.isNotBlank() } ?: "Campus User",
                email = email,
                passwordHash = "",
                role = "Admin",
                phone = "+1 (555) 234-5678",
                identifierNumber = "ADM-2024-G01",
                department = "Academic Administration",
                designation = "Campus Administrator"
            )
            userDao.insertUser(newUser)
            user = newUser
        }
        val jwt = generateJwtSimulator(user.id, user.role)
        _currentUser.value = user
        _token.value = jwt
    }

    fun logout() {
        _currentUser.value = null
        _token.value = null
    }
}
