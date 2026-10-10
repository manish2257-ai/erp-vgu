const express = require('express');
const router = express.Router();
const {
  markAttendance,
  getAttendanceByStudent,
  getAttendance
} = require('../controllers/attendanceController');
const { protect, authorizeRoles } = require('../middleware/auth');

router.use(protect);

router
  .route('/')
  .get(getAttendance)
  .post(authorizeRoles('Admin', 'Teacher'), markAttendance);

router.route('/student/:studentId').get(getAttendanceByStudent);

module.exports = router;
