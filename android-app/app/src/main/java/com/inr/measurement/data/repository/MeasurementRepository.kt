package com.inr.measurement.data.repository

import androidx.lifecycle.LiveData
import com.inr.measurement.data.database.MeasurementDao
import com.inr.measurement.data.models.INRMeasurement

/**
 * Repository for managing INR measurement data
 * Provides a clean API for data access to the rest of the app
 */
class MeasurementRepository(private val measurementDao: MeasurementDao) {

    /**
     * Get all measurements as LiveData
     */
    val allMeasurements: LiveData<List<INRMeasurement>> = measurementDao.getAllMeasurements()

    /**
     * Get a specific measurement by ID
     */
    suspend fun getMeasurementById(id: Long): INRMeasurement? {
        return measurementDao.getMeasurementById(id)
    }

    /**
     * Get recent measurements
     */
    suspend fun getRecentMeasurements(limit: Int = 10): List<INRMeasurement> {
        return measurementDao.getRecentMeasurements(limit)
    }

    /**
     * Insert a new measurement
     */
    suspend fun insertMeasurement(measurement: INRMeasurement): Long {
        return measurementDao.insertMeasurement(measurement)
    }

    /**
     * Update an existing measurement
     */
    suspend fun updateMeasurement(measurement: INRMeasurement) {
        measurementDao.updateMeasurement(measurement)
    }

    /**
     * Delete a measurement
     */
    suspend fun deleteMeasurement(measurement: INRMeasurement) {
        measurementDao.deleteMeasurement(measurement)
    }

    /**
     * Delete measurement by ID
     */
    suspend fun deleteMeasurementById(id: Long) {
        measurementDao.deleteMeasurementById(id)
    }

    /**
     * Delete all measurements
     */
    suspend fun deleteAllMeasurements() {
        measurementDao.deleteAllMeasurements()
    }

    /**
     * Get total count of measurements
     */
    suspend fun getMeasurementCount(): Int {
        return measurementDao.getMeasurementCount()
    }
}
