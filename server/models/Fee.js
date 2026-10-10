const mongoose = require('mongoose');

const FeeSchema = new mongoose.Schema(
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
    feeType: {
      type: String,
      required: [true, 'Please provide fee classification / type'],
      trim: true
    },
    amount: {
      type: Number,
      required: [true, 'Please provide fee amount'],
      min: [1, 'Amount must be greater than zero']
    },
    status: {
      type: String,
      enum: ['Paid', 'Pending', 'Overdue'],
      default: 'Pending'
    },
    paymentDate: {
      type: String,
      default: ''
    },
    receiptNumber: {
      type: String,
      required: true,
      unique: true
    },
    paymentMethod: {
      type: String,
      default: 'Online Portal'
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('Fee', FeeSchema);
