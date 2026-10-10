package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExamResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_results ORDER BY createdAt DESC")
    fun getAllResults(): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId ORDER BY subject ASC")
    fun getResultsByStudent(studentId: String): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE subject = :subject ORDER BY marks DESC")
    fun getResultsBySubject(subject: String): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND subject = :subject AND semester = :semester LIMIT 1")
    suspend fun getExistingResult(studentId: String, subject: String, semester: String): ExamResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ExamResultEntity)

    @Update
    suspend fun updateResult(result: ExamResultEntity)

    @Query("DELETE FROM exam_results WHERE id = :id")
    suspend fun deleteResult(id: String): Int

    @Query("SELECT AVG(marks) FROM exam_results")
    fun getAverageMarks(): Flow<Double?>
}
