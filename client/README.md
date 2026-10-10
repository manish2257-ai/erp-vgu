# Campus ERP - Responsive Web Client

Modern, responsive frontend application for the Campus Educational Resource Planning (ERP) platform built with pure **HTML5**, **CSS3 (CSS Variables & Flexbox/Grid)**, and **JavaScript (Fetch API)**.

## Pages & User Interfaces

1. **Authentication & Role Selection (`#auth-modal`)**:
   - Secure Sign In & Register tabs.
   - Quick one-click demo login buttons for **Admin**, **Teacher**, and **Student**.
   - Client-side token storage in `localStorage` (`campus_erp_token` and `campus_erp_user`).

2. **Dashboard Overview (`#view-dashboard`)**:
   - Welcome banner personalized to active user and role.
   - Live KPI Cards: Total Students, Faculty Members, Attendance Average %, Fees Collected/Pending, and Average Exam Score.
   - Dynamic Charts & Analytics:
     - **Attendance Ratio Breakdown**: Color-coded progress bars for Present, Late, and Absent sessions.
     - **Tuition & Fee Progress**: Collected vs Outstanding balances.
     - **Semester Grade Histogram**: Visual bar distribution for grades A+, A, B, C, D, and F.
     - **Department Enrollment Share**: Student distribution across Computer Science, Mechanical Engineering, Electrical Engineering, and Business Administration.

3. **Student Management Directory (`#view-students`)**:
   - Live real-time search by name, email, or roll number.
   - Multi-parameter filtering by academic course/program and year.
   - Data table with roll number, student name, email, department badge, year, and status.
   - **Admin Features**: "Enroll New Student" modal dialog, "Edit Student Details" modal, and student deletion with confirmation.
   - "Academic Report" button linking directly to individual student performance.

4. **Attendance Tracking & Analytics (`#view-attendance`)**:
   - Aggregate KPI summary (Average attendance %, Present logs, Late sessions, Absent counts).
   - Filter by subject, date picker, and search.
   - Log table with status badges (`Present`, `Late`, `Absent`).
   - "Record Attendance" modal dialog (Teachers & Admins).
   - "Check %" action to view individual student attendance percentage and eligibility (≥75%).

5. **Fees Management & Finance (`#view-fees`)**:
   - Financial KPI summary: Total Collected ($), Pending Invoices ($), Overdue ($).
   - Filter by status (`Paid`, `Pending`, `Overdue`) and search.
   - Fee History table with receipt IDs, amounts, categories, and payment dates.
   - "Create Fee Invoice" modal dialog (Admin only).
   - "Update Payment Status" modal dialog (Admin only).
   - **Official Printable Receipt**: Generates a professional university receipt modal with invoice ID, student details, line items, and instant print/PDF support (`window.print()`).

6. **Examinations & Results (`#view-results`)**:
   - Academic KPI cards: Semester Average Score, Pass Rate %, Graded Papers count.
   - Filter by subject and search.
   - Results table with marks, grades, and pass/fail indicators.
   - "Record Exam Marks" modal dialog with automatic grade calculation preview.
   - **Student Academic Grade Card**: Consolidated report card modal detailing all courses, cumulative percentage, and academic standing.

## Connecting to Backend API

The client communicates with the Node.js/Express backend (`/server`) using standard Fetch requests:
- Base URL defaults to: `http://localhost:5000/api`
- Can be customized anytime via the API badge in the sidebar footer.
- **Resilience Engine**: If the backend is starting up or in standby, the client automatically and seamlessly falls back to local data storage, ensuring zero downtime during demonstration and testing.
