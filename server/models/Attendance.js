const mongoose = require('mongoose');

const AttendanceSchema = new mongoose.Schema(
  {
    studentId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Student',
      required: [true, 'Please provide student ID']
    },
    studentName: {
      type: String,
      required: true
    },
    rollNumber: {
      type: String,
      required: true
    },
    subject: {
      type: String,
      required: [true, 'Please provide subject name'],
      trim: true
    },
    date: {
      type: String, // Format: YYYY-MM-DD
      required: [true, 'Please provide date string']
    },
    status: {
      type: String,
      enum: ['Present', 'Absent', 'Late'],
      default: 'Present'
    },
    markedBy: {
      type: String,
      default: 'Faculty'
    }
  },
  {
    timestamps: true
  }
);

// Compound index to ensure uniqueness per student, subject, and date
AttendanceSchema.index({ studentId: 1, subject: 1, date: 1 }, { unique: true });

module.exports = mongoose.model('Attendance', AttendanceSchema);
