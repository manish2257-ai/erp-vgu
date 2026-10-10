package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "fees")
data class FeeEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val feeType: String = "Tuition Fee",
    val amount: Double,
    val status: String = "Pending", // "Paid", "Pending", "Overdue"
    val paymentDate: String = "", // e.g. "2026-10-09" or empty if pending
    val receiptNumber: String = "REC-" + (1000..9999).random(),
    val paymentMethod: String = "Online Portal",
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
