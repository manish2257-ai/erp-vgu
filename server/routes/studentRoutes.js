const express = require('express');
const router = express.Router();
const {
  createStudent,
  getAllStudents,
  getStudentById,
  updateStudent,
  deleteStudent
} = require('../controllers/studentController');
const { protect, authorizeRoles } = require('../middleware/auth');

// All student routes require valid JWT authentication
router.use(protect);

// GET /api/students -> accessible by Admin, Teacher, Student
// POST /api/students -> SECURE: only Admin access
router
  .route('/')
  .get(getAllStudents)
  .post(authorizeRoles('Admin'), createStudent);

// GET /api/students/:id -> accessible by Admin, Teacher, Student
// PUT /api/students/:id -> SECURE: only Admin access
// DELETE /api/students/:id -> SECURE: only Admin access
router
  .route('/:id')
  .get(getStudentById)
  .put(authorizeRoles('Admin'), updateStudent)
  .delete(authorizeRoles('Admin'), deleteStudent);

module.exports = router;
