package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FacultyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyDao {
    @Query("SELECT * FROM faculty ORDER BY name ASC")
    fun getAllFaculty(): Flow<List<FacultyEntity>>

    @Query("SELECT * FROM faculty WHERE id = :id LIMIT 1")
    suspend fun getFacultyById(id: String): FacultyEntity?

    @Query("SELECT * FROM faculty WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getFacultyByEmail(email: String): FacultyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(faculty: FacultyEntity)

    @Update
    suspend fun updateFaculty(faculty: FacultyEntity)

    @Query("DELETE FROM faculty WHERE id = :id")
    suspend fun deleteFacultyById(id: String): Int

    @Query("SELECT COUNT(*) FROM faculty")
    fun countFaculty(): Flow<Int>
}
