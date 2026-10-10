const ExamResult = require('../models/ExamResult');
const Student = require('../models/Student');

/**
 * @desc    Add or update exam marks (Teacher & Admin only)
 * @route   POST /api/exams
 * @access  Private (Teacher, Admin)
 */
const addExamMarks = async (req, res) => {
  try {
    const { studentId, subject, marks, examType, semester } = req.body;

    if (!studentId || !subject || marks === undefined) {
      return res.status(400).json({
        success: false,
        message: 'Missing required fields: studentId, subject, and marks'
      });
    }

    const numMarks = Number(marks);
    if (numMarks < 0 || numMarks > 100) {
      return res.status(400).json({
        success: false,
        message: 'Marks must be between 0 and 100'
      });
    }

    const student = await Student.findById(studentId);
    if (!student) {
      return res.status(404).json({
        success: false,
        message: `Student not found with ID: ${studentId}`
      });
    }

    // Determine grade automatically
    let grade = 'F';
    if (numMarks >= 90) grade = 'A+';
    else if (numMarks >= 80) grade = 'A';
    else if (numMarks >= 70) grade = 'B';
    else if (numMarks >= 60) grade = 'C';
    else if (numMarks >= 50) grade = 'D';

    const result = await ExamResult.findOneAndUpdate(
      { studentId, subject, semester: semester || 'Fall 2026' },
      {
        studentName: student.name,
        rollNumber: student.rollNumber,
        marks: numMarks,
        grade,
        examType: examType || 'Midterm',
        addedBy: req.user ? req.user.name : 'Faculty'
      },
      { new: true, upsert: true, runValidators: true }
    );

    return res.status(200).json({
      success: true,
      message: `Marks recorded for ${subject} with Grade ${grade}`,
      data: result
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while recording marks'
    });
  }
};

/**
 * @desc    Get results by student ID with total and percentage calculation
 * @route   GET /api/exams/student/:studentId
 * @access  Private (Admin, Teacher, Student)
 */
const getResultsByStudent = async (req, res) => {
  try {
    const results = await ExamResult.find({ studentId: req.params.studentId }).sort({ subject: 1 });

    const totalMarks = results.reduce((acc, curr) => acc + curr.marks, 0);
    const maxMarks = results.length * 100;
    const percentage = results.length > 0 ? (totalMarks / maxMarks) * 100 : 0;
    const avgScore = results.length > 0 ? totalMarks / results.length : 0;

    let overallGrade = 'F';
    if (avgScore >= 90) overallGrade = 'A+';
    else if (avgScore >= 80) overallGrade = 'A';
    else if (avgScore >= 70) overallGrade = 'B';
    else if (avgScore >= 60) overallGrade = 'C';
    else if (avgScore >= 50) overallGrade = 'D';

    return res.status(200).json({
      success: true,
      count: results.length,
      performance: {
        totalMarksScored: totalMarks,
        maxPossibleMarks: maxMarks,
        percentage: Math.round(percentage * 10) / 10,
        averageScore: Math.round(avgScore * 10) / 10,
        overallGrade
      },
      data: results
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while calculating student results'
    });
  }
};

/**
 * @desc    Get all exam results with subject filter
 * @route   GET /api/exams
 * @access  Private (Admin, Teacher, Student)
 */
const getAllResults = async (req, res) => {
  try {
    const { subject } = req.query;
    let query = {};
    if (subject && subject !== 'All Subjects') {
      query.subject = subject;
    }

    const results = await ExamResult.find(query).sort({ marks: -1 });

    const avg = results.length > 0 ? results.reduce((a, b) => a + b.marks, 0) / results.length : 0;
    const passCount = results.filter((r) => r.marks >= 50).length;
    const passRate = results.length > 0 ? (passCount / results.length) * 100 : 100;

    return res.status(200).json({
      success: true,
      count: results.length,
      analytics: {
        averageScore: Math.round(avg * 10) / 10,
        passRate: Math.round(passRate * 10) / 10
      },
      data: results
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching exam results'
    });
  }
};

module.exports = {
  addExamMarks,
  getResultsByStudent,
  getAllResults
};
