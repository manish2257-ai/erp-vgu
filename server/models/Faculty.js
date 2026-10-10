const mongoose = require('mongoose');

const FacultySchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: [true, 'Please provide the faculty full name'],
      trim: true,
      maxlength: [100, 'Faculty name cannot exceed 100 characters']
    },
    department: {
      type: String,
      required: [true, 'Please provide department'],
      trim: true
    },
    subjects: {
      type: [String],
      required: [true, 'Please assign at least one subject']
    },
    email: {
      type: String,
      required: [true, 'Please provide faculty email'],
      unique: true,
      lowercase: true,
      trim: true,
      match: [
        /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/,
        'Please provide a valid email address'
      ]
    },
    phone: {
      type: String,
      default: ''
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('Faculty', FacultySchema);
