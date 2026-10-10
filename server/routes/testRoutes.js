const express = require('express');
const router = express.Router();

/**
 * @desc    Basic test route to verify server status and JSON parsing
 * @route   GET /api/test
 * @access  Public
 */
router.get('/', (req, res) => {
  return res.status(200).json({
    success: true,
    message: 'ERP API Server is online and functioning properly!',
    service: 'Campus ERP REST API',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

module.exports = router;
