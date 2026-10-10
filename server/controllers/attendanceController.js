const Attendance = require('../models/Attendance');

/**
 * @desc    Mark or update student attendance (Teacher & Admin)
 * @route   POST /api/attendance
 * @access  Private (Teacher, Admin)
 */
const markAttendance = async (req, res) => {
  try {
    const { studentId, studentName, rollNumber, subject, date, status } = req.body;

    if (!studentId || !studentName || !rollNumber || !subject || !date || !status) {
      return res.status(400).json({
        success: false,
        message: 'Missing required parameters: studentId, studentName, rollNumber, subject, date, status'
      });
    }

    const markedByName = req.user ? req.user.name : 'Faculty';

    // Upsert attendance record for that student, subject, and date
    const record = await Attendance.findOneAndUpdate(
      { studentId, subject, date },
      {
        studentName,
        rollNumber,
        status,
        markedBy: markedByName
      },
      { new: true, upsert: true, runValidators: true }
    );

    return res.status(200).json({
      success: true,
      message: `Attendance for ${studentName} marked as ${status}`,
      data: record
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while marking attendance'
    });
  }
};

/**
 * @desc    Get attendance records by student ID
 * @route   GET /api/attendance/student/:studentId
 * @access  Private (Admin, Teacher, Student)
 */
const getAttendanceByStudent = async (req, res) => {
  try {
    const records = await Attendance.find({ studentId: req.params.studentId }).sort({ date: -1 });

    const total = records.length;
    const present = records.count ? records.filter((r) => r.status === 'Present').length : 0;
    const late = records.filter((r) => r.status === 'Late').length;
    const percentage = total > 0 ? ((present + late * 0.5) / total) * 100 : 100;

    return res.status(200).json({
      success: true,
      count: total,
      attendancePercentage: Math.round(percentage * 10) / 10,
      data: records
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching student attendance'
    });
  }
};

/**
 * @desc    Get attendance filtered by subject and/or date
 * @route   GET /api/attendance
 * @access  Private (Admin, Teacher, Student)
 */
const getAttendance = async (req, res) => {
  try {
    const { subject, date } = req.query;
    let query = {};

    if (subject && subject !== 'All Subjects') {
      query.subject = subject;
    }
    if (date && date !== 'All Dates') {
      query.date = date;
    }

    const records = await Attendance.find(query).sort({ date: -1 });

    return res.status(200).json({
      success: true,
      count: records.length,
      data: records
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching attendance records'
    });
  }
};

module.exports = {
  markAttendance,
  getAttendanceByStudent,
  getAttendance
};
