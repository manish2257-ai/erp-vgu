const express = require('express');
const router = express.Router();
const {
  registerUser,
  loginUser,
  getMe
} = require('../controllers/authController');
const { protect } = require('../middleware/auth');

// Public auth endpoints
router.post('/register', registerUser);
router.post('/login', loginUser);

// Protected user profile endpoint
router.get('/me', protect, getMe);

module.exports = router;
