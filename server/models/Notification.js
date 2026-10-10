const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: [true, 'Please provide notification title'],
      trim: true
    },
    message: {
      type: String,
      required: [true, 'Please provide notification message'],
      trim: true
    },
    targetRole: {
      type: String,
      enum: ['All', 'Admin', 'Teacher', 'Student'],
      default: 'All'
    },
    senderName: {
      type: String,
      default: 'Administration'
    },
    isRead: {
      type: Boolean,
      default: false
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('Notification', NotificationSchema);
