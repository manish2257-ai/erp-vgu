package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {
    @Query("SELECT * FROM fees ORDER BY createdAt DESC")
    fun getAllFees(): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getFeesByStudent(studentId: String): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE id = :id LIMIT 1")
    suspend fun getFeeById(id: String): FeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeEntity)

    @Query("UPDATE fees SET status = :status, paymentDate = :paymentDate, paymentMethod = :method WHERE id = :id")
    suspend fun updateFeeStatus(id: String, status: String, paymentDate: String, method: String)

    @Query("DELETE FROM fees WHERE id = :id")
    suspend fun deleteFeeById(id: String): Int

    @Query("SELECT SUM(amount) FROM fees WHERE status = 'Paid'")
    fun getTotalPaidAmount(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM fees WHERE status = 'Pending' OR status = 'Overdue'")
    fun getTotalPendingAmount(): Flow<Double?>
}
