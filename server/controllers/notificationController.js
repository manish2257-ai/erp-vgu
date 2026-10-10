const Notification = require('../models/Notification');

/**
 * @desc    Send / broadcast a new notification
 * @route   POST /api/notifications
 * @access  Private (Admin only)
 */
const sendNotification = async (req, res) => {
  try {
    const { title, message, targetRole } = req.body;

    if (!title || !message) {
      return res.status(400).json({
        success: false,
        message: 'Title and message are required'
      });
    }

    const notification = await Notification.create({
      title,
      message,
      targetRole: targetRole || 'All',
      senderName: req.user ? req.user.name : 'Administration'
    });

    // Optional basic email integration simulation
    console.log(`[Email Integration] Notification broadcasted to ${targetRole} users: ${title}`);

    return res.status(201).json({
      success: true,
      message: 'Notification dispatched successfully',
      data: notification
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error creating notification'
    });
  }
};

/**
 * @desc    Get notifications filtered by user role
 * @route   GET /api/notifications
 * @access  Private (Admin, Teacher, Student)
 */
const getNotifications = async (req, res) => {
  try {
    const userRole = req.user ? req.user.role : 'Student';
    const query = {
      $or: [{ targetRole: 'All' }, { targetRole: userRole }]
    };

    const notifications = await Notification.find(query).sort({ createdAt: -1 });

    return res.status(200).json({
      success: true,
      count: notifications.length,
      data: notifications
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error fetching notifications'
    });
  }
};

/**
 * @desc    Mark notification as read
 * @route   PUT /api/notifications/:id/read
 * @access  Private (Admin, Teacher, Student)
 */
const markAsRead = async (req, res) => {
  try {
    const notification = await Notification.findByIdAndUpdate(
      req.params.id,
      { isRead: true },
      { new: true }
    );

    if (!notification) {
      return res.status(404).json({
        success: false,
        message: 'Notification not found'
      });
    }

    return res.status(200).json({
      success: true,
      data: notification
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error updating notification status'
    });
  }
};

module.exports = {
  sendNotification,
  getNotifications,
  markAsRead
};
