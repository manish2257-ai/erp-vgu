const express = require('express');
const router = express.Router();
const {
  addFaculty,
  getAllFaculty,
  getFacultyById,
  updateFaculty,
  deleteFaculty
} = require('../controllers/facultyController');
const { protect, authorizeRoles } = require('../middleware/auth');

router.use(protect);

router
  .route('/')
  .get(getAllFaculty)
  .post(authorizeRoles('Admin'), addFaculty);

router
  .route('/:id')
  .get(getFacultyById)
  .put(authorizeRoles('Admin'), updateFaculty)
  .delete(authorizeRoles('Admin'), deleteFaculty);

module.exports = router;
