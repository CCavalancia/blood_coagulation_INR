package com.inr.measurement.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.inr.measurement.data.models.INRMeasurement

/**
 * Data Access Object for INR measurements
 */
@Dao
interface MeasurementDao {

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): LiveData<List<INRMeasurement>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): INRMeasurement?

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMeasurements(limit: Int): List<INRMeasurement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: INRMeasurement): Long

    @Update
    suspend fun updateMeasurement(measurement: INRMeasurement)

    @Delete
    suspend fun deleteMeasurement(measurement: INRMeasurement)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)

    @Query("DELETE FROM measurements")
    suspend fun deleteAllMeasurements()

    @Query("SELECT COUNT(*) FROM measurements")
    suspend fun getMeasurementCount(): Int
}
