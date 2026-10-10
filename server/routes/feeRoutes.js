const express = require('express');
const router = express.Router();
const {
  addFeeRecord,
  getAllFees,
  updatePaymentStatus,
  generateReceipt
} = require('../controllers/feeController');
const { protect, authorizeRoles } = require('../middleware/auth');

router.use(protect);

router
  .route('/')
  .get(getAllFees)
  .post(authorizeRoles('Admin'), addFeeRecord);

router.route('/:id/status').put(authorizeRoles('Admin'), updatePaymentStatus);
router.route('/:id/receipt').get(generateReceipt);

module.exports = router;
