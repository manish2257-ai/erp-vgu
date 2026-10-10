const mongoose = require('mongoose');

const ExamResultSchema = new mongoose.Schema(
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
    marks: {
      type: Number,
      required: [true, 'Please provide marks scored'],
      min: [0, 'Marks cannot be negative'],
      max: [100, 'Marks cannot exceed 100']
    },
    maxMarks: {
      type: Number,
      default: 100
    },
    grade: {
      type: String,
      default: 'A'
    },
    semester: {
      type: String,
      default: 'Fall 2026'
    },
    examType: {
      type: String,
      enum: ['Midterm', 'Final', 'Quiz', 'Assignment'],
      default: 'Midterm'
    },
    addedBy: {
      type: String,
      default: 'Faculty'
    }
  },
  {
    timestamps: true
  }
);

// Automatic grade calculator pre-save hook
ExamResultSchema.pre('save', function (next) {
  if (this.marks >= 90) this.grade = 'A+';
  else if (this.marks >= 80) this.grade = 'A';
  else if (this.marks >= 70) this.grade = 'B';
  else if (this.marks >= 60) this.grade = 'C';
  else if (this.marks >= 50) this.grade = 'D';
  else this.grade = 'F';
  next();
});

module.exports = mongoose.model('ExamResult', ExamResultSchema);
