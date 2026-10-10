const Faculty = require('../models/Faculty');

/**
 * @desc    Add a new faculty member (Admin only)
 * @route   POST /api/faculty
 * @access  Private (Admin only)
 */
const addFaculty = async (req, res) => {
  try {
    const { name, department, subjects, email, phone } = req.body;

    if (!name || !department || !subjects || !email) {
      return res.status(400).json({
        success: false,
        message: 'All fields (name, department, subjects, email) are required'
      });
    }

    const existingEmail = await Faculty.findOne({ email: email.toLowerCase() });
    if (existingEmail) {
      return res.status(400).json({
        success: false,
        message: `Faculty member with email '${email}' already exists`
      });
    }

    const subjectArray = Array.isArray(subjects)
      ? subjects
      : subjects.split(',').map((s) => s.trim());

    const faculty = await Faculty.create({
      name,
      department,
      subjects: subjectArray,
      email: email.toLowerCase(),
      phone: phone || ''
    });

    return res.status(201).json({
      success: true,
      message: 'Faculty member added successfully',
      data: faculty
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while adding faculty'
    });
  }
};

/**
 * @desc    Get all faculty members
 * @route   GET /api/faculty
 * @access  Private (Admin, Teacher, Student)
 */
const getAllFaculty = async (req, res) => {
  try {
    const { department, search } = req.query;
    let query = {};

    if (department) {
      query.department = department;
    }

    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } },
        { department: { $regex: search, $options: 'i' } }
      ];
    }

    const faculty = await Faculty.find(query).sort({ name: 1 });

    return res.status(200).json({
      success: true,
      count: faculty.length,
      data: faculty
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching faculty'
    });
  }
};

/**
 * @desc    Get faculty by ID
 * @route   GET /api/faculty/:id
 * @access  Private (Admin, Teacher, Student)
 */
const getFacultyById = async (req, res) => {
  try {
    const faculty = await Faculty.findById(req.params.id);
    if (!faculty) {
      return res.status(404).json({
        success: false,
        message: `Faculty not found with id: ${req.params.id}`
      });
    }

    return res.status(200).json({
      success: true,
      data: faculty
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching faculty member'
    });
  }
};

/**
 * @desc    Update faculty member (Admin only)
 * @route   PUT /api/faculty/:id
 * @access  Private (Admin only)
 */
const updateFaculty = async (req, res) => {
  try {
    let faculty = await Faculty.findById(req.params.id);
    if (!faculty) {
      return res.status(404).json({
        success: false,
        message: `Faculty not found with id: ${req.params.id}`
      });
    }

    const { name, department, subjects, email, phone } = req.body;
    const updateData = {};

    if (name) updateData.name = name;
    if (department) updateData.department = department;
    if (phone) updateData.phone = phone;
    if (subjects) {
      updateData.subjects = Array.isArray(subjects)
        ? subjects
        : subjects.split(',').map((s) => s.trim());
    }
    if (email) {
      updateData.email = email.toLowerCase();
    }

    faculty = await Faculty.findByIdAndUpdate(req.params.id, updateData, {
      new: true,
      runValidators: true
    });

    return res.status(200).json({
      success: true,
      message: 'Faculty member updated successfully',
      data: faculty
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while updating faculty'
    });
  }
};

/**
 * @desc    Delete faculty member (Admin only)
 * @route   DELETE /api/faculty/:id
 * @access  Private (Admin only)
 */
const deleteFaculty = async (req, res) => {
  try {
    const faculty = await Faculty.findById(req.params.id);
    if (!faculty) {
      return res.status(404).json({
        success: false,
        message: `Faculty not found with id: ${req.params.id}`
      });
    }

    await Faculty.findByIdAndDelete(req.params.id);

    return res.status(200).json({
      success: true,
      message: `Faculty member '${faculty.name}' deleted successfully`
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while deleting faculty'
    });
  }
};

module.exports = {
  addFaculty,
  getAllFaculty,
  getFacultyById,
  updateFaculty,
  deleteFaculty
};
