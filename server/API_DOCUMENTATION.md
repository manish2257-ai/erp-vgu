# Campus ERP - REST API & Full CRUD Documentation

Welcome to the **Campus ERP** Backend API documentation. This document provides complete specifications for authentication, role-based authorization, testing, and student management CRUD operations.

---

## 🚀 Base URL
- **Local Development:** `http://localhost:5000`
- **Android Emulator Loopback:** `http://10.0.2.2:5000`

---

## 🔑 Authentication & Authorization

All protected routes require an `Authorization` header formatted as:
```http
Authorization: Bearer <JWT_TOKEN>
```

### Role Access Matrix
| Endpoint | Method | Public | Student | Teacher | Admin | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| `/api/test` | GET | ✅ | ✅ | ✅ | ✅ | Server status ping |
| `/api/auth/register` | POST | ✅ | ✅ | ✅ | ✅ | Register a new user |
| `/api/auth/login` | POST | ✅ | ✅ | ✅ | ✅ | Authenticate user & get JWT |
| `/api/auth/me` | GET | ❌ | ✅ | ✅ | ✅ | Get profile of logged-in user |
| `/api/students` | GET | ❌ | ✅ | ✅ | ✅ | List all students |
| `/api/students/:id` | GET | ❌ | ✅ | ✅ | ✅ | Get student details by ID |
| `/api/students` | POST | ❌ | ❌ | ❌ | ✅ | **Add a new student (Admin only)** |
| `/api/students/:id` | PUT | ❌ | ❌ | ❌ | ✅ | **Update student (Admin only)** |
| `/api/students/:id` | DELETE | ❌ | ❌ | ❌ | ✅ | **Delete student (Admin only)** |

---

## 📡 1. System Test API

### `GET /api/test`
Verifies server health, JSON parsing, and connectivity.

- **Request:** None
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "ERP API Server is online and functioning properly!",
  "service": "Campus ERP REST API",
  "version": "1.0.0",
  "timestamp": "2026-10-09T23:30:00.000Z"
}
```

---

## 🔐 2. Authentication APIs

### `POST /api/auth/register`
Creates a new user account with role `Admin`, `Teacher`, or `Student`.

- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "name": "Dr. Sarah Connor",
  "email": "sarah.admin@campus.edu",
  "password": "Password123!",
  "role": "Admin"
}
```
- **Response `201 Created`:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "6614ae8e7a0d1b32f5e10001",
    "name": "Dr. Sarah Connor",
    "email": "sarah.admin@campus.edu",
    "role": "Admin",
    "createdAt": "2026-10-09T23:30:00.000Z"
  }
}
```
- **Response `400 Bad Request`:**
```json
{
  "success": false,
  "message": "User with this email already exists"
}
```

---

### `POST /api/auth/login`
Authenticates an existing user and returns a signed JWT token.

- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "email": "sarah.admin@campus.edu",
  "password": "Password123!"
}
```
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "6614ae8e7a0d1b32f5e10001",
    "name": "Dr. Sarah Connor",
    "email": "sarah.admin@campus.edu",
    "role": "Admin"
  }
}
```
- **Response `401 Unauthorized`:**
```json
{
  "success": false,
  "message": "Invalid email or password credentials"
}
```

---

### `GET /api/auth/me`
Retrieves current authenticated user's information.

- **Headers:** `Authorization: Bearer <token>`
- **Response `200 OK`:**
```json
{
  "success": true,
  "user": {
    "_id": "6614ae8e7a0d1b32f5e10001",
    "name": "Dr. Sarah Connor",
    "email": "sarah.admin@campus.edu",
    "role": "Admin"
  }
}
```

---

## 🎓 3. Student Management APIs (CRUD)

### `GET /api/students`
Retrieves list of all students with optional filtering and search.

- **Headers:** `Authorization: Bearer <token>`
- **Query Parameters (Optional):**
  - `search`: Filter by name, email, or rollNumber
  - `course`: Filter by exact course name (e.g. `Computer Science`)
  - `year`: Filter by academic year (e.g. `2`)
- **Response `200 OK`:**
```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "_id": "6614b0107a0d1b32f5e10010",
      "name": "Alex Mercer",
      "email": "alex.m@campus.edu",
      "course": "Computer Science",
      "year": 3,
      "rollNumber": "CS-2024-001",
      "createdAt": "2026-10-09T22:15:00.000Z"
    },
    {
      "_id": "6614b0257a0d1b32f5e10011",
      "name": "Maya Lin",
      "email": "maya.l@campus.edu",
      "course": "Mechanical Engineering",
      "year": 2,
      "rollNumber": "ME-2024-042",
      "createdAt": "2026-10-09T22:20:00.000Z"
    }
  ]
}
```

---

### `GET /api/students/:id`
Retrieves a single student by MongoDB ObjectId.

- **Headers:** `Authorization: Bearer <token>`
- **Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "_id": "6614b0107a0d1b32f5e10010",
    "name": "Alex Mercer",
    "email": "alex.m@campus.edu",
    "course": "Computer Science",
    "year": 3,
    "rollNumber": "CS-2024-001",
    "createdAt": "2026-10-09T22:15:00.000Z"
  }
}
```
- **Response `404 Not Found`:**
```json
{
  "success": false,
  "message": "Student not found with id: 6614b0107a0d1b32f5e10010"
}
```

---

### `POST /api/students`
Creates and enrolls a new student. **Only Admin role allowed.**

- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer <admin_token>`
- **Body:**
```json
{
  "name": "David Miller",
  "email": "david.m@campus.edu",
  "course": "Information Technology",
  "year": 1,
  "rollNumber": "IT-2026-105"
}
```
- **Response `201 Created`:**
```json
{
  "success": true,
  "message": "Student added successfully",
  "data": {
    "_id": "6614b0557a0d1b32f5e10015",
    "name": "David Miller",
    "email": "david.m@campus.edu",
    "course": "Information Technology",
    "year": 1,
    "rollNumber": "IT-2026-105",
    "createdBy": "6614ae8e7a0d1b32f5e10001",
    "createdAt": "2026-10-09T23:35:00.000Z"
  }
}
```
- **Response `403 Forbidden` (If Teacher or Student tries to add):**
```json
{
  "success": false,
  "message": "Forbidden: User role 'Teacher' is not authorized to access this resource"
}
```
- **Response `400 Bad Request`:**
```json
{
  "success": false,
  "message": "Student with roll number 'IT-2026-105' already exists"
}
```

---

### `PUT /api/students/:id`
Updates an existing student's details. **Only Admin role allowed.**

- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer <admin_token>`
- **Body:**
```json
{
  "course": "Data Science & AI",
  "year": 2
}
```
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Student updated successfully",
  "data": {
    "_id": "6614b0557a0d1b32f5e10015",
    "name": "David Miller",
    "email": "david.m@campus.edu",
    "course": "Data Science & AI",
    "year": 2,
    "rollNumber": "IT-2026-105"
  }
}
```

---

### `DELETE /api/students/:id`
Removes a student record permanently. **Only Admin role allowed.**

- **Headers:** `Authorization: Bearer <admin_token>`
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Student with roll number 'IT-2026-105' deleted successfully",
  "data": {
    "id": "6614b0557a0d1b32f5e10015"
  }
}
```
- **Response `403 Forbidden`:**
```json
{
  "success": false,
  "message": "Forbidden: User role 'Student' is not authorized to access this resource"
}
```

---

## 👨‍🏫 4. Faculty Management APIs (CRUD)

### `GET /api/faculty`
Retrieves list of all faculty members with optional filtering.
- **Headers:** `Authorization: Bearer <token>`
- **Response `200 OK`:**
```json
{
  "success": true,
  "count": 4,
  "data": [
    {
      "_id": "fac_001",
      "name": "Prof. Eleanor Wright",
      "department": "Computer Science",
      "subjects": ["Data Structures", "Database Systems"],
      "email": "teacher@campus.edu",
      "phone": "+1 (555) 789-0123"
    }
  ]
}
```

### `POST /api/faculty` (Admin Only)
Adds a new faculty member and assigns subjects.
- **Headers:** `Authorization: Bearer <admin_token>`, `Content-Type: application/json`
- **Body:**
```json
{
  "name": "Dr. Alan Turing",
  "department": "Computer Science",
  "subjects": ["AI Systems", "Theory of Computation"],
  "email": "alan.turing@campus.edu",
  "phone": "+1 (555) 890-1234"
}
```
- **Response `201 Created` / `403 Forbidden`**

---

## 📅 5. Attendance Module APIs

### `POST /api/attendance` (Teacher & Admin Only)
Marks or updates attendance for a student.
- **Headers:** `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Body:**
```json
{
  "studentId": "std_001",
  "studentName": "Alex Mercer",
  "rollNumber": "CS-2024-001",
  "subject": "Data Structures",
  "date": "2026-10-09",
  "status": "Present"
}
```
- **Response `200 OK`**

### `GET /api/attendance/student/:studentId`
Retrieves attendance records and calculated attendance percentage for a student.
- **Response `200 OK`:**
```json
{
  "success": true,
  "count": 10,
  "attendancePercentage": 95.0,
  "data": [...]
}
```

