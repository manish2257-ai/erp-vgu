const express = require('express');
const router = express.Router();
const {
  sendNotification,
  getNotifications,
  markAsRead
} = require('../controllers/notificationController');
const { protect, authorizeRoles } = require('../middleware/auth');

router.use(protect);

router
  .route('/')
  .get(getNotifications)
  .post(authorizeRoles('Admin'), sendNotification);

router.route('/:id/read').put(markAsRead);

module.exports = router;
