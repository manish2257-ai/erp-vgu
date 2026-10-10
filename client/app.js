/**
 * Campus ERP - Client Application
 * Connects to Node.js/Express Backend via Fetch API with Local Resilience Engine
 */

let API_BASE = localStorage.getItem('campus_erp_api_url') || 'http://localhost:5000/api';
let authToken = localStorage.getItem('campus_erp_token') || null;
let currentUser = JSON.parse(localStorage.getItem('campus_erp_user') || 'null');
let currentView = 'dashboard';
let isBackendConnected = false;

// In-Memory / Local Fallback Seed Data (used when backend is offline or for instant mock preview)
const FALLBACK_STATE = {
  students: [
    { _id: 'std_1', name: 'Alexander Wright', email: 'alex.wright@campus.edu', course: 'Computer Science', year: 3, rollNumber: 'CS2023-014', status: 'Active' },
    { _id: 'std_2', name: 'Sophia Chen', email: 'sophia.chen@campus.edu', course: 'Computer Science', year: 2, rollNumber: 'CS2024-008', status: 'Active' },
    { _id: 'std_3', name: 'Marcus Johnson', email: 'marcus.j@campus.edu', course: 'Mechanical Engineering', year: 4, rollNumber: 'ME2022-045', status: 'Active' },
    { _id: 'std_4', name: 'Elena Rostova', email: 'elena.r@campus.edu', course: 'Electrical Engineering', year: 1, rollNumber: 'EE2025-019', status: 'Active' },
    { _id: 'std_5', name: 'David Kim', email: 'david.kim@campus.edu', course: 'Business Administration', year: 2, rollNumber: 'BA2024-033', status: 'Active' }
  ],
  attendance: [
    { _id: 'att_1', studentId: 'std_1', studentName: 'Alexander Wright', rollNumber: 'CS2023-014', subject: 'Data Structures & Algorithms', date: '2026-10-09', status: 'Present' },
    { _id: 'att_2', studentId: 'std_2', studentName: 'Sophia Chen', rollNumber: 'CS2024-008', subject: 'Operating Systems', date: '2026-10-09', status: 'Present' },
    { _id: 'att_3', studentId: 'std_3', studentName: 'Marcus Johnson', rollNumber: 'ME2022-045', subject: 'Thermodynamics', date: '2026-10-08', status: 'Late' },
    { _id: 'att_4', studentId: 'std_4', studentName: 'Elena Rostova', rollNumber: 'EE2025-019', subject: 'Digital Circuits', date: '2026-10-08', status: 'Present' },
    { _id: 'att_5', studentId: 'std_5', studentName: 'David Kim', rollNumber: 'BA2024-033', subject: 'Business Analytics', date: '2026-10-07', status: 'Absent' }
  ],
  fees: [
    { _id: 'fee_1', studentId: 'std_1', studentName: 'Alexander Wright', rollNumber: 'CS2023-014', feeType: 'Tuition Fee', amount: 3500, status: 'Paid', paymentDate: '2026-09-15', receiptNumber: 'REC-2026-0891' },
    { _id: 'fee_2', studentId: 'std_2', studentName: 'Sophia Chen', rollNumber: 'CS2024-008', feeType: 'Tuition Fee', amount: 3500, status: 'Paid', paymentDate: '2026-09-18', receiptNumber: 'REC-2026-0944' },
    { _id: 'fee_3', studentId: 'std_3', studentName: 'Marcus Johnson', rollNumber: 'ME2022-045', feeType: 'Lab & Library Fee', amount: 850, status: 'Pending', paymentDate: '2026-10-30', receiptNumber: 'REC-2026-1022' },
    { _id: 'fee_4', studentId: 'std_4', studentName: 'Elena Rostova', rollNumber: 'EE2025-019', feeType: 'Tuition Fee', amount: 3200, status: 'Paid', paymentDate: '2026-09-12', receiptNumber: 'REC-2026-0775' },
    { _id: 'fee_5', studentId: 'std_5', studentName: 'David Kim', rollNumber: 'BA2024-033', feeType: 'Hostel & Amenities', amount: 1500, status: 'Overdue', paymentDate: '2026-09-01', receiptNumber: 'REC-2026-0612' }
  ],
  exams: [
    { _id: 'ex_1', studentId: 'std_1', studentName: 'Alexander Wright', rollNumber: 'CS2023-014', subject: 'Data Structures & Algorithms', marks: 92, grade: 'A+', semester: 'Fall 2026' },
    { _id: 'ex_2', studentId: 'std_2', studentName: 'Sophia Chen', rollNumber: 'CS2024-008', subject: 'Operating Systems', marks: 88, grade: 'A', semester: 'Fall 2026' },
    { _id: 'ex_3', studentId: 'std_3', studentName: 'Marcus Johnson', rollNumber: 'ME2022-045', subject: 'Thermodynamics', marks: 74, grade: 'B', semester: 'Fall 2026' },
    { _id: 'ex_4', studentId: 'std_4', studentName: 'Elena Rostova', rollNumber: 'EE2025-019', subject: 'Digital Circuits', marks: 95, grade: 'A+', semester: 'Fall 2026' },
    { _id: 'ex_5', studentId: 'std_5', studentName: 'David Kim', rollNumber: 'BA2024-033', subject: 'Business Analytics', marks: 68, grade: 'C', semester: 'Fall 2026' }
  ]
};

// Initialize Application on DOM Ready
document.addEventListener('DOMContentLoaded', async () => {
  // Set default dates in inputs
  const today = new Date().toISOString().split('T')[0];
  const dateInputs = ['att-date-input', 'fee-date-input'];
  dateInputs.forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = today;
  });

  await checkBackendConnection();

  if (authToken && currentUser) {
    document.getElementById('auth-modal').classList.remove('active');
    updateUserInterfaceHeader();
    navigateTo('dashboard');
  } else {
    // Show auth modal by default if not signed in
    document.getElementById('auth-modal').classList.add('active');
  }
});

/* ==================== API HELPERS ==================== */

async function checkBackendConnection() {
  const dot = document.getElementById('api-status-dot');
  const text = document.getElementById('api-status-text');

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 2000);
    const res = await fetch(`${API_BASE}/test`, { signal: controller.signal });
    clearTimeout(timeoutId);

    if (res.ok) {
      isBackendConnected = true;
      if (dot) {
        dot.style.background = 'var(--color-success)';
        dot.style.boxShadow = '0 0 6px var(--color-success)';
      }
      if (text) text.innerText = 'API: Connected (Live)';
      return true;
    }
  } catch (err) {
    // Backend offline / connecting
  }

  isBackendConnected = false;
  if (dot) {
    dot.style.background = 'var(--color-warning)';
    dot.style.boxShadow = '0 0 6px var(--color-warning)';
  }
  if (text) text.innerText = 'API: Standby (Local Engine)';
  return false;
}

async function apiRequest(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(authToken ? { 'Authorization': `Bearer ${authToken}` } : {}),
    ...(options.headers || {})
  };

  if (isBackendConnected) {
    try {
      const res = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || 'API request failed');
      }
      return data;
    } catch (error) {
      console.warn(`[Backend API] Request to ${endpoint} failed, utilizing local engine fallback:`, error.message);
      // Fallback to local simulation if remote server fails
    }
  }

  // Local fallback response simulation
  return handleLocalFallbackApi(endpoint, options);
}

function handleLocalFallbackApi(endpoint, options) {
  const method = (options.method || 'GET').toUpperCase();
  const body = options.body ? JSON.parse(options.body) : {};

  if (endpoint.startsWith('/dashboard/stats')) {
    const totalStudents = FALLBACK_STATE.students.length;
    const totalFaculty = 4;
    const totalAtt = FALLBACK_STATE.attendance.length;
    const presentAtt = FALLBACK_STATE.attendance.filter(a => a.status === 'Present').length;
    const lateAtt = FALLBACK_STATE.attendance.filter(a => a.status === 'Late').length;
    const attAverage = totalAtt > 0 ? Math.round(((presentAtt + lateAtt * 0.5) / totalAtt) * 1000) / 10 : 92.5;

    const totalPaid = FALLBACK_STATE.fees.filter(f => f.status === 'Paid').reduce((a, b) => a + b.amount, 0);
    const totalPending = FALLBACK_STATE.fees.filter(f => f.status !== 'Paid').reduce((a, b) => a + b.amount, 0);

    const totalExams = FALLBACK_STATE.exams.length;
    const avgScore = totalExams > 0 ? Math.round((FALLBACK_STATE.exams.reduce((a, b) => a + b.marks, 0) / totalExams) * 10) / 10 : 85.0;
    const passCount = FALLBACK_STATE.exams.filter(e => e.marks >= 50).length;
    const passRate = totalExams > 0 ? Math.round((passCount / totalExams) * 1000) / 10 : 100.0;

    return {
      success: true,
      data: {
        kpi: {
          totalStudents,
          totalFaculty,
          attendanceAverage: attAverage,
          totalFeesCollected: totalPaid,
          totalFeesPending: totalPending,
          resultPerformance: { averageScore: avgScore, passRate, totalExamsEvaluated: totalExams }
        },
        charts: {
          departmentEnrollment: [
            { course: 'Computer Science', count: FALLBACK_STATE.students.filter(s => s.course === 'Computer Science').length },
            { course: 'Mechanical Engineering', count: FALLBACK_STATE.students.filter(s => s.course === 'Mechanical Engineering').length },
            { course: 'Electrical Engineering', count: FALLBACK_STATE.students.filter(s => s.course === 'Electrical Engineering').length },
            { course: 'Business Administration', count: FALLBACK_STATE.students.filter(s => s.course === 'Business Administration').length }
          ],
          gradeDistribution: {
            'A+': FALLBACK_STATE.exams.filter(e => e.grade === 'A+').length,
            'A': FALLBACK_STATE.exams.filter(e => e.grade === 'A').length,
            'B': FALLBACK_STATE.exams.filter(e => e.grade === 'B').length,
            'C': FALLBACK_STATE.exams.filter(e => e.grade === 'C').length,
            'D': FALLBACK_STATE.exams.filter(e => e.grade === 'D').length,
            'F': FALLBACK_STATE.exams.filter(e => e.grade === 'F').length
          },
          attendanceBreakdown: {
            present: presentAtt,
            late: lateAtt,
            absent: FALLBACK_STATE.attendance.filter(a => a.status === 'Absent').length
          },
          feeBreakdown: { collected: totalPaid, pending: totalPending }
        }
      }
    };
  }

  // Students endpoint
  if (endpoint.startsWith('/students')) {
    if (method === 'GET') {
      return { success: true, count: FALLBACK_STATE.students.length, data: [...FALLBACK_STATE.students] };
    }
    if (method === 'POST') {
      const newStudent = { _id: 'std_' + Date.now(), ...body, status: 'Active' };
      FALLBACK_STATE.students.push(newStudent);
      return { success: true, data: newStudent };
    }
  }

  // Attendance endpoint
  if (endpoint.startsWith('/attendance')) {
    if (method === 'GET') {
      return { success: true, count: FALLBACK_STATE.attendance.length, data: [...FALLBACK_STATE.attendance] };
    }
    if (method === 'POST') {
      const std = FALLBACK_STATE.students.find(s => s._id === body.studentId) || { name: 'Student', rollNumber: 'CS-000' };
      const newAtt = { _id: 'att_' + Date.now(), studentName: std.name, rollNumber: std.rollNumber, ...body };
      FALLBACK_STATE.attendance.unshift(newAtt);
      return { success: true, data: newAtt };
    }
  }

  // Fees endpoint
  if (endpoint.startsWith('/fees')) {
    if (method === 'GET') {
      return { success: true, count: FALLBACK_STATE.fees.length, data: [...FALLBACK_STATE.fees] };
    }
    if (method === 'POST') {
      const std = FALLBACK_STATE.students.find(s => s._id === body.studentId) || { name: 'Student', rollNumber: 'CS-000' };
      const newFee = {
        _id: 'fee_' + Date.now(),
        studentName: std.name,
        rollNumber: std.rollNumber,
        receiptNumber: 'REC-' + new Date().getFullYear() + '-' + Math.floor(1000 + Math.random() * 9000),
        ...body
      };
      FALLBACK_STATE.fees.unshift(newFee);
      return { success: true, data: newFee };
    }
  }

  // Exams endpoint
  if (endpoint.startsWith('/exams')) {
    if (method === 'GET') {
      return { success: true, count: FALLBACK_STATE.exams.length, data: [...FALLBACK_STATE.exams] };
    }
    if (method === 'POST') {
      const std = FALLBACK_STATE.students.find(s => s._id === body.studentId) || { name: 'Student', rollNumber: 'CS-000' };
      const marks = Number(body.marks);
      let grade = 'F';
      if (marks >= 90) grade = 'A+';
      else if (marks >= 80) grade = 'A';
      else if (marks >= 70) grade = 'B';
      else if (marks >= 60) grade = 'C';
      else if (marks >= 50) grade = 'D';

      const newExam = {
        _id: 'ex_' + Date.now(),
        studentName: std.name,
        rollNumber: std.rollNumber,
        semester: 'Fall 2026',
        marks,
        grade,
        ...body
      };
      FALLBACK_STATE.exams.unshift(newExam);
      return { success: true, data: newExam };
    }
  }

  return { success: true, data: [] };
}

/* ==================== AUTHENTICATION ==================== */

let activeAuthTab = 'login';

function switchAuthTab(tab) {
  activeAuthTab = tab;
  document.getElementById('tab-login').classList.toggle('active', tab === 'login');
  document.getElementById('tab-register').classList.toggle('active', tab === 'register');
  document.getElementById('group-name').style.display = tab === 'register' ? 'block' : 'none';
  document.getElementById('group-role').style.display = tab === 'register' ? 'block' : 'none';
  document.getElementById('auth-submit-btn').innerText = tab === 'login' ? 'Sign In to ERP' : 'Create ERP Account';
  document.getElementById('auth-error').style.display = 'none';
}

async function handleAuthSubmit(e) {
  e.preventDefault();
  const errorDiv = document.getElementById('auth-error');
  errorDiv.style.display = 'none';

  const email = document.getElementById('auth-email').value.trim();
  const password = document.getElementById('auth-password').value.trim();
  const name = document.getElementById('auth-name').value.trim();
  const role = document.getElementById('auth-role').value;

  try {
    let payload;
    let endpoint;

    if (activeAuthTab === 'login') {
      endpoint = '/auth/login';
      payload = { email, password };
    } else {
      endpoint = '/auth/register';
      payload = { name, email, password, role };
    }

    if (isBackendConnected) {
      const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Authentication failed');

      authToken = data.token;
      currentUser = data.user;
    } else {
      // Local fallback auth
      authToken = 'demo_jwt_token_' + Date.now();
      currentUser = {
        id: 'usr_' + Date.now(),
        name: name || (email.includes('admin') ? 'Administrator' : email.includes('teacher') ? 'Prof. Sharma' : 'Student Scholar'),
        email,
        role: activeAuthTab === 'register' ? role : (email.includes('admin') ? 'Admin' : email.includes('teacher') ? 'Teacher' : 'Student')
      };
    }

    localStorage.setItem('campus_erp_token', authToken);
    localStorage.setItem('campus_erp_user', JSON.stringify(currentUser));

    document.getElementById('auth-modal').classList.remove('active');
    updateUserInterfaceHeader();
    navigateTo('dashboard');
    showToast(`Signed in successfully as ${currentUser.name} (${currentUser.role})`, 'success');
  } catch (err) {
    errorDiv.innerText = err.message || 'Authentication error';
    errorDiv.style.display = 'block';
  }
}

function quickLoginRole(role) {
  const accounts = {
    Admin: { name: 'Dr. Arthur Vance', email: 'admin@campus.edu', role: 'Admin' },
    Teacher: { name: 'Prof. Rajesh Sharma', email: 'prof.sharma@campus.edu', role: 'Teacher' },
    Student: { name: 'Alexander Wright', email: 'alex.wright@campus.edu', role: 'Student' }
  };

  const user = accounts[role];
  authToken = 'demo_role_jwt_' + role.toLowerCase();
  currentUser = { id: 'usr_' + role.toLowerCase(), ...user };

  localStorage.setItem('campus_erp_token', authToken);
  localStorage.setItem('campus_erp_user', JSON.stringify(currentUser));

  document.getElementById('auth-modal').classList.remove('active');
  updateUserInterfaceHeader();
  navigateTo('dashboard');
  showToast(`Switched active session to ${role}: ${user.name}`, 'success');
}

function logout() {
  authToken = null;
  currentUser = null;
  localStorage.removeItem('campus_erp_token');
  localStorage.removeItem('campus_erp_user');
  document.getElementById('auth-modal').classList.add('active');
  showToast('Logged out of ERP session', 'info');
}

function updateUserInterfaceHeader() {
  if (!currentUser) return;

  const role = currentUser.role || 'Admin';
  const roleBadge = document.getElementById('top-role-badge');
  roleBadge.innerText = role.toUpperCase();
  roleBadge.className = `role-badge ${role.toLowerCase()}`;

  document.getElementById('top-user-name').innerText = currentUser.name || 'User';
  document.getElementById('top-user-email').innerText = currentUser.email || '';

  // Avatar Initials
  const initials = (currentUser.name || 'U')
    .split(' ')
    .map(p => p[0])
    .join('')
    .substring(0, 2)
    .toUpperCase();
  document.getElementById('top-avatar').innerText = initials;

  // Banner Greeting
  const greetingEl = document.getElementById('banner-greeting');
  if (greetingEl) {
    greetingEl.innerText = `Welcome back, ${currentUser.name} 👋`;
  }

  // Adjust role permissions visibility
  const btnAddStudent = document.getElementById('btn-add-student');
  if (btnAddStudent) btnAddStudent.style.display = role === 'Admin' ? 'inline-flex' : 'none';

  const btnMarkAtt = document.getElementById('btn-mark-att');
  if (btnMarkAtt) btnMarkAtt.style.display = (role === 'Admin' || role === 'Teacher') ? 'inline-flex' : 'none';

  const btnAddFee = document.getElementById('btn-add-fee');
  if (btnAddFee) btnAddFee.style.display = role === 'Admin' ? 'inline-flex' : 'none';

  const btnAddMarks = document.getElementById('btn-add-marks');
  if (btnAddMarks) btnAddMarks.style.display = (role === 'Admin' || role === 'Teacher') ? 'inline-flex' : 'none';
}

/* ==================== NAVIGATION ==================== */

function navigateTo(viewName) {
  currentView = viewName;

  // Update sidebar active class
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.view === viewName);
  });

  // Update views active class
  document.querySelectorAll('.view-section').forEach(view => {
    view.classList.toggle('active', view.id === `view-${viewName}`);
  });

  // Update Topbar Title
  const titles = {
    dashboard: 'Dashboard Overview',
    students: 'Student Management Directory',
    attendance: 'Campus Attendance Tracking',
    fees: 'Fees & Financial Records',
    results: 'Examinations & Performance Results'
  };
  document.getElementById('page-title').innerText = titles[viewName] || 'Campus ERP';

  // Load specific view data
  if (viewName === 'dashboard') refreshDashboardData();
  else if (viewName === 'students') handleStudentSearch();
  else if (viewName === 'attendance') handleAttendanceFilter();
  else if (viewName === 'fees') handleFeeFilter();
  else if (viewName === 'results') handleResultsFilter();
}

/* ==================== VIEW 1: DASHBOARD ==================== */

async function refreshDashboardData() {
  try {
    const res = await apiRequest('/dashboard/stats');
    if (!res || !res.data) return;

    const { kpi, charts } = res.data;

    // Update KPI Cards
    document.getElementById('kpi-students').innerText = kpi.totalStudents ?? 0;
    document.getElementById('kpi-faculty').innerText = kpi.totalFaculty ?? 0;
    document.getElementById('kpi-attendance').innerText = `${kpi.attendanceAverage ?? 92.5}%`;

    const feesCollected = kpi.totalFeesCollected ?? 0;
    const feesPending = kpi.totalFeesPending ?? 0;
    document.getElementById('kpi-fees-collected').innerText = `$${feesCollected.toLocaleString()}`;
    document.getElementById('kpi-fees-pending').innerText = `Pending: $${feesPending.toLocaleString()}`;

    if (kpi.resultPerformance) {
      document.getElementById('kpi-exam-score').innerText = `${kpi.resultPerformance.averageScore ?? 85} / 100`;
      document.getElementById('kpi-pass-rate').innerText = `Pass Rate: ${kpi.resultPerformance.passRate ?? 100}%`;
    }

    // Update Chart 1: Attendance Breakdown
    if (charts.attendanceBreakdown) {
      const { present, late, absent } = charts.attendanceBreakdown;
      const total = (present + late + absent) || 1;
      const pPct = Math.round((present / total) * 100);
      const lPct = Math.round((late / total) * 100);
      const aPct = Math.round((absent / total) * 100);

      document.getElementById('chart-att-present-val').innerText = `${present} logs (${pPct}%)`;
      document.getElementById('chart-att-present-bar').style.width = `${pPct}%`;

      document.getElementById('chart-att-late-val').innerText = `${late} logs (${lPct}%)`;
      document.getElementById('chart-att-late-bar').style.width = `${lPct}%`;

      document.getElementById('chart-att-absent-val').innerText = `${absent} logs (${aPct}%)`;
      document.getElementById('chart-att-absent-bar').style.width = `${aPct}%`;
    }

    // Update Chart 2: Fee Collections
    if (charts.feeBreakdown) {
      const { collected, pending } = charts.feeBreakdown;
      const totalRevenue = (collected + pending) || 1;
      const colPct = Math.round((collected / totalRevenue) * 100);
      const penPct = Math.round((pending / totalRevenue) * 100);

      document.getElementById('chart-fee-collected-val').innerText = `$${collected.toLocaleString()} (${colPct}%)`;
      document.getElementById('chart-fee-collected-bar').style.width = `${colPct}%`;

      document.getElementById('chart-fee-pending-val').innerText = `$${pending.toLocaleString()} (${penPct}%)`;
      document.getElementById('chart-fee-pending-bar').style.width = `${penPct}%`;
    }

    // Update Chart 3: Histogram Grades
    if (charts.gradeDistribution) {
      const gd = charts.gradeDistribution;
      const maxGrade = Math.max(...Object.values(gd), 1);

      const grades = ['aplus', 'a', 'b', 'c', 'd', 'f'];
      const keys = ['A+', 'A', 'B', 'C', 'D', 'F'];

      grades.forEach((g, idx) => {
        const val = gd[keys[idx]] || 0;
        const heightPct = Math.max(8, Math.round((val / maxGrade) * 100));
        const countEl = document.getElementById(`hist-${g}-count`);
        const barEl = document.getElementById(`hist-${g}-bar`);
        if (countEl) countEl.innerText = val;
        if (barEl) barEl.style.height = `${heightPct}%`;
      });
    }

    // Update Chart 4: Department Breakdown
    if (charts.departmentEnrollment) {
      const container = document.getElementById('dept-bars-container');
      container.innerHTML = '';
      const totalStudents = kpi.totalStudents || 1;

      charts.departmentEnrollment.forEach(dept => {
        const pct = Math.round((dept.count / totalStudents) * 100);
        const item = document.createElement('div');
        item.className = 'prog-item';
        item.innerHTML = `
          <div class="prog-label">
            <span>${dept.course}</span>
            <span>${dept.count} students (${pct}%)</span>
          </div>
          <div class="prog-bar-bg">
            <div class="prog-bar-fill purple" style="width: ${pct}%"></div>
          </div>
        `;
        container.appendChild(item);
      });
    }

  } catch (err) {
    console.error('Error fetching dashboard stats:', err);
  }
}

/* ==================== VIEW 2: STUDENTS ==================== */

async function handleStudentSearch() {
  const query = (document.getElementById('student-search').value || '').toLowerCase();
  const course = document.getElementById('student-filter-course').value;
  const year = document.getElementById('student-filter-year').value;

  try {
    let endpoint = '/students';
    const params = new URLSearchParams();
    if (course !== 'All') params.append('course', course);
    if (year !== '0') params.append('year', year);
    if (query) params.append('search', query);
    if (params.toString()) endpoint += `?${params.toString()}`;

    const res = await apiRequest(endpoint);
    let list = res.data || [];

    // Client-side filter fallback
    if (!isBackendConnected) {
      list = list.filter(s => {
        const matchCourse = course === 'All' || s.course === course;
        const matchYear = year === '0' || String(s.year) === year;
        const matchQuery = !query || s.name.toLowerCase().includes(query) || s.rollNumber.toLowerCase().includes(query) || s.email.toLowerCase().includes(query);
        return matchCourse && matchYear && matchQuery;
      });
    }

    renderStudentsTable(list);
  } catch (err) {
    showToast('Failed to load students: ' + err.message, 'error');
  }
}

function renderStudentsTable(students) {
  const tbody = document.getElementById('students-table-body');
  tbody.innerHTML = '';

  if (!students || students.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No student records found matching your filters.</td></tr>`;
    return;
  }

  const isAdmin = currentUser && currentUser.role === 'Admin';

  students.forEach(s => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${s.rollNumber}</strong></td>
      <td>${s.name}</td>
      <td style="color: var(--text-muted);">${s.email}</td>
      <td><span class="badge badge-purple">${s.course}</span></td>
      <td>Year ${s.year}</td>
      <td><span class="badge badge-success">Enrolled</span></td>
      <td>
        <div style="display: flex; gap: 6px;">
          ${isAdmin ? `<button class="btn btn-secondary btn-sm" onclick="openEditStudentModal('${s._id}')">Edit</button>` : ''}
          ${isAdmin ? `<button class="btn btn-danger btn-sm" onclick="handleDeleteStudent('${s._id}', '${s.name}')">Delete</button>` : ''}
          <button class="btn btn-outline btn-sm" onclick="viewStudentReportCard('${s._id}')">Report</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function openAddStudentModal() {
  document.getElementById('modal-student-title').innerText = 'Enroll New Student';
  document.getElementById('student-edit-id').value = '';
  document.getElementById('student-name-input').value = '';
  document.getElementById('student-email-input').value = '';
  document.getElementById('student-roll-input').value = '';
  openModal('modal-student');
}

function openEditStudentModal(id) {
  const student = FALLBACK_STATE.students.find(s => s._id === id);
  if (!student) return;

  document.getElementById('modal-student-title').innerText = 'Edit Student Details';
  document.getElementById('student-edit-id').value = id;
  document.getElementById('student-name-input').value = student.name;
  document.getElementById('student-email-input').value = student.email;
  document.getElementById('student-course-input').value = student.course;
  document.getElementById('student-year-input').value = student.year;
  document.getElementById('student-roll-input').value = student.rollNumber;
  openModal('modal-student');
}

async function handleSaveStudent(e) {
  e.preventDefault();
  const id = document.getElementById('student-edit-id').value;
  const name = document.getElementById('student-name-input').value.trim();
  const email = document.getElementById('student-email-input').value.trim();
  const course = document.getElementById('student-course-input').value;
  const year = Number(document.getElementById('student-year-input').value);
  const rollNumber = document.getElementById('student-roll-input').value.trim();

  try {
    if (id) {
      await apiRequest(`/students/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ name, email, course, year, rollNumber })
      });
      // Fallback state update
      const local = FALLBACK_STATE.students.find(s => s._id === id);
      if (local) Object.assign(local, { name, email, course, year, rollNumber });
      showToast(`Updated student profile for ${name}`, 'success');
    } else {
      await apiRequest('/students', {
        method: 'POST',
        body: JSON.stringify({ name, email, course, year, rollNumber })
      });
      showToast(`Successfully enrolled student ${name}`, 'success');
    }

    closeModal('modal-student');
    handleStudentSearch();
    refreshDashboardData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleDeleteStudent(id, name) {
  if (!confirm(`Are you sure you want to unenroll student "${name}"?`)) return;

  try {
    await apiRequest(`/students/${id}`, { method: 'DELETE' });
    FALLBACK_STATE.students = FALLBACK_STATE.students.filter(s => s._id !== id);
    showToast(`Removed student ${name} from registry`, 'info');
    handleStudentSearch();
    refreshDashboardData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

/* ==================== VIEW 3: ATTENDANCE ==================== */

async function handleAttendanceFilter() {
  const subject = document.getElementById('att-filter-subject').value;
  const date = document.getElementById('att-filter-date').value;
  const query = (document.getElementById('att-search').value || '').toLowerCase();

  try {
    let endpoint = '/attendance';
    const params = new URLSearchParams();
    if (subject !== 'All') params.append('subject', subject);
    if (date) params.append('date', date);
    if (params.toString()) endpoint += `?${params.toString()}`;

    const res = await apiRequest(endpoint);
    let list = res.data || [];

    if (!isBackendConnected) {
      list = list.filter(a => {
        const matchSub = subject === 'All' || a.subject === subject;
        const matchDate = !date || a.date === date;
        const matchQuery = !query || a.studentName.toLowerCase().includes(query) || a.rollNumber.toLowerCase().includes(query);
        return matchSub && matchDate && matchQuery;
      });
    }

    renderAttendanceTable(list);
    updateAttendanceKpis(list);
  } catch (err) {
    showToast('Failed to load attendance: ' + err.message, 'error');
  }
}

function updateAttendanceKpis(records) {
  const total = records.length;
  const present = records.filter(r => r.status === 'Present').length;
  const late = records.filter(r => r.status === 'Late').length;
  const absent = records.filter(r => r.status === 'Absent').length;
  const rate = total > 0 ? Math.round(((present + late * 0.5) / total) * 1000) / 10 : 92.5;

  document.getElementById('att-stats-rate').innerText = `${rate}%`;
  document.getElementById('att-stats-present').innerText = present;
  document.getElementById('att-stats-late').innerText = late;
  document.getElementById('att-stats-absent').innerText = absent;
}

function renderAttendanceTable(records) {
  const tbody = document.getElementById('attendance-table-body');
  tbody.innerHTML = '';

  if (!records || records.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No attendance logs matching selected criteria.</td></tr>`;
    return;
  }

  records.forEach(r => {
    const tr = document.createElement('tr');
    let badgeClass = 'badge-success';
    if (r.status === 'Late') badgeClass = 'badge-warning';
    if (r.status === 'Absent') badgeClass = 'badge-danger';

    tr.innerHTML = `
      <td>${r.date}</td>
      <td><strong>${r.rollNumber}</strong></td>
      <td>${r.studentName}</td>
      <td><span class="badge badge-purple">${r.subject}</span></td>
      <td><span class="badge ${badgeClass}">${r.status}</span></td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="viewIndividualStudentAttendance('${r.studentId}', '${r.studentName}')">
          Check %
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function openMarkAttendanceModal() {
  populateStudentSelect('att-student-select');
  openModal('modal-attendance');
}

async function handleSaveAttendance(e) {
  e.preventDefault();
  const studentId = document.getElementById('att-student-select').value;
  const subject = document.getElementById('att-subject-input').value;
  const date = document.getElementById('att-date-input').value;
  const status = document.getElementById('att-status-input').value;

  try {
    await apiRequest('/attendance', {
      method: 'POST',
      body: JSON.stringify({ studentId, subject, date, status })
    });

    closeModal('modal-attendance');
    showToast(`Recorded attendance (${status}) for session`, 'success');
    handleAttendanceFilter();
    refreshDashboardData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function viewIndividualStudentAttendance(studentId, name) {
  const studentAtt = FALLBACK_STATE.attendance.filter(a => a.studentId === studentId);
  const total = studentAtt.length || 1;
  const present = studentAtt.filter(a => a.status === 'Present').length;
  const late = studentAtt.filter(a => a.status === 'Late').length;
  const pct = Math.round(((present + late * 0.5) / total) * 100);

  alert(`Attendance Profile: ${name}\n\nTotal Recorded Classes: ${total}\nPresent: ${present}\nLate: ${late}\nAbsent: ${total - present - late}\nAttendance Rate: ${pct}%\nStatus: ${pct >= 75 ? 'ELIGIBLE (≥ 75%)' : 'WARNING (< 75%)'}`);
}

/* ==================== VIEW 4: FEES ==================== */

async function handleFeeFilter() {
  const status = document.getElementById('fee-filter-status').value;
  const query = (document.getElementById('fee-search').value || '').toLowerCase();

  try {
    let endpoint = '/fees';
    if (status !== 'All') endpoint += `?status=${status}`;

    const res = await apiRequest(endpoint);
    let list = res.data || [];

    if (!isBackendConnected) {
      list = list.filter(f => {
        const matchStatus = status === 'All' || f.status === status;
        const matchQuery = !query || f.studentName.toLowerCase().includes(query) || f.rollNumber.toLowerCase().includes(query) || f.receiptNumber.toLowerCase().includes(query);
        return matchStatus && matchQuery;
      });
    }

    renderFeesTable(list);
    updateFeeKpis(list);
  } catch (err) {
    showToast('Failed to load fees: ' + err.message, 'error');
  }
}

function updateFeeKpis(records) {
  const paid = records.filter(r => r.status === 'Paid').reduce((a, b) => a + b.amount, 0);
  const pending = records.filter(r => r.status === 'Pending').reduce((a, b) => a + b.amount, 0);
  const overdue = records.filter(r => r.status === 'Overdue').reduce((a, b) => a + b.amount, 0);

  document.getElementById('fee-summary-paid').innerText = `$${paid.toLocaleString()}`;
  document.getElementById('fee-summary-pending').innerText = `$${pending.toLocaleString()}`;
  document.getElementById('fee-summary-overdue').innerText = `$${overdue.toLocaleString()}`;
}

function renderFeesTable(fees) {
  const tbody = document.getElementById('fees-table-body');
  tbody.innerHTML = '';

  if (!fees || fees.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty-state">No fee records found.</td></tr>`;
    return;
  }

  const isAdmin = currentUser && currentUser.role === 'Admin';

  fees.forEach(f => {
    const tr = document.createElement('tr');
    let badgeClass = 'badge-success';
    if (f.status === 'Pending') badgeClass = 'badge-warning';
    if (f.status === 'Overdue') badgeClass = 'badge-danger';

    tr.innerHTML = `
      <td><code>${f.receiptNumber}</code></td>
      <td><strong>${f.rollNumber}</strong></td>
      <td>${f.studentName}</td>
      <td><span class="badge badge-purple">${f.feeType}</span></td>
      <td><strong>$${f.amount.toLocaleString()}</strong></td>
      <td><span class="badge ${badgeClass}">${f.status}</span></td>
      <td>${f.paymentDate}</td>
      <td>
        <div style="display: flex; gap: 6px;">
          <button class="btn btn-outline btn-sm" onclick="viewReceipt('${f._id}')">Receipt 🖨</button>
          ${isAdmin ? `<button class="btn btn-secondary btn-sm" onclick="openUpdateFeeStatusModal('${f._id}', '${f.receiptNumber}', '${f.status}')">Status</button>` : ''}
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function openAddFeeModal() {
  populateStudentSelect('fee-student-select');
  openModal('modal-fee');
}

async function handleSaveFee(e) {
  e.preventDefault();
  const studentId = document.getElementById('fee-student-select').value;
  const feeType = document.getElementById('fee-type-input').value;
  const amount = Number(document.getElementById('fee-amount-input').value);
  const status = document.getElementById('fee-status-input').value;
  const paymentDate = document.getElementById('fee-date-input').value;

  try {
    await apiRequest('/fees', {
      method: 'POST',
      body: JSON.stringify({ studentId, feeType, amount, status, paymentDate })
    });

    closeModal('modal-fee');
    showToast(`Created fee invoice of $${amount} (${status})`, 'success');
    handleFeeFilter();
    refreshDashboardData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function openUpdateFeeStatusModal(id, receipt, currentStatus) {
  document.getElementById('fee-update-id').value = id;
  document.getElementById('fee-update-info').innerText = `Updating payment ledger for Receipt: ${receipt}`;
  document.getElementById('fee-update-status-select').value = currentStatus;
  openModal('modal-fee-status');
}

async function handleUpdateFeeStatus(e) {
  e.preventDefault();
  const id = document.getElementById('fee-update-id').value;
  const status = document.getElementById('fee-update-status-select').value;

  try {
    await apiRequest(`/fees/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    });

    const local = FALLBACK_STATE.fees.find(f => f._id === id);
    if (local) local.status = status;

    closeModal('modal-fee-status');
    showToast(`Updated fee status to ${status}`, 'success');
    handleFeeFilter();
    refreshDashboardData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function viewReceipt(feeId) {
  const fee = FALLBACK_STATE.fees.find(f => f._id === feeId);
  if (!fee) return;

  const content = document.getElementById('receipt-content');
  content.innerHTML = `
    <div class="receipt-header">
      <h2>CAMPUS ERP UNIVERSITY</h2>
      <p style="font-size: 11px; margin-top: 4px;">OFFICIAL ACADEMIC FEE RECEIPT</p>
      <p style="font-size: 10px; color: #64748b; margin-top: 2px;">Receipt ID: ${fee.receiptNumber} | Issued: ${fee.paymentDate}</p>
    </div>
    <div class="receipt-body">
      <div class="receipt-row"><span>Student Name:</span><strong>${fee.studentName}</strong></div>
      <div class="receipt-row"><span>Roll Number:</span><strong>${fee.rollNumber}</strong></div>
      <div class="receipt-row"><span>Fee Category:</span><span>${fee.feeType}</span></div>
      <div class="receipt-row"><span>Payment Term:</span><span>Academic Semester 2026</span></div>
      <div class="receipt-total receipt-row">
        <span>TOTAL AMOUNT:</span>
        <span>$${fee.amount.toLocaleString()}.00 USD</span>
      </div>
      <div class="receipt-row"><span>Ledger Status:</span><strong>${fee.status.toUpperCase()}</strong></div>
      <div style="text-align: center;">
        <span class="receipt-stamp">${fee.status === 'Paid' ? 'VERIFIED PAID' : 'PENDING APPROVAL'}</span>
      </div>
    </div>
  `;
  openModal('modal-receipt');
}

function printReceipt() {
  window.print();
}

/* ==================== VIEW 5: RESULTS ==================== */

async function handleResultsFilter() {
  const subject = document.getElementById('results-filter-subject').value;
  const query = (document.getElementById('results-search').value || '').toLowerCase();

  try {
    let endpoint = '/exams';
    if (subject !== 'All') endpoint += `?subject=${subject}`;

    const res = await apiRequest(endpoint);
    let list = res.data || [];

    if (!isBackendConnected) {
      list = list.filter(e => {
        const matchSub = subject === 'All' || e.subject === subject;
        const matchQuery = !query || e.studentName.toLowerCase().includes(query) || e.rollNumber.toLowerCase().includes(query);
        return matchSub && matchQuery;
      });
    }

    renderResultsTable(list);
    updateResultKpis(list);
  } catch (err) {
    showToast('Failed to load results: ' + err.message, 'error');
  }
}

function updateResultKpis(records) {
  const total = records.length;
  const avg = total > 0 ? Math.round((records.reduce((a, b) => a + b.marks, 0) / total) * 10) / 10 : 0;
  const passCount = records.filter(r => r.marks >= 50).length;
  const passRate = total > 0 ? Math.round((passCount / total) * 1000) / 10 : 100;

  document.getElementById('results-kpi-avg').innerText = `${avg} / 100`;
  document.getElementById('results-kpi-pass').innerText = `${passRate}%`;
  document.getElementById('results-kpi-count').innerText = total;
}

function renderResultsTable(records) {
  const tbody = document.getElementById('results-table-body');
  tbody.innerHTML = '';

  if (!records || records.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No examination results found.</td></tr>`;
    return;
  }

  records.forEach(r => {
    const tr = document.createElement('tr');
    const isPass = r.marks >= 50;
    const statusBadge = isPass ? '<span class="badge badge-success">PASS</span>' : '<span class="badge badge-danger">FAIL</span>';

    tr.innerHTML = `
      <td><strong>${r.rollNumber}</strong></td>
      <td>${r.studentName}</td>
      <td><span class="badge badge-purple">${r.subject}</span></td>
      <td><strong>${r.marks}</strong></td>
      <td><span class="badge badge-blue" style="font-size: 13px;">${r.grade}</span></td>
      <td>${statusBadge}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="viewStudentReportCard('${r.studentId}')">
          Grade Card 📜
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function openAddMarksModal() {
  populateStudentSelect('marks-student-select');
  previewGrade();
  openModal('modal-marks');
}

function previewGrade() {
  const marks = Number(document.getElementById('marks-score-input').value) || 0;
  let grade = 'F';
  if (marks >= 90) grade = 'A+';
  else if (marks >= 80) grade = 'A';
  else if (marks >= 70) grade = 'B';
  else if (marks >= 60) grade = 'C';
  else if (marks >= 50) grade = 'D';

  document.getElementById('marks-grade-preview').value = grade;
}

async function handleSaveMarks(e) {
  e.preventDefault();
  const studentId = document.getElementById('marks-student-select').value;
  const subject = document.getElementById('marks-subject-input').value;
  const marks = Number(document.getElementById('marks-score-input').value);

  try {
    await apiRequest('/exams', {
      method: 'POST',
      body: JSON.stringify({ studentId, subject, marks, semester: 'Fall 2026' })
    });

    closeModal('modal-marks');
    showToast(`Saved score ${marks} for ${subject}`, 'success');
    handleResultsFilter();
    refreshDashboardData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function viewStudentReportCard(studentId) {
  const student = FALLBACK_STATE.students.find(s => s._id === studentId) || { name: 'Student', rollNumber: 'CS-000', course: 'Computer Science' };
  const studentExams = FALLBACK_STATE.exams.filter(e => e.studentId === studentId);

  const container = document.getElementById('report-card-content');
  const total = studentExams.length;
  const totalScore = studentExams.reduce((a, b) => a + b.marks, 0);
  const avg = total > 0 ? Math.round((totalScore / total) * 10) / 10 : 85;

  let rows = '';
  if (studentExams.length === 0) {
    rows = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 16px;">No recorded exams for this student.</td></tr>`;
  } else {
    studentExams.forEach(ex => {
      rows += `
        <tr>
          <td>${ex.subject}</td>
          <td><strong>${ex.marks} / 100</strong></td>
          <td><span class="badge badge-blue">${ex.grade}</span></td>
          <td>${ex.marks >= 50 ? '<span class="badge badge-success">Pass</span>' : '<span class="badge badge-danger">Fail</span>'}</td>
        </tr>
      `;
    });
  }

  container.innerHTML = `
    <div style="background: var(--bg-secondary); padding: 18px; border-radius: var(--radius-md); margin-bottom: 20px;">
      <h4 style="font-size: 16px; font-weight: 700;">${student.name}</h4>
      <p style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Roll Number: ${student.rollNumber} | Course: ${student.course}</p>
      <div style="display: flex; gap: 20px; margin-top: 12px;">
        <div><span style="font-size: 11px; color: var(--text-muted);">Cumulative Average:</span> <strong style="font-size: 16px; color: var(--color-primary);">${avg}%</strong></div>
        <div><span style="font-size: 11px; color: var(--text-muted);">Semester Standing:</span> <strong style="font-size: 16px; color: var(--color-success);">${avg >= 50 ? 'GOOD' : 'PROBATION'}</strong></div>
      </div>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>Subject Name</th>
          <th>Marks Scored</th>
          <th>Grade</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>
  `;

  openModal('modal-report-card');
}

/* ==================== COMMON UTILITIES ==================== */

function populateStudentSelect(selectId) {
  const select = document.getElementById(selectId);
  if (!select) return;
  select.innerHTML = '';

  FALLBACK_STATE.students.forEach(s => {
    const opt = document.createElement('option');
    opt.value = s._id;
    opt.innerText = `${s.name} (${s.rollNumber}) - ${s.course}`;
    select.appendChild(opt);
  });
}

function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('active');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('active');
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerText = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function showApiConfigModal() {
  document.getElementById('api-base-url-input').value = API_BASE;
  openModal('modal-api-config');
}

async function saveApiConfig() {
  const newUrl = document.getElementById('api-base-url-input').value.trim();
  if (newUrl) {
    API_BASE = newUrl;
    localStorage.setItem('campus_erp_api_url', API_BASE);
    closeModal('modal-api-config');
    showToast('Rechecking backend API connectivity...', 'info');
    await checkBackendConnection();
    refreshDashboardData();
  }
}

function handleProfileClick() {
  if (currentUser) {
    alert(`Campus ERP Active Session\n\nName: ${currentUser.name}\nEmail: ${currentUser.email}\nRole: ${currentUser.role}\nToken: ${authToken ? 'Active' : 'None'}`);
  }
}
