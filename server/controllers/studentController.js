const Student = require('../models/Student');

/**
 * @desc    Create / Add a new student
 * @route   POST /api/students
 * @access  Private (Admin only)
 */
const createStudent = async (req, res) => {
  try {
    const { name, email, course, year, rollNumber } = req.body;

    // Field validation
    if (!name || !email || !course || !year || !rollNumber) {
      return res.status(400).json({
        success: false,
        message: 'All fields are required: name, email, course, year, rollNumber'
      });
    }

    // Check if roll number already exists
    const existingRoll = await Student.findOne({ rollNumber: rollNumber.toUpperCase() });
    if (existingRoll) {
      return res.status(400).json({
        success: false,
        message: `Student with roll number '${rollNumber}' already exists`
      });
    }

    // Check if email already exists
    const existingEmail = await Student.findOne({ email: email.toLowerCase() });
    if (existingEmail) {
      return res.status(400).json({
        success: false,
        message: `Student with email '${email}' already exists`
      });
    }

    // Create student
    const student = await Student.create({
      name,
      email: email.toLowerCase(),
      course,
      year: Number(year),
      rollNumber: rollNumber.toUpperCase(),
      createdBy: req.user ? req.user.id : undefined
    });

    return res.status(201).json({
      success: true,
      message: 'Student added successfully',
      data: student
    });
  } catch (error) {
    console.error('[Create Student Error]:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while creating student'
    });
  }
};

/**
 * @desc    Get all students (with optional query filters)
 * @route   GET /api/students
 * @access  Private (Admin, Teacher, Student)
 */
const getAllStudents = async (req, res) => {
  try {
    const { search, course, year } = req.query;
    let query = {};

    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { rollNumber: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } }
      ];
    }

    if (course) {
      query.course = course;
    }

    if (year) {
      query.year = Number(year);
    }

    const students = await Student.find(query).sort({ createdAt: -1 });

    return res.status(200).json({
      success: true,
      count: students.length,
      data: students
    });
  } catch (error) {
    console.error('[Get All Students Error]:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching students'
    });
  }
};

/**
 * @desc    Get single student by ID
 * @route   GET /api/students/:id
 * @access  Private (Admin, Teacher, Student)
 */
const getStudentById = async (req, res) => {
  try {
    const student = await Student.findById(req.params.id);

    if (!student) {
      return res.status(404).json({
        success: false,
        message: `Student not found with id: ${req.params.id}`
      });
    }

    return res.status(200).json({
      success: true,
      data: student
    });
  } catch (error) {
    console.error('[Get Student By ID Error]:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching student'
    });
  }
};

/**
 * @desc    Update student details
 * @route   PUT /api/students/:id
 * @access  Private (Admin only)
 */
const updateStudent = async (req, res) => {
  try {
    let student = await Student.findById(req.params.id);

    if (!student) {
      return res.status(404).json({
        success: false,
        message: `Student not found with id: ${req.params.id}`
      });
    }

    const { name, email, course, year, rollNumber } = req.body;

    // Check duplicate roll number if changed
    if (rollNumber && rollNumber.toUpperCase() !== student.rollNumber) {
      const rollExists = await Student.findOne({ rollNumber: rollNumber.toUpperCase() });
      if (rollExists) {
        return res.status(400).json({
          success: false,
          message: `Another student already has roll number '${rollNumber}'`
        });
      }
    }

    // Check duplicate email if changed
    if (email && email.toLowerCase() !== student.email) {
      const emailExists = await Student.findOne({ email: email.toLowerCase() });
      if (emailExists) {
        return res.status(400).json({
          success: false,
          message: `Another student already has email '${email}'`
        });
      }
    }

    const updateData = {};
    if (name) updateData.name = name;
    if (email) updateData.email = email.toLowerCase();
    if (course) updateData.course = course;
    if (year) updateData.year = Number(year);
    if (rollNumber) updateData.rollNumber = rollNumber.toUpperCase();

    student = await Student.findByIdAndUpdate(req.params.id, updateData, {
      new: true,
      runValidators: true
    });

    return res.status(200).json({
      success: true,
      message: 'Student updated successfully',
      data: student
    });
  } catch (error) {
    console.error('[Update Student Error]:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while updating student'
    });
  }
};

/**
 * @desc    Delete student
 * @route   DELETE /api/students/:id
 * @access  Private (Admin only)
 */
const deleteStudent = async (req, res) => {
  try {
    const student = await Student.findById(req.params.id);

    if (!student) {
      return res.status(404).json({
        success: false,
        message: `Student not found with id: ${req.params.id}`
      });
    }

    await Student.findByIdAndDelete(req.params.id);

    return res.status(200).json({
      success: true,
      message: `Student with roll number '${student.rollNumber}' deleted successfully`,
      data: { id: req.params.id }
    });
  } catch (error) {
    console.error('[Delete Student Error]:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while deleting student'
    });
  }
};

module.exports = {
  createStudent,
  getAllStudents,
  getStudentById,
  updateStudent,
  deleteStudent
};
