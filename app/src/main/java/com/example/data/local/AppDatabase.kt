package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceEntity
import com.example.data.model.ExamResultEntity
import com.example.data.model.FacultyEntity
import com.example.data.model.FeeEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.time.LocalDate

@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        FacultyEntity::class,
        AttendanceEntity::class,
        FeeEntity::class,
        ExamResultEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun facultyDao(): FacultyDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feeDao(): FeeDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_erp.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(
                            database.userDao(),
                            database.studentDao(),
                            database.facultyDao(),
                            database.attendanceDao(),
                            database.feeDao(),
                            database.examResultDao(),
                            database.notificationDao()
                        )
                    }
                }
            }
        }

        suspend fun populateInitialData(
            userDao: UserDao,
            studentDao: StudentDao,
            facultyDao: FacultyDao,
            attendanceDao: AttendanceDao,
            feeDao: FeeDao,
            examResultDao: ExamResultDao,
            notificationDao: NotificationDao
        ) {
            if (userDao.countUsers() == 0) {
                // Seed Admin User
                userDao.insertUser(
                    UserEntity(
                        id = "user_admin_001",
                        name = "Dean Arthur Vance",
                        email = "admin@campus.edu",
                        passwordHash = hashPassword("admin123"),
                        role = "Admin",
                        phone = "+1 (555) 234-5678",
                        identifierNumber = "ADM-2024-001",
                        department = "Office of Academic Affairs",
                        designation = "Dean of Academic Administration"
                    )
                )

                // Seed Teacher / Faculty User
                userDao.insertUser(
                    UserEntity(
                        id = "user_teacher_001",
                        name = "Prof. Eleanor Wright",
                        email = "teacher@campus.edu",
                        passwordHash = hashPassword("teacher123"),
                        role = "Teacher",
                        phone = "+1 (555) 789-0123",
                        identifierNumber = "FAC-CS-042",
                        department = "Computer Science & Engineering",
                        designation = "Associate Professor"
                    )
                )

                // Seed Student User
                userDao.insertUser(
                    UserEntity(
                        id = "user_student_001",
                        name = "Marcus Brody",
                        email = "student@campus.edu",
                        passwordHash = hashPassword("student123"),
                        role = "Student",
                        phone = "+1 (555) 456-7890",
                        identifierNumber = "STU-2024-001",
                        department = "Computer Science",
                        designation = "Year 3 Undergraduate (Semester 6)"
                    )
                )

                // Seed Students
                val students = listOf(
                    StudentEntity(
                        id = "std_001",
                        name = "Alex Mercer",
                        email = "alex.mercer@campus.edu",
                        course = "Computer Science",
                        year = 3,
                        rollNumber = "CS-2024-001",
                        createdBy = "user_admin_001"
                    ),
                    StudentEntity(
                        id = "std_002",
                        name = "Maya Lin",
                        email = "maya.lin@campus.edu",
                        course = "Mechanical Engineering",
                        year = 2,
                        rollNumber = "ME-2024-042",
                        createdBy = "user_admin_001"
                    ),
                    StudentEntity(
                        id = "std_003",
                        name = "Sophia Chen",
                        email = "sophia.chen@campus.edu",
                        course = "Electrical Engineering",
                        year = 4,
                        rollNumber = "EE-2023-018",
                        createdBy = "user_admin_001"
                    ),
                    StudentEntity(
                        id = "std_004",
                        name = "Liam O'Connor",
                        email = "liam.oc@campus.edu",
                        course = "Business Administration",
                        year = 1,
                        rollNumber = "BA-2025-110",
                        createdBy = "user_admin_001"
                    ),
                    StudentEntity(
                        id = "std_005",
                        name = "Zara Patel",
                        email = "zara.patel@campus.edu",
                        course = "Computer Science",
                        year = 2,
                        rollNumber = "CS-2025-089",
                        createdBy = "user_admin_001"
                    )
                )
                students.forEach { studentDao.insertStudent(it) }

                // Seed Faculty Members
                val facultyMembers = listOf(
                    FacultyEntity(
                        id = "fac_001",
                        name = "Prof. Eleanor Wright",
                        department = "Computer Science",
                        subjects = "Data Structures, Database Systems",
                        email = "teacher@campus.edu",
                        phone = "+1 (555) 789-0123"
                    ),
                    FacultyEntity(
                        id = "fac_002",
                        name = "Dr. Alan Turing",
                        department = "Computer Science",
                        subjects = "Theory of Computation, AI Systems",
                        email = "alan.turing@campus.edu",
                        phone = "+1 (555) 890-1234"
                    ),
                    FacultyEntity(
                        id = "fac_003",
                        name = "Dr. Ada Lovelace",
                        department = "Software Engineering",
                        subjects = "Algorithms, Object-Oriented Programming",
                        email = "ada.lovelace@campus.edu",
                        phone = "+1 (555) 901-2345"
                    ),
                    FacultyEntity(
                        id = "fac_004",
                        name = "Prof. Nikola Tesla",
                        department = "Electrical Engineering",
                        subjects = "Electromagnetics, Power Circuits",
                        email = "nikola.tesla@campus.edu",
                        phone = "+1 (555) 012-3456"
                    )
                )
                facultyMembers.forEach { facultyDao.insertFaculty(it) }

                // Seed Attendance Records
                val today = LocalDate.now().toString()
                val yesterday = LocalDate.now().minusDays(1).toString()
                val attendanceRecords = listOf(
                    AttendanceEntity(
                        studentId = "std_001",
                        studentName = "Alex Mercer",
                        rollNumber = "CS-2024-001",
                        subject = "Data Structures",
                        date = today,
                        status = "Present",
                        markedBy = "Prof. Eleanor Wright"
                    ),
                    AttendanceEntity(
                        studentId = "std_001",
                        studentName = "Alex Mercer",
                        rollNumber = "CS-2024-001",
                        subject = "Algorithms",
                        date = yesterday,
                        status = "Present",
                        markedBy = "Dr. Ada Lovelace"
                    ),
                    AttendanceEntity(
                        studentId = "std_002",
                        studentName = "Maya Lin",
                        rollNumber = "ME-2024-042",
                        subject = "Thermodynamics",
                        date = today,
                        status = "Present",
                        markedBy = "Prof. Eleanor Wright"
                    ),
                    AttendanceEntity(
                        studentId = "std_003",
                        studentName = "Sophia Chen",
                        rollNumber = "EE-2023-018",
                        subject = "Power Circuits",
                        date = today,
                        status = "Present",
                        markedBy = "Prof. Nikola Tesla"
                    ),
                    AttendanceEntity(
                        studentId = "std_004",
                        studentName = "Liam O'Connor",
                        rollNumber = "BA-2025-110",
                        subject = "Business Ethics",
                        date = today,
                        status = "Absent",
                        markedBy = "Prof. Eleanor Wright"
                    )
                )
                attendanceDao.insertAttendanceList(attendanceRecords)

                // Seed Fee Records
                val feeRecords = listOf(
                    FeeEntity(
                        id = "fee_001",
                        studentId = "std_001",
                        studentName = "Alex Mercer",
                        rollNumber = "CS-2024-001",
                        feeType = "Tuition Fee - Fall 2026",
                        amount = 4500.0,
                        status = "Paid",
                        paymentDate = "2026-09-15",
                        receiptNumber = "REC-2026-1042",
                        paymentMethod = "Online Portal"
                    ),
                    FeeEntity(
                        id = "fee_002",
                        studentId = "std_001",
                        studentName = "Alex Mercer",
                        rollNumber = "CS-2024-001",
                        feeType = "Computer Lab & Tech Fee",
                        amount = 350.0,
                        status = "Paid",
                        paymentDate = "2026-09-18",
                        receiptNumber = "REC-2026-1189",
                        paymentMethod = "Credit Card"
                    ),
                    FeeEntity(
                        id = "fee_003",
                        studentId = "std_002",
                        studentName = "Maya Lin",
                        rollNumber = "ME-2024-042",
                        feeType = "Tuition Fee - Fall 2026",
                        amount = 4500.0,
                        status = "Paid",
                        paymentDate = "2026-09-14",
                        receiptNumber = "REC-2026-1033",
                        paymentMethod = "Bank Transfer"
                    ),
                    FeeEntity(
                        id = "fee_004",
                        studentId = "std_003",
                        studentName = "Sophia Chen",
                        rollNumber = "EE-2023-018",
                        feeType = "Tuition Fee - Fall 2026",
                        amount = 4500.0,
                        status = "Pending",
                        paymentDate = "",
                        receiptNumber = "REC-2026-2001",
                        paymentMethod = "Pending"
                    ),
                    FeeEntity(
                        id = "fee_005",
                        studentId = "std_004",
                        studentName = "Liam O'Connor",
                        rollNumber = "BA-2025-110",
                        feeType = "Hostel & Amenities Fee",
                        amount = 1200.0,
                        status = "Overdue",
                        paymentDate = "",
                        receiptNumber = "REC-2026-2055",
                        paymentMethod = "Overdue"
                    )
                )
                feeRecords.forEach { feeDao.insertFee(it) }

                // Seed Exam Results
                val examResults = listOf(
                    ExamResultEntity(
                        id = "res_001",
                        studentId = "std_001",
                        studentName = "Alex Mercer",
                        rollNumber = "CS-2024-001",
                        subject = "Data Structures",
                        marks = 92.5,
                        examType = "Midterm",
                        addedBy = "Prof. Eleanor Wright"
                    ),
                    ExamResultEntity(
                        id = "res_002",
                        studentId = "std_001",
                        studentName = "Alex Mercer",
                        rollNumber = "CS-2024-001",
                        subject = "Algorithms",
                        marks = 88.0,
                        examType = "Midterm",
                        addedBy = "Dr. Ada Lovelace"
                    ),
                    ExamResultEntity(
                        id = "res_003",
                        studentId = "std_001",
                        studentName = "Alex Mercer",
                        rollNumber = "CS-2024-001",
                        subject = "Database Systems",
                        marks = 95.0,
                        examType = "Midterm",
                        addedBy = "Prof. Eleanor Wright"
                    ),
                    ExamResultEntity(
                        id = "res_004",
                        studentId = "std_002",
                        studentName = "Maya Lin",
                        rollNumber = "ME-2024-042",
                        subject = "Thermodynamics",
                        marks = 78.5,
                        examType = "Midterm",
                        addedBy = "Prof. Eleanor Wright"
                    ),
                    ExamResultEntity(
                        id = "res_005",
                        studentId = "std_003",
                        studentName = "Sophia Chen",
                        rollNumber = "EE-2023-018",
                        subject = "Power Circuits",
                        marks = 84.0,
                        examType = "Midterm",
                        addedBy = "Prof. Nikola Tesla"
                    )
                )
                examResults.forEach { examResultDao.insertResult(it) }

                // Seed Notifications
                val notifications = listOf(
                    NotificationEntity(
                        id = "notif_001",
                        title = "Fall 2026 Midterm Examination Schedules Published",
                        message = "All department schedules and exam room allocations are now live on the student portal.",
                        targetRole = "All",
                        senderName = "Office of Academic Affairs"
                    ),
                    NotificationEntity(
                        id = "notif_002",
                        title = "Tuition Fee Due Date Notice",
                        message = "Please clear outstanding tuition fee balances before October 25 to avoid late enrollment charges.",
                        targetRole = "Student",
                        senderName = "Bursar & Finance Office"
                    ),
                    NotificationEntity(
                        id = "notif_003",
                        title = "Faculty Senate Meeting This Thursday",
                        message = "Monthly senate meeting regarding curriculum updates in Hall B at 15:00 EST.",
                        targetRole = "Teacher",
                        senderName = "Dean Arthur Vance"
                    )
                )
                notificationDao.insertNotificationList(notifications)
            }
        }
    }
}
