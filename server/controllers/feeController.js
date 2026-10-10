const Fee = require('../models/Fee');
const Student = require('../models/Student');

/**
 * @desc    Create a new fee record (Admin only)
 * @route   POST /api/fees
 * @access  Private (Admin only)
 */
const addFeeRecord = async (req, res) => {
  try {
    const { studentId, feeType, amount, status, paymentDate, paymentMethod } = req.body;

    if (!studentId || !feeType || !amount) {
      return res.status(400).json({
        success: false,
        message: 'Missing required parameters: studentId, feeType, and amount'
      });
    }

    if (Number(amount) <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Amount must be greater than zero'
      });
    }

    const student = await Student.findById(studentId);
    if (!student) {
      return res.status(404).json({
        success: false,
        message: `Student not found with ID: ${studentId}`
      });
    }

    const receiptNumber = 'REC-' + Math.floor(100000 + Math.random() * 900000);

    const fee = await Fee.create({
      studentId,
      studentName: student.name,
      rollNumber: student.rollNumber,
      feeType,
      amount: Number(amount),
      status: status || 'Pending',
      paymentDate: status === 'Paid' ? (paymentDate || new Date().toISOString().split('T')[0]) : '',
      receiptNumber,
      paymentMethod: paymentMethod || 'Online Portal'
    });

    return res.status(201).json({
      success: true,
      message: `Fee record created successfully with receipt #${receiptNumber}`,
      data: fee
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while creating fee record'
    });
  }
};

/**
 * @desc    Get all fee records with filters
 * @route   GET /api/fees
 * @access  Private (Admin, Teacher, Student)
 */
const getAllFees = async (req, res) => {
  try {
    const { status, studentId } = req.query;
    let query = {};

    if (status && status !== 'All') {
      query.status = status;
    }
    if (studentId) {
      query.studentId = studentId;
    }

    const fees = await Fee.find(query).sort({ createdAt: -1 });

    const totalCollected = fees
      .filter((f) => f.status === 'Paid')
      .reduce((acc, curr) => acc + curr.amount, 0);

    const totalPending = fees
      .filter((f) => f.status !== 'Paid')
      .reduce((acc, curr) => acc + curr.amount, 0);

    return res.status(200).json({
      success: true,
      count: fees.length,
      analytics: {
        totalCollected,
        totalPending
      },
      data: fees
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while fetching fee records'
    });
  }
};

/**
 * @desc    Update fee payment status (Admin only)
 * @route   PUT /api/fees/:id/status
 * @access  Private (Admin only)
 */
const updatePaymentStatus = async (req, res) => {
  try {
    const { status, paymentDate, paymentMethod } = req.body;

    const fee = await Fee.findById(req.params.id);
    if (!fee) {
      return res.status(404).json({
        success: false,
        message: `Fee record not found with ID: ${req.params.id}`
      });
    }

    fee.status = status || fee.status;
    if (status === 'Paid') {
      fee.paymentDate = paymentDate || new Date().toISOString().split('T')[0];
    }
    if (paymentMethod) {
      fee.paymentMethod = paymentMethod;
    }

    await fee.save();

    return res.status(200).json({
      success: true,
      message: `Fee payment status updated to ${fee.status}`,
      data: fee
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error while updating payment status'
    });
  }
};

/**
 * @desc    Generate simple receipt by fee ID
 * @route   GET /api/fees/:id/receipt
 * @access  Private (Admin, Teacher, Student)
 */
const generateReceipt = async (req, res) => {
  try {
    const fee = await Fee.findById(req.params.id);
    if (!fee) {
      return res.status(404).json({
        success: false,
        message: `Receipt not found for fee ID: ${req.params.id}`
      });
    }

    return res.status(200).json({
      success: true,
      receipt: {
        receiptNumber: fee.receiptNumber,
        studentName: fee.studentName,
        rollNumber: fee.rollNumber,
        feeType: fee.feeType,
        amount: fee.amount,
        status: fee.status,
        paymentDate: fee.paymentDate || 'Pending',
        paymentMethod: fee.paymentMethod,
        institution: 'Campus ERP Institute of Technology',
        generatedAt: new Date().toISOString()
      }
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error generating receipt'
    });
  }
};

module.exports = {
  addFeeRecord,
  getAllFees,
  updatePaymentStatus,
  generateReceipt
};
