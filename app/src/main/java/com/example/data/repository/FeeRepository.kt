package com.example.data.repository

import com.example.data.local.FeeDao
import com.example.data.model.FeeEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class FeeOpResult {
    data class Success(val message: String, val fee: FeeEntity? = null) : FeeOpResult()
    data class Error(val message: String, val statusCode: Int = 400) : FeeOpResult()
}

class FeeRepository(private val feeDao: FeeDao) {

    val allFees: Flow<List<FeeEntity>> = feeDao.getAllFees()
    val totalPaid: Flow<Double?> = feeDao.getTotalPaidAmount()
    val totalPending: Flow<Double?> = feeDao.getTotalPendingAmount()

    fun getFeesForStudent(studentId: String): Flow<List<FeeEntity>> {
        return feeDao.getFeesByStudent(studentId)
    }

    suspend fun addFeeRecord(
        role: UserRole,
        studentId: String,
        studentName: String,
        rollNumber: String,
        feeType: String,
        amount: Double,
        status: String,
        paymentDate: String,
        paymentMethod: String
    ): FeeOpResult {
        // Enforce RBAC security: Admin controls all fees operations
        if (role != UserRole.ADMIN) {
            return FeeOpResult.Error(
                message = "403 Forbidden: Only Admin accounts can create fee records",
                statusCode = 403
            )
        }

        if (studentId.isBlank() || amount <= 0) {
            return FeeOpResult.Error("Valid student ID and positive amount are required")
        }

        val fee = FeeEntity(
            id = "fee_" + UUID.randomUUID().toString().take(8),
            studentId = studentId,
            studentName = studentName,
            rollNumber = rollNumber,
            feeType = feeType.trim(),
            amount = amount,
            status = status,
            paymentDate = if (status == "Paid") paymentDate else "",
            receiptNumber = "REC-2026-" + (1000..9999).random(),
            paymentMethod = paymentMethod
        )

        feeDao.insertFee(fee)
        return FeeOpResult.Success("Fee record for '$studentName' created successfully", fee)
    }

    suspend fun updatePaymentStatus(
        role: UserRole,
        id: String,
        status: String,
        paymentDate: String,
        paymentMethod: String
    ): FeeOpResult {
        if (role != UserRole.ADMIN) {
            return FeeOpResult.Error(
                message = "403 Forbidden: Only Admin accounts can update payment status",
                statusCode = 403
            )
        }

        feeDao.updateFeeStatus(id, status, paymentDate, paymentMethod)
        return FeeOpResult.Success("Payment status updated to $status")
    }

    suspend fun deleteFee(role: UserRole, id: String): FeeOpResult {
        if (role != UserRole.ADMIN) {
            return FeeOpResult.Error(
                message = "403 Forbidden: Only Admin accounts can delete fee records",
                statusCode = 403
            )
        }

        feeDao.deleteFeeById(id)
        return FeeOpResult.Success("Fee record deleted successfully")
    }
}
