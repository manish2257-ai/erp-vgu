const express = require('express');
const router = express.Router();
const {
  addExamMarks,
  getResultsByStudent,
  getAllResults
} = require('../controllers/examController');
const { protect, authorizeRoles } = require('../middleware/auth');

router.use(protect);

router
  .route('/')
  .get(getAllResults)
  .post(authorizeRoles('Admin', 'Teacher'), addExamMarks);

router.route('/student/:studentId').get(getResultsByStudent);

module.exports = router;
