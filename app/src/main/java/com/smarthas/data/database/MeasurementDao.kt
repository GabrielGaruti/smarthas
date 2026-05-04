package com.smarthas.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement): Long

    @Update
    suspend fun update(measurement: Measurement)

    @Query("SELECT * FROM measurements ORDER BY id DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Int): Measurement?

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT COUNT(*) FROM measurements")
    fun getMeasurementCount(): Flow<Int>

    @Query("SELECT * FROM measurements ORDER BY id DESC LIMIT 1")
    suspend fun getLatestMeasurement(): Measurement?

    @Query("SELECT * FROM measurements ORDER BY id DESC LIMIT 1")
    fun getLatestMeasurementFlow(): Flow<Measurement?>
}
